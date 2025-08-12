package greencity.service.utility;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EntityManagerUtils {
    @PersistenceContext
    private EntityManager entityManager;

    private static PersistenceUnitUtil persistenceUnitUtil;

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
    private static final String INVALID_GETTER_EXCEPTION = "One or more specified attributes getter method can't be invoked";

    @PostConstruct
    private void init() {
        persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    public <T> EntityGraph<T> createEntityGraph(Class<T> entityClass, List<String> attributes) {
        EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityClass);

        Map<String, Subgraph<?>> subgraphsMap = new HashMap<>();

        for (String attribute : attributes) {
            if (!attribute.contains(".")) {
                try {
                    entityGraph.addAttributeNodes(attribute);
                } catch (Exception exception) {
                    throw new IllegalStateException(INVALID_ARGUMENT_EXCEPTION);
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
                        String rootSubgraphPath = String.join(".", Arrays.copyOfRange(subAttributes, 0, length-2));
                        Subgraph<?> rootSubgraph = subgraphsMap.get(rootSubgraphPath);
                        Subgraph<?> subgraph = rootSubgraph.addSubgraph(subAttributes[length - 2]);
                        subgraph.addAttributeNodes(subAttributes[length - 1]);
                        subgraphsMap.put(String.join(".", rootSubgraphPath, subAttributes[length - 2]), subgraph);
                    }
                } catch (Exception exception) {
                    throw new IllegalStateException(INVALID_ARGUMENT_EXCEPTION);
                }
            }
        }

        return entityGraph;
    }

    public <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes) {
        return createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes, EntityGraphType.LOAD);
    }

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

    public <T> Page<T> createAndRunPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes,
        List<String> pluralAttributes, Pageable pageable) {
        TypedQuery<T> query = createPageableTypedQueryWithEntityGraph(
            entityClass, jpqlQueryString, attributes, pageable);
        return runPageableTypedQueryWithEntityGraph(
            entityClass, query, jpqlQueryString, pluralAttributes, pageable);
    }

    public <T> Page<T> createAndRunPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes, Pageable pageable) {
        TypedQuery<T> query = createPageableTypedQueryWithEntityGraph(
            entityClass, jpqlQueryString, attributes, pageable);
        return runPageableTypedQueryWithEntityGraph(query, jpqlQueryString, pageable);
    }

    public <T> Page<T> runPageableTypedQueryWithEntityGraph(
        TypedQuery<T> query, String jpqlQueryString, Pageable pageable) {
        List<T> results = query.getResultList();
        Long total = createAndRunCountQueryFor(query, jpqlQueryString);
        return new PageImpl<>(results, pageable, total);
    }

    public <T, C, ID> Page<T> runPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, TypedQuery<T> query, String jpqlQueryString,
        List<String> pluralAttributes, Pageable pageable) {
        Page<T> page = runPageableTypedQueryWithEntityGraph(query, jpqlQueryString, pageable);

        if (!page.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<ID> ids = page.getContent().stream()
                .map(entity -> (ID) persistenceUnitUtil.getIdentifier(entity))
                .toList();

            EntityType<T> meta = entityManager.getMetamodel().entity(entityClass);
            for (String pluralAttributeString : pluralAttributes) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) meta
                    .getAttribute(pluralAttributeString);

                Field collectionField;
                try {
                    collectionField = entityClass.getDeclaredField(pluralAttributeString);
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException(INVALID_ARGUMENT_EXCEPTION);
                }

                String childProperty;
                if (collectionField.isAnnotationPresent(OneToMany.class)) {
                    childProperty = collectionField.getAnnotation(OneToMany.class).mappedBy();
                } else if (collectionField.isAnnotationPresent(ManyToMany.class)) {
                    childProperty = collectionField.getAnnotation(ManyToMany.class).mappedBy();
                } else if (collectionField.isAnnotationPresent(ElementCollection.class)){
                    childProperty = collectionField.getName();
                } else {
                    throw new IllegalStateException(INVALID_ARGUMENT_EXCEPTION);
                }

                @SuppressWarnings("unchecked")
                Class<C> bagEntityClass = (Class<C>) pluralAttribute.getElementType().getJavaType();

                @SuppressWarnings("unchecked")
                List<C> bag = (List<C>) batchFetchBag(
                    pluralAttribute.getElementType().getJavaType(), childProperty, ids);

                mapFetchedBagToEntity(
                    page.getContent(), collectionField,
                    bagEntityClass, bag, childProperty);
            }
        }

        return page;
    }

    public <T> TypedQuery<T> createPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, Pageable pageable) {
        TypedQuery<T> query = createTypedQueryWithEntityGraph(entityClass, jpqlQueryString, attributes);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query;
    }

    public <C, ID> List<C> batchFetchBag(
        Class<C> bagEntityClass, String foreignKeyProperty, List<ID> parentIds) {
        String entityName = entityManager.getMetamodel().entity(bagEntityClass).getName();

        String jpql = String.format(
            "select distinct c from %s c where c.%s.id in :ids", entityName, foreignKeyProperty);

        TypedQuery<C> query = entityManager.createQuery(jpql, bagEntityClass);
        query.setParameter("ids", parentIds);

        return query.getResultList();
    }

    public <T, C, ID> void mapFetchedBagToEntity(
        List<T> parents, Field collectionField, Class<C> bagEntityClass,
        List<C> bag, String foreignKeyProperty) {
        Method getter;
        try {
            getter = bagEntityClass.getMethod("get" + Pattern.compile("^.")
                .matcher(foreignKeyProperty)
                .replaceFirst(matchResult -> matchResult.group().toUpperCase()));
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(INVALID_GETTER_EXCEPTION);
        }

        @SuppressWarnings("unchecked")
        Map<ID, List<C>> groupedByParent = bag.stream()
            .collect(Collectors.groupingBy(bagEntity -> {
                try {
                    Object entity = getter.invoke(bagEntity);
                    return (ID) persistenceUnitUtil.getIdentifier(entity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(INVALID_GETTER_EXCEPTION);
                }
            }));

        for (T parent : parents) {
            @SuppressWarnings("unchecked")
            ID id = (ID) persistenceUnitUtil.getIdentifier(parent);
            List<C> children = groupedByParent.getOrDefault(id, List.of());
            setCollection(parent, collectionField, children);
        }
    }

    private <T, C> void setCollection(T parent, Field collectionField, List<C> children) {
        Class<?> fieldType = collectionField.getType();

        Collection<C> newCollection = switch (fieldType.getSimpleName()) {
          case "List" -> new ArrayList<>(children);
          case "Set" -> new HashSet<>(children);
          case "SortedSet" -> new TreeSet<>(children);
          default -> {
                try {
                    @SuppressWarnings("unchecked")
                    Collection<C> instance = (Collection<C>) fieldType
                        .getDeclaredConstructor()
                        .newInstance();
                    instance.addAll(children);
                    yield instance;
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException exception) {
                    throw new IllegalStateException(exception.getMessage());
                }
            }
        };

        collectionField.setAccessible(true);
        try {
            collectionField.set(parent, newCollection);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception.getMessage());
        }
    }

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

        return countQuery.replaceFirst("(?iu)\\s+order\\s+by\\s+.*", "");
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
