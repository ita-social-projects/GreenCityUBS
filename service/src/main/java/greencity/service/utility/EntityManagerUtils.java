package greencity.service.utility;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EntityManagerUtils {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String IDENTIFIER_GROUP =
        String.format("(%s)", "[._$[\\P{Z}&&\\P{Cc}&&\\P{Cf}&&\\P{Punct}]]+");
    private static final Pattern STARTS_WITH_PAREN = Pattern.compile("^\\s*\\(");
    private static final Pattern PARENS_TO_REMOVE = Pattern.compile("(\\(.*\\bfrom\\b[^)]+\\))", 42);
    private static final Pattern ORDER_BY_PATTERN = Pattern
        .compile("(?iu)\\s+order\\s+by\\s+[\\s\\S]*\\z", 98);
    private static final Pattern COUNT_MATCH;
    private static final Pattern ALIAS_MATCH;

    static {
        StringBuilder builder = new StringBuilder();
        builder.append("\\s*");
        builder.append("(select\\s+((distinct)?((?s).+?)?)\\s+)?(from\\s+");
        builder.append("[._$[\\P{Z}&&\\P{Cc}&&\\P{Cf}&&\\P{Punct}]]+");
        builder.append("(?:\\s+as)?\\s+)");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(.*)");
        COUNT_MATCH = Pattern.compile(builder.toString(), 34);
        builder = new StringBuilder();
        builder.append("(?<=\\bfrom)");
        builder.append("(?:\\s)+");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(?:\\sas)*");
        builder.append("(?:\\s)+");
        builder.append("(?!(?:where|group\\s*by|order\\s*by))(\\w+)");
        ALIAS_MATCH = Pattern.compile(builder.toString(), 2);
    }

    public static final String ENTITY_GRAPH_ARGUMENT_EXCEPTION = "One or more specified attributes can't be applied";

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
            if (!attribute.contains(".")) {
                try {
                    entityGraph.addAttributeNodes(attribute);
                } catch (Exception exception) {
                    throw new IllegalStateException(ENTITY_GRAPH_ARGUMENT_EXCEPTION);
                }
            } else {
                try {
                    String[] subAttributes = attribute.split("\\.");
                    int length = subAttributes.length;
                    if (length == 2) {
                        Subgraph<?> subgraph = entityGraph.addSubgraph(subAttributes[length - 2]);
                        subgraph.addAttributeNodes(subAttributes[length - 1]);
                        subgraphsMap.put(subAttributes[0], subgraph);
                    } else {
                        String rootSubgraphPath = String.join(".", Arrays.copyOfRange(subAttributes, 0, length - 2));
                        Subgraph<?> rootSubgraph = subgraphsMap.get(rootSubgraphPath);
                        Subgraph<?> subgraph = rootSubgraph.addSubgraph(subAttributes[length - 2]);
                        subgraph.addAttributeNodes(subAttributes[length - 1]);
                        subgraphsMap.put(String.join(".", rootSubgraphPath, subAttributes[length - 2]), subgraph);
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
            ? "jakarta.persistence.fetchgraph"
            : "jakarta.persistence.loadgraph";
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
        return runPageableTypedQueryWithEntityGraph(query, jpqlQueryString, pageable);
    }

    /**
     * Methods runs TypedQuery, creates and runs count query to populate all
     * {@link PageImpl} fields. Useful when query has parameters that need to be set
     * manually before running.
     *
     * @param query           query to be run;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @param pageable        resulting page parameters;
     * @return {@link Page} query result as a page.
     * @author Oleksandr Ilnytskyi
     */
    public <T> Page<T> runPageableTypedQueryWithEntityGraph(
        TypedQuery<T> query, String jpqlQueryString, Pageable pageable) {
        List<T> results = query.getResultList();
        Long total = createAndRunCountQueryFor(query, jpqlQueryString);
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
     * Methods creates and runs count query based on jpqlQueryString, removing
     * elements that are not allowed in count queries.
     *
     * @param query           original query from which parameters for count query
     *                        will be parsed;
     * @param jpqlQueryString jakarta persistence query language string, same as
     *                        used in JPA repository @Query() methods;
     * @return {@link Long} total amount of elements as query result.
     * @author Oleksandr Ilnytskyi
     */
    public <T> Long createAndRunCountQueryFor(TypedQuery<T> query, String jpqlQueryString) {
        String countQueryString = createCountQueryStringFor(jpqlQueryString);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        query.getParameters()
            .forEach(parameter -> countQuery.setParameter(parameter.getName(), query.getParameterValue(parameter)));
        return countQuery.getSingleResult();
    }

    private static String createCountQueryStringFor(String jpqlQueryString) {
        return createCountQueryStringFor(jpqlQueryString, null);
    }

    private static String createCountQueryStringFor(String jpqlQueryString, @Nullable String countProjection) {
        return createCountQueryStringFor(jpqlQueryString, countProjection, false);
    }

    private static String createCountQueryStringFor(
        String jpqlQueryString, @Nullable String countProjection, boolean nativeQuery) {
        Assert.hasText(jpqlQueryString, "OriginalQuery must not be null or empty");
        Matcher matcher = COUNT_MATCH.matcher(jpqlQueryString);
        String countQuery;
        if (countProjection == null) {
            String variable = matcher.matches() ? matcher.group(4) : null;
            boolean useVariable = StringUtils.hasText(variable) && !variable.startsWith("new")
                && !variable.startsWith(" new") && !variable.startsWith("count(") && !variable.contains(",");
            String complexCountValue = matcher.matches() && StringUtils.hasText(matcher.group(3)) ? "$3 $6" : "$6";
            String replacement = useVariable ? "$2" : complexCountValue;
            if (variable != null && nativeQuery && (variable.contains(",") || "*".equals(variable))) {
                replacement = "1";
            } else {
                String alias = detectAlias(jpqlQueryString);
                if ("*".equals(variable) && alias != null) {
                    replacement = alias;
                }
            }

            countQuery = matcher.replaceFirst(String.format("select count(%s) $5$6$7", replacement));
        } else {
            countQuery = matcher.replaceFirst(String.format("select count(%s) $5$6$7", countProjection));
        }

        return ORDER_BY_PATTERN.matcher(countQuery).replaceFirst("");
    }

    private static String detectAlias(String jpqlQueryString) {
        String alias = null;

        for (Matcher matcher = ALIAS_MATCH.matcher(removeSubqueries(jpqlQueryString)); matcher.find(); alias =
            matcher.group(2)) {
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

            for (int i = 0; i < query.length(); ++i) {
                char c = query.charAt(i);
                if (c == '(') {
                    opens.add(i);
                } else if (c == ')') {
                    closes.add(i);
                    closeMatches.add(Boolean.FALSE);
                }
            }

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
    }

    private static Integer findClose(final Integer open, final List<Integer> closes, final List<Boolean> closeMatches) {
        for (int i = 0; i < closes.size(); ++i) {
            int close = closes.get(i);
            if (close > open && !(Boolean) closeMatches.get(i)) {
                closeMatches.set(i, Boolean.TRUE);
                return close;
            }
        }

        return -1;
    }
}
