package greencity.service.utility;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityManagerUtils {
    @PersistenceContext
    private EntityManager entityManager;

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

    protected <T> TypedQuery<T> createTypedQueryWithFetchGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes) {
        TypedQuery<T> query = entityManager.createQuery(jpqlQueryString, entityClass);
        query.setHint("jakarta.persistence.fetchgraph", createEntityGraph(entityClass, attributes));
        return query;
    }

    protected <T> TypedQuery<T> createTypedQueryWithLoadGraph(
        Class<T> entityClass, String jpqlQueryString, List<String> attributes) {
        TypedQuery<T> query = entityManager.createQuery(jpqlQueryString, entityClass);
        query.setHint("jakarta.persistence.loadgraph", createEntityGraph(entityClass, attributes));
        return query;
    }
}
