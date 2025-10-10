package greencity.service.utility;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Parameter;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.hibernate.jpa.QueryHints.JAKARTA_HINT_FETCHGRAPH;
import static org.hibernate.jpa.QueryHints.JAKARTA_HINT_LOADGRAPH;

@Service
public class EntityManagerUtils {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String IDENTIFIER_GROUP =
        String.format("(%s)", "[._$[\\P{Z}&&\\P{Cc}&&\\P{Cf}&&\\P{Punct}]]+");
    private static final Pattern STARTS_WITH_PAREN = Pattern.compile("^\\s*\\(");
    private static final Pattern PARENS_TO_REMOVE = Pattern
        .compile("(\\(.*\\bfrom\\b[^)]+\\))",
            Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_BY_PATTERN = Pattern
        .compile("\\s+order\\s+by\\s+.*\\z",
            Pattern.UNICODE_CASE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern COUNT_MATCH;
    private static final Pattern ALIAS_MATCH;

    private static final String ATTRIBUTE_DELIMITER = ".";

    static {
        StringBuilder builder = new StringBuilder();
        builder.append("\\s*");
        builder.append("(select\\s+((distinct)?((?s).+?)?)\\s+)?(from\\s+");
        builder.append("[._$[\\P{Z}&&\\P{Cc}&&\\P{Cf}&&\\P{Punct}]]+");
        builder.append("(?:\\s+as)?\\s+)");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(.*)");
        COUNT_MATCH = Pattern.compile(builder.toString(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        builder = new StringBuilder();
        builder.append("(?<=\\bfrom)");
        builder.append("(?:\\s)+");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(?:\\sas)*");
        builder.append("(?:\\s)+");
        builder.append("(?!(?:where|group\\s*by|order\\s*by))(\\w+)");
        ALIAS_MATCH = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    }

    public static final String ENTITY_GRAPH_ARGUMENT_EXCEPTION = "One or more specified attributes can't be applied";

    private record Key(String queryString, String countProjection, boolean nativeQuery) {
    }

    private final LoadingCache<Key, String> countQueryStringCache;

    public EntityManagerUtils(
        @Value("${greencity.cache.countQueryCacheMaximumEntries}") long cacheMaximumEntries,
        @Value("${greencity.cache.countQueryCacheLifetime}") long cacheLifeDuration) {
        countQueryStringCache = Caffeine.newBuilder()
            .maximumSize(cacheMaximumEntries)
            .expireAfterWrite(cacheLifeDuration, TimeUnit.MINUTES)
            .build(
                key -> createCountQueryStringForInternal(key.queryString(), key.countProjection(), key.nativeQuery()));
    }

    /**
     * Methods creates EntityGraph which can be then set as fetchgraph or loadgraph
     * query hint. This method is well-suited and proves effectiveness in fixing n+1
     * via eager loading of chosen manyToOne or oneToOne relations, while only slows
     * down a query in case of collection-type relations usage due to cartesian
     * product emerging.
     *
     * @param entityClass class of Entity that entityGraph is built for;
     * @param attributes  names of relation fields to be fetched, order is important
     *                    in case of nested relations, you can't specify a.b.c
     *                    before specifying a.b;
     * @return {@link EntityGraph} ready-to-use EntityGraph object with all
     *         attributes and subgraphs set.
     * @author Oleksandr Ilnytskyi
     */
    public <T> EntityGraph<T> createEntityGraph(Class<T> entityClass, List<String> attributes) {
        EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityClass);

        Map<String, Subgraph<?>> subgraphsMap = new HashMap<>();

        for (String attribute : attributes) {
            if (!attribute.contains(ATTRIBUTE_DELIMITER)) {
                try {
                    entityGraph.addAttributeNodes(attribute);
                } catch (Exception exception) {
                    throw new IllegalStateException(ENTITY_GRAPH_ARGUMENT_EXCEPTION);
                }
            } else {
                try {
                    String[] subAttributes = attribute.split("\\" + ATTRIBUTE_DELIMITER);
                    int length = subAttributes.length;
                    if (length == 2) {
                        Subgraph<?> subgraph = entityGraph.addSubgraph(subAttributes[length - 2]);
                        subgraph.addAttributeNodes(subAttributes[length - 1]);
                        subgraphsMap.put(subAttributes[0], subgraph);
                    } else {
                        String rootSubgraphPath =
                            String.join(ATTRIBUTE_DELIMITER, Arrays.copyOfRange(subAttributes, 0, length - 2));
                        Subgraph<?> rootSubgraph = subgraphsMap.get(rootSubgraphPath);
                        Subgraph<?> subgraph = rootSubgraph.addSubgraph(subAttributes[length - 2]);
                        subgraph.addAttributeNodes(subAttributes[length - 1]);
                        subgraphsMap.put(String.join(ATTRIBUTE_DELIMITER, rootSubgraphPath, subAttributes[length - 2]),
                            subgraph);
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException(ENTITY_GRAPH_ARGUMENT_EXCEPTION);
                }
            }
        }

        return entityGraph;
    }

    /**
     * Methods creates TypedQuery for given jpql query string with EntityGraph,
     * setting it as loadgraph query hint. This method is well-suited and proves
     * effectiveness in fixing n+1 via eager loading of chosen manyToOne or oneToOne
     * relations, while only slows down a query in case of collection-type relations
     * usage due to cartesian product emerging.
     *
     * @param entityClass     class of resulting Entity;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @param attributes      names of relation fields to be fetched, order is
     *                        important in case of nested relations, you can't
     *                        specify a.b.c before specifying a.b;
     * @return {@link TypedQuery} query with loadgraph hint set. Parameters are to
     *         be set manually and needs to be executed via one of getResults()
     *         methods.
     * @author Oleksandr Ilnytskyi
     */
    public <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes) {
        return createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes, EntityGraphType.LOAD);
    }

    /**
     * Methods creates TypedQuery for given jpql query string with EntityGraph,
     * where type of hint can be chosen. This method is well-suited and proves
     * effectiveness in fixing n+1 via eager loading of chosen manyToOne or oneToOne
     * relations, while only slows down a query in case of collection-type relations
     * usage due to cartesian product emerging.
     *
     * @param entityClass     class of resulting Entity;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @param attributes      names of relation fields to be fetched, order is
     *                        important in case of nested relations, you can't
     *                        specify a.b.c before specifying a.b;
     * @param entityGraphType entity graph type to be applied in hint;
     * @return {@link TypedQuery} query with loadgraph hint set. Parameters are to
     *         be set manually and needs to be executed via one of getResults()
     *         methods.
     * @author Oleksandr Ilnytskyi
     */
    public <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, EntityGraphType entityGraphType) {
        TypedQuery<T> query = entityManager.createQuery(jpqlQueryString, entityClass);

        String hintKey = entityGraphType == EntityGraphType.FETCH
            ? JAKARTA_HINT_FETCHGRAPH
            : JAKARTA_HINT_LOADGRAPH;
        query.setHint(hintKey, createEntityGraph(entityClass, attributes));

        return query;
    }

    /**
     * Methods creates TypedQuery for given jpql query string with EntityGraph,
     * setting it as loadgraph query hint, applies paging to the query and runs both
     * query and count query to populate all {@link PageImpl} fields. Useful in case
     * query has no parameters and can be run immediately after creation.
     *
     * @param entityClass     class of Entity, with which resulting page content is
     *                        populated;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @param attributes      names of relation fields to be fetched, order is
     *                        important in case of nested relations, you can't
     *                        specify a.b.c before specifying a.b;
     * @param pageable        resulting page parameters;
     * @return {@link Page} query result as a page.
     * @author Oleksandr Ilnytskyi
     */
    public <T> Page<T> createAndRunPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes, Pageable pageable) {
        TypedQuery<T> query = createPageableTypedQueryWithEntityGraph(
            entityClass, jpqlQueryString, attributes, pageable);
        return runPageableTypedQueryWithEntityGraph(query, pageable);
    }

    /**
     * Methods runs TypedQuery, creates and runs count query to populate all
     * {@link PageImpl} fields. Useful when query has parameters that need to be set
     * manually before running.
     *
     * @param query    query to be run;
     * @param pageable resulting page parameters;
     * @return {@link Page} query result as a page.
     * @author Oleksandr Ilnytskyi
     */
    public <T> Page<T> runPageableTypedQueryWithEntityGraph(
        TypedQuery<T> query, Pageable pageable) {
        List<T> results = query.getResultList();
        Long total = createAndRunCountQueryFor(query);
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Methods creates TypedQuery for given jpql query string with EntityGraph,
     * setting it as loadgraph query hint, applies paging to the query. If query
     * contains any parameters, they need to be set manually before running.
     *
     * @param entityClass     class of Entity, with which resulting page content is
     *                        populated;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @param attributes      names of relation fields to be fetched, order is
     *                        important in case of nested relations, you can't
     *                        specify a.b.c before specifying a.b;
     * @param pageable        resulting page parameters;
     * @return {@link TypedQuery} query with loadgraph hint set. Parameters are to
     *         be set manually and query has to be run via
     *         runPageableTypedQueryWithEntityGraph() method.
     * @author Oleksandr Ilnytskyi
     */
    public <T> TypedQuery<T> createPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, Pageable pageable) {
        TypedQuery<T> query = createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query;
    }

    /**
     * Methods creates and runs count query based on query created with
     * jpqlQueryString, removing elements that are not allowed in count queries.
     *
     * @param query original query from which parameters for count query will be
     *              parsed;
     * @return {@link Long} total amount of elements as query result.
     * @author Oleksandr Ilnytskyi
     */
    public <T> Long createAndRunCountQueryFor(TypedQuery<T> query) {
        String jpqlQueryString = query.unwrap(Query.class).getQueryString();
        String countQueryString = createCountQueryStringFor(jpqlQueryString);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        query.getParameters().forEach(parameter -> setParameterFromQuery(query, countQuery, parameter));

        return countQuery.getSingleResult();
    }

    private static <T, C> void setParameterFromQuery(
        TypedQuery<T> queryFrom, TypedQuery<C> queryTo, Parameter<?> parameter) {
        if (parameter.getName() != null) {
            queryTo.setParameter(parameter.getName(), queryFrom.getParameterValue(parameter));
        } else {
            queryTo.setParameter(parameter.getPosition(), queryFrom.getParameterValue(parameter));
        }
    }

    public String createCountQueryStringFor(String jpqlQueryString) {
        return createCountQueryStringFor(jpqlQueryString, null);
    }

    public String createCountQueryStringFor(String jpqlQueryString, @Nullable String countProjection) {
        return createCountQueryStringFor(jpqlQueryString, countProjection, false);
    }

    public String createCountQueryStringFor(
        String queryString, @Nullable String countProjection, boolean nativeQuery) {
        Key key = new Key(queryString, countProjection, nativeQuery);
        return countQueryStringCache.get(key);
    }

    private static String createCountQueryStringForInternal(
        String queryString, @Nullable String countProjection, boolean nativeQuery) {
        Assert.hasText(queryString, "OriginalQuery must not be null or empty");
        Matcher matcher = COUNT_MATCH.matcher(queryString);
        String countQuery;
        if (countProjection == null) {
            String variable = determineVariable(matcher);
            String replacement = determineReplacement(matcher, variable, queryString, nativeQuery);
            countQuery = buildCountQueryString(matcher, replacement);
        } else {
            countQuery = buildCountQueryString(matcher, countProjection);
        }

        return ORDER_BY_PATTERN.matcher(countQuery).replaceFirst("");
    }

    private static String buildCountQueryString(Matcher matcher, String replacement) {
        return matcher.replaceFirst(String.format("select count(%s) $5$6$7", replacement));
    }

    private static String determineVariable(Matcher matcher) {
        return matcher.matches() ? matcher.group(4) : null;
    }

    private static String determineReplacement(Matcher matcher, String variable, String jpqlQueryString,
        boolean nativeQuery) {
        boolean hasComplexCount = matcher.matches() && StringUtils.hasText(matcher.group(3));
        String complexCountValue = hasComplexCount ? "$3 $6" : "$6";

        boolean useVariable = StringUtils.hasText(variable)
            && !variable.startsWith("new")
            && !variable.startsWith(" new")
            && !variable.startsWith("count(")
            && !variable.contains(",");
        String replacement = useVariable ? "$2" : complexCountValue;

        if (variable != null && nativeQuery && (variable.contains(",") || "*".equals(variable))) {
            return "1";
        }

        if ("*".equals(variable)) {
            String alias = detectAlias(jpqlQueryString);
            if (alias != null) {
                return alias;
            }
        }

        return replacement;
    }

    private static String detectAlias(String queryString) {
        String refinedQueryString = removeSubqueries(queryString);
        Matcher matcher = ALIAS_MATCH.matcher(refinedQueryString);
        String alias = null;

        while (matcher.find()) {
            alias = matcher.group(2);
        }
        return alias;
    }

    private static String removeSubqueries(String query) {
        if (!StringUtils.hasText(query)) {
            return query;
        } else {
            List<Integer> opens = new ArrayList<>();
            List<Integer> closes = new ArrayList<>();
            List<Boolean> closeMatches = new ArrayList<>();
            findParens(query, opens, closes, closeMatches);
            return clearEligibleParens(query, opens, closes, closeMatches);
        }
    }

    private static void findParens(
        String query, List<Integer> opens, List<Integer> closes, List<Boolean> closeMatches) {
        for (int i = 0; i < query.length(); ++i) {
            char c = query.charAt(i);
            if (c == '(') {
                opens.add(i);
            } else if (c == ')') {
                closes.add(i);
                closeMatches.add(Boolean.FALSE);
            }
        }
    }

    private static String clearEligibleParens(
        String query, List<Integer> opens, List<Integer> closes, List<Boolean> closeMatches) {
        StringBuilder sb = new StringBuilder(query);
        boolean startsWithParen = STARTS_WITH_PAREN.matcher(query).find();

        for (int i = opens.size() - 1; i >= (startsWithParen ? 1 : 0); --i) {
            Integer open = opens.get(i);
            int close = findClose(open, closes, closeMatches) + 1;
            if (close > open) {
                String subquery = sb.substring(open, close);
                Matcher matcher = PARENS_TO_REMOVE.matcher(subquery);
                if (matcher.find()) {
                    sb.replace(open, close, (new String(new char[close - open])).replace('\u0000', ' '));
                }
            }
        }

        return sb.toString();
    }

    private static Integer findClose(final Integer open, final List<Integer> closes, final List<Boolean> closeMatches) {
        for (int i = 0; i < closes.size(); ++i) {
            int close = closes.get(i);
            if (close > open && Boolean.TRUE.equals(!(Boolean) closeMatches.get(i))) {
                closeMatches.set(i, Boolean.TRUE);
                return close;
            }
        }

        return -1;
    }
}
