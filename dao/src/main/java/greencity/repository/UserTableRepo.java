package greencity.repository;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import greencity.filters.UserFilterCriteria;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
public class UserTableRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private static final String ORDERS = "orders";

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */

    public UserTableRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Finds list of User who have made at least one order.
     *
     * @return {@link Page} of {@link User}.
     * @author - Stepan Tehlivets.
     */

    public Page<User> findAll(UserFilterCriteria userFilterCriteria, String column,
        SortingOrder sortingOrder, Pageable page) {
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        userRoot.join(ORDERS, JoinType.INNER);
        criteriaQuery.groupBy(userRoot.get("id"));
        setOrder(column, sortingOrder, criteriaQuery, userRoot);

        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(page.getPageNumber() * 10);
        typedQuery.setMaxResults(10);

        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), "recipientPhone");
        Pageable pageable = PageRequest.of(page.getPageNumber(), 10, sort);

        List<User> resultList = typedQuery.getResultList();

        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    private Predicate getPredicate(UserFilterCriteria us, Root<User> userRoot) {
        List<Predicate> predicateList = new ArrayList<>();

        return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
    }

    private void setOrder(String column, SortingOrder sortingOrder, CriteriaQuery<User> criteriaQuery,
        Root<User> userRoot) {
        Expression<?> sortBy = userRoot.get("recipientName");

        if (nonNull(column)) {
            if (column.equals("orderDate")) {
                sortBy = criteriaBuilder.max(userRoot.getJoins().stream().findFirst().get().get("orderDate"));
            } else if (column.equals("number_of_orders")) {
                sortBy = criteriaBuilder.count(userRoot.getJoins().stream().findFirst().get().get("user"));
            } else if (column.equals("clientName")) {
                sortBy = userRoot.get("recipientName");
            } else {
                sortBy = userRoot.get(column);
            }
        }
        if (sortingOrder.equals(SortingOrder.DESC)) {
            criteriaQuery.orderBy(criteriaBuilder.desc(sortBy));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.asc(sortBy));
        }
    }
}
