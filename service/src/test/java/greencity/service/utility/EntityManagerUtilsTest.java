package greencity.service.utility;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.Parameter;
import jakarta.persistence.Subgraph;
import jakarta.persistence.TypedQuery;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static greencity.service.utility.EntityManagerUtils.ENTITY_GRAPH_ARGUMENT_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityManagerUtilsTest {

    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private EntityGraph<DummyEntity> entityGraph;

    @Mock
    private Subgraph<?> subgraphLevel1;

    @Mock
    private Subgraph<?> subgraphLevel2;

    @Mock
    private TypedQuery<DummyEntity> typedQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @Mock
    private Query<?> unwrappedQuery;

    @InjectMocks
    private EntityManagerUtils entityManagerUtils = new EntityManagerUtils(1);

    private static class DummyEntity {
    }

    private static final String BASIC_ATTRIBURE_NAME = "relatedEntity";
    private static final String WRONG_ATTRIBUTE_NAME = "unrelatedEntity";

    static final String ATTRIBUTE_ERROR_MESSAGE = "Attribute doesn't exist";

    @Test
    void createEntityGraphWithSingleAttribute() {
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);

        EntityGraph<DummyEntity> result = entityManagerUtils.createEntityGraph(DummyEntity.class, List.of(BASIC_ATTRIBURE_NAME));

        assertSame(entityGraph, result);
        verify(entityGraph).addAttributeNodes(BASIC_ATTRIBURE_NAME);
        verifyNoMoreInteractions(entityGraph);
    }

    @Test
    void createEntityGraphWithTwoLevelNestedAttribute() {
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);
        when(entityGraph.addSubgraph("parent")).thenReturn((Subgraph<Object>) subgraphLevel1);

        EntityGraph<DummyEntity> result = entityManagerUtils.createEntityGraph(DummyEntity.class, List.of("parent.child"));

        assertSame(entityGraph, result);
        verify(entityGraph).addSubgraph("parent");
        verify(subgraphLevel1).addAttributeNodes("child");
    }

    @Test
    void createEntityGraphThreeLevelNestedAttribute() {
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);

        when(entityGraph.addSubgraph("a")).thenReturn((Subgraph<Object>) subgraphLevel1);
        when(subgraphLevel1.addSubgraph("b")).thenReturn((Subgraph<Object>) subgraphLevel2);

        EntityGraph<DummyEntity> result = entityManagerUtils.createEntityGraph(DummyEntity.class, List.of("a.b", "a.b.c"));

        assertSame(entityGraph, result);
        verify(entityGraph).addSubgraph("a");
        verify(subgraphLevel1).addAttributeNodes("b");
        verify(subgraphLevel1).addSubgraph("b");
        verify(subgraphLevel2).addAttributeNodes("c");
    }

    @Test
    void createEntityGraphWithSingleAttributeWhenCantBeApplied() {
        List<String> attributeNames = List.of(WRONG_ATTRIBUTE_NAME);

        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);
        doThrow(new RuntimeException(ATTRIBUTE_ERROR_MESSAGE)).when(entityGraph)
            .addAttributeNodes(WRONG_ATTRIBUTE_NAME);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> entityManagerUtils.createEntityGraph(DummyEntity.class, attributeNames));

        assertTrue(ex.getMessage().contains(ENTITY_GRAPH_ARGUMENT_EXCEPTION));
    }

    @Test
    void createEntityGraphWithNestedAttributesWhenCantBeApplied() {
        List<String> attributeNames = List.of("parent", "parent.child");

        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);
        when(entityGraph.addSubgraph("parent")).thenReturn((Subgraph<Object>) subgraphLevel1);
        doThrow(new RuntimeException(ATTRIBUTE_ERROR_MESSAGE)).when(subgraphLevel1).addAttributeNodes("child");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> entityManagerUtils.createEntityGraph(DummyEntity.class, attributeNames));

        assertTrue(ex.getMessage().contains(ENTITY_GRAPH_ARGUMENT_EXCEPTION));
    }

    @Test
    void createTypedQueryWithEntityGraphSetsLoadgraphHintByDefault() {
        String jpql = "select d from DummyEntity d";
        when(entityManager.createQuery(jpql, DummyEntity.class)).thenReturn(typedQuery);
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);

        TypedQuery<DummyEntity> result =
            entityManagerUtils.createTypedQueryWithEntityGraph(DummyEntity.class, jpql, List.of("relatedEntity"));

        assertSame(typedQuery, result);
        verify(typedQuery).setHint("jakarta.persistence.loadgraph", entityGraph);
    }

    @Test
    void createTypedQueryWithEntityGraphSetsFetchgraphHintWhenSpecified() {
        String jpql = "select d from DummyEntity d";
        when(entityManager.createQuery(jpql, DummyEntity.class)).thenReturn(typedQuery);
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);

        TypedQuery<DummyEntity> result = entityManagerUtils.createTypedQueryWithEntityGraph(
            DummyEntity.class, jpql, List.of(BASIC_ATTRIBURE_NAME), EntityGraphType.FETCH);

        assertSame(typedQuery, result);
        verify(typedQuery).setHint("jakarta.persistence.fetchgraph", entityGraph);
    }

    @Test
    void createPageableTypedQueryWithEntityGraph() {
        String jpql = "select d from DummyEntity d";
        Pageable pageable = PageRequest.of(2, 5);
        when(entityManager.createQuery(jpql, DummyEntity.class)).thenReturn(typedQuery);
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);

        TypedQuery<DummyEntity> q = entityManagerUtils.createPageableTypedQueryWithEntityGraph(
            DummyEntity.class, jpql, List.of(), pageable);

        assertSame(typedQuery, q);
        verify(typedQuery).setFirstResult((int) pageable.getOffset());
        verify(typedQuery).setMaxResults(pageable.getPageSize());
    }

    @Test
    void runPageableTypedQueryWithEntityGraph() {
        String jpql = "select d from DummyEntity d";
        Pageable pageable = PageRequest.of(0, 10);

        List<DummyEntity> results = List.of(new DummyEntity(), new DummyEntity());

        when(typedQuery.getResultList()).thenReturn(results);
        when(typedQuery.unwrap(Query.class)).thenReturn(unwrappedQuery);
        when(unwrappedQuery.getQueryString()).thenReturn(jpql);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(123L);
        when(typedQuery.getParameters()).thenReturn(Collections.emptySet());

        Page<DummyEntity> page = entityManagerUtils.runPageableTypedQueryWithEntityGraph(typedQuery, pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(123L, page.getTotalElements());
        assertEquals(results, page.getContent());
    }

    @Test
    void createAndRunPageableTypedQueryWithEntityGraph() {
        String jpql = "select d from DummyEntity d";
        Pageable pageable = PageRequest.of(0, 2);
        List<DummyEntity> results = List.of(new DummyEntity(), new DummyEntity());

        when(entityManager.createQuery(jpql, DummyEntity.class)).thenReturn(typedQuery);
        when(entityManager.createEntityGraph(DummyEntity.class)).thenReturn(entityGraph);
        when(typedQuery.getResultList()).thenReturn(results);
        when(typedQuery.unwrap(Query.class)).thenReturn(unwrappedQuery);
        when(unwrappedQuery.getQueryString()).thenReturn(jpql);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(123L);
        when(typedQuery.getParameters()).thenReturn(Collections.emptySet());

        Page<DummyEntity> page = entityManagerUtils.createAndRunPageableTypedQueryWithEntityGraph(
            DummyEntity.class, jpql, List.of(BASIC_ATTRIBURE_NAME), pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(123L, page.getTotalElements());
        assertEquals(results, page.getContent());
    }

    @Test
    void createAndRunCountQueryFor() {
        String jpql = "select d from DummyEntity d where d.x = :p1";

        @SuppressWarnings("unchecked")
        Parameter<Object> parameter = mock(Parameter.class);
        when(parameter.getName()).thenReturn("p1");

        when(typedQuery.unwrap(Query.class)).thenReturn(unwrappedQuery);
        when(unwrappedQuery.getQueryString()).thenReturn(jpql);
        when(typedQuery.getParameters()).thenReturn(Set.of(parameter));
        when(typedQuery.getParameterValue(parameter)).thenReturn("VALUE");
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(7L);

        Long total = entityManagerUtils.createAndRunCountQueryFor(typedQuery);

        assertEquals(7L, total);
        verify(countQuery).setParameter("p1", "VALUE");
    }

    @Test
    void createAndRunCountQueryForWithPositionalParameter() {
        String jpql = "select d from DummyEntity d where d.x = ?1";

        @SuppressWarnings("unchecked")
        Parameter<Object> parameter = mock(Parameter.class);
        when(parameter.getName()).thenReturn(null);
        when(parameter.getPosition()).thenReturn(1);

        when(typedQuery.unwrap(Query.class)).thenReturn(unwrappedQuery);
        when(unwrappedQuery.getQueryString()).thenReturn(jpql);
        when(typedQuery.getParameters()).thenReturn(Set.of(parameter));
        when(typedQuery.getParameterValue(parameter)).thenReturn("VALUE");
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(7L);

        Long total = entityManagerUtils.createAndRunCountQueryFor(typedQuery);

        assertEquals(7L, total);
        verify(countQuery).setParameter(1, "VALUE");
    }

    @Test
    void createCountQueryStringForNativeQueryWithStar() {
        String queryString = "select * from DummyEntity d";
        String result = entityManagerUtils.createCountQueryStringFor(queryString, null, true);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertTrue(result.contains("count(1)"), "native query with '*' must use count(1)");
    }

    @Test
    void createCountQueryStringForNativeQueryWithComma() {
        String queryString = "select a, b from DummyEntity d";
        String result = entityManagerUtils.createCountQueryStringFor(queryString, null, true);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertTrue(result.contains("count(1)"), "native query with comma must use count(1)");
    }

    @Test
    void createCountQueryStringForQueryWithStarUsesAlias() {
        String jpql = "select * from DummyEntity d";
        String result = entityManagerUtils.createCountQueryStringFor(jpql, null, false);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertTrue(result.contains("count(d)"), "jpql query with '*' must use count(d)");
    }

    @Test
    void createCountQueryStringForQueryWithDistinctWithOrderBy() {
        String jpql = "select distinct d.id, d.name from DummyEntity d where d.x = :p order by d.name";
        String result = entityManagerUtils.createCountQueryStringFor(jpql, null, false);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertTrue(result.toLowerCase().contains("distinct"));
        assertFalse(result.toLowerCase().contains("order by"));
    }

    @Test
    void createCountQueryStringWithCountProjection() {
        String jpql = "select d from DummyEntity d where d.x = :p";
        String projection = "distinct d.id";
        String result = entityManagerUtils.createCountQueryStringFor(jpql, projection, false);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertTrue(result.contains("count(distinct d.id)"), "should use provided count projection");
    }

    @Test
    void createCountQueryStringForQueryWithConstructorExpression() {
        String jpql = "select new com.example.Dto(d.x) from DummyEntity d where d.x = 1";
        String result = entityManagerUtils.createCountQueryStringFor(jpql, null, false);

        assertTrue(result.toLowerCase().startsWith("select count("));
        assertFalse(result.toLowerCase().contains("count(new"));
        assertFalse(result.toLowerCase().contains("new"));
    }

    @Test
    void createCountQueryStringForEmptyQuery() {
        assertThrows(IllegalArgumentException.class,
            () -> entityManagerUtils.createCountQueryStringFor("", null, false));
    }

    @Test
    void detectAlias() throws Exception {
        Method detectAlias = EntityManagerUtils.class.getDeclaredMethod("detectAlias", String.class);
        detectAlias.setAccessible(true);

        String jpql = "select d from DummyEntity d where d.x = 1 order by d.x";
        String alias = (String) detectAlias.invoke(null, jpql);

        assertEquals("d", alias, "alias should be detected as 'd'");
    }

    @Test
    void detectAliasWhenNoAliasPresent() throws Exception {
        Method detectAlias = EntityManagerUtils.class.getDeclaredMethod("detectAlias", String.class);
        detectAlias.setAccessible(true);

        String jpql = "select * from DummyEntity";
        String alias = (String) detectAlias.invoke(null, jpql);

        assertNull(alias);
    }

    @Test
    void removeSubqueries() throws Exception {
        Method removeSubqueries = EntityManagerUtils.class
            .getDeclaredMethod("removeSubqueries", String.class);
        removeSubqueries.setAccessible(true);

        String jpql = "select d from DummyEntity d where d.x in (select x from OtherEntity o where o.flag = true)";
        String result = (String) removeSubqueries.invoke(null, jpql);

        assertFalse(result.contains("select x from OtherEntity"), "subquery contents must be removed");
        assertTrue(result.contains("from DummyEntity"), "outer query must remain intact");
    }

    @Test
    void removeSubqueriesWithNestedSubqueries() throws Exception {
        Method removeSubqueries = EntityManagerUtils.class
            .getDeclaredMethod("removeSubqueries", String.class);
        removeSubqueries.setAccessible(true);

        String jpql = "select d from DummyEntity d where d.x in (" +
            " select x from SecondEntity s where s.y in (" +
            "   select y from ThirdEntity t where t.flag = true" +
            " )" +
            ") and d.active = true";
        String result = (String) removeSubqueries.invoke(null, jpql);

        assertFalse(result.contains("select x from SecondEntity"));
        assertFalse(result.contains("select y from ThirdEntity"));
        assertTrue(result.contains("select d from DummyEntity"));
        assertTrue(result.contains("d.active = true"));
    }

    @Test
    void removeSubqueriesWhenQueryIsNull() throws Exception {
        Method removeSubqueries = EntityManagerUtils.class
            .getDeclaredMethod("removeSubqueries", String.class);
        removeSubqueries.setAccessible(true);

        String result = (String) removeSubqueries.invoke(null, new Object[] { null });
        assertNull(result);
    }

    @Test
    void removeSubqueriesWhenQueryIsBlank() throws Exception {
        Method removeSubqueries = EntityManagerUtils.class
            .getDeclaredMethod("removeSubqueries", String.class);
        removeSubqueries.setAccessible(true);

        String result = (String) removeSubqueries.invoke(null, "");
        assertEquals("", result);
    }
}
