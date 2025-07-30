package greencity.service.utility;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.List;
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

    private static final String INVALID_ARGUMENT_EXCEPTION = "One or more specified attributes can't be applied";

    protected <T> EntityGraph<T> createEntityGraph(Class<T> entityClass, List<String> attributes) {
        EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityClass);
        if (attributes != null) {
            for (String attribute : attributes) {
                try {
                    entityGraph.addAttributeNodes(attribute);
                } catch (Exception exception) {
                    throw new IllegalStateException(INVALID_ARGUMENT_EXCEPTION);
                }
            }
        }
        return entityGraph;
    }

    protected <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes) {
        return createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes, EntityGraphType.LOAD);
    }

    protected <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, EntityGraphType entityGraphType) {
        TypedQuery<T> query = entityManager.createQuery(jpqlQueryString, entityClass);

        String hintKey = entityGraphType == EntityGraphType.FETCH
            ? "jakarta.persistence.loadgraph"
            : "jakarta.persistence.fetchgraph";
        query.setHint(hintKey, createEntityGraph(entityClass, attributes));

        return query;
    }

    protected <T> Page<T> runPageableTypedQueryWithFetchGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, Pageable pageable) {
        TypedQuery<T> query = createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<T> results = query.getResultList();

        String countQueryString = createCountQueryFor(jpqlQueryString);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    private static String createCountQueryFor(String jpqlQueryString) {
        return createCountQueryFor(jpqlQueryString, null);
    }

    private static String createCountQueryFor(String jpqlQueryString, @Nullable String countProjection) {
        return createCountQueryFor(jpqlQueryString, countProjection, false);
    }

    private static String createCountQueryFor(
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

        return countQuery.replaceFirst("(?iu)\\s+order\\s+by\\s+.*", "");
    }

    private static String detectAlias(String jpqlQueryString) {
        String alias = null;

        for (Matcher matcher = ALIAS_MATCH.matcher(removeSubqueries(jpqlQueryString)); matcher.find(); alias =
            matcher.group(2)) {
        }

        return alias;
    }

    static String removeSubqueries(String query) {
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
