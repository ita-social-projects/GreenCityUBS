package greencity.persistence;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.lang.Nullable;
import java.util.List;

public interface JpqlQueryHelper {
    /**
     * Method creates EntityGraph which can be then set as fetchgraph or loadgraph
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
    <T> EntityGraph<T> createEntityGraph(Class<T> entityClass, List<String> attributes);

    /**
     * Method creates TypedQuery for given jpql query string with EntityGraph,
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
    <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes);

    /**
     * Method creates TypedQuery for given jpql query string with EntityGraph, where
     * type of hint can be chosen. This method is well-suited and proves
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
    <T> TypedQuery<T> createTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, EntityGraphType entityGraphType);

    /**
     * Method creates TypedQuery for given jpql query string with EntityGraph,
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
    <T> Page<T> createAndRunPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes, Pageable pageable);

    /**
     * Method runs TypedQuery, creates and runs count query to populate all
     * {@link PageImpl} fields. Useful when query has parameters that need to be set
     * manually before running.
     *
     * @param query    query to be run;
     * @param pageable resulting page parameters;
     * @return {@link Page} query result as a page.
     * @author Oleksandr Ilnytskyi
     */
    <T> Page<T> runPageableTypedQueryWithEntityGraph(TypedQuery<T> query, Pageable pageable);

    /**
     * Method creates TypedQuery for given jpql query string with EntityGraph,
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
    <T> TypedQuery<T> createPageableTypedQueryWithEntityGraph(
        Class<T> entityClass, String jpqlQueryString,
        List<String> attributes, Pageable pageable);

    /**
     * Method creates and runs count query based on query created with
     * jpqlQueryString, removing elements that are not allowed in count queries.
     *
     * @param query original query from which parameters for count query will be
     *              parsed;
     * @return {@link Long} total amount of elements as query result.
     * @author Oleksandr Ilnytskyi
     */
    <T> Long createAndRunCountQueryFor(TypedQuery<T> query);

    /**
     * Method creates count query string for jpqlQueryString, if that query string
     * has already been created, then it's retrieved from cache.
     *
     * @param jpqlQueryString original query string for which countQueryString will
     *                        be generated;
     * @return {@link String} count query string with all disallowed elements
     *         removed or replaced.
     * @author Oleksandr Ilnytskyi
     */
    String createCountQueryStringFor(String jpqlQueryString);

    /**
     * Method creates count query string for jpqlQueryString and applies count
     * projection, if that query string has already been created, then it's
     * retrieved from cache.
     *
     * @param jpqlQueryString original query string for which countQueryString will
     *                        be generated;
     * @param countProjection count projection to be applied;
     * @return {@link String} count query string with all disallowed elements
     *         removed or replaced.
     * @author Oleksandr Ilnytskyi
     */
    String createCountQueryStringFor(String jpqlQueryString, @Nullable String countProjection);
}
