package greencity.repository;

import greencity.enums.SortingOrder;
import greencity.entity.user.Violation;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Expression;
import java.util.List;
import static java.util.Objects.nonNull;

@Repository
public class UserViolationsTableRepo {
    private final CriteriaBuilder criteriaBuilder;
    private final EntityManager entityManager;

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    public UserViolationsTableRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Gets a page of violations with sorting by user id.
     *
     * @return {@link Page} of {@link Violation}.
     * @author - Max Bohonko.
     */
    public Page<Violation> findAll(Long userId, String column, SortingOrder sortingOrder, Pageable page) {
        CriteriaQuery<Violation> violationCriteriaQuery = criteriaBuilder.createQuery(Violation.class);
        Root<Violation> violationRoot = violationCriteriaQuery.from(Violation.class);
        Join<Violation, Order> orderJoin = violationRoot.join("order", JoinType.INNER);

        violationCriteriaQuery.select(violationRoot);
        violationCriteriaQuery.where(criteriaBuilder.equal(orderJoin.get("user").get("id"), userId));

        setOrder(column, sortingOrder, violationCriteriaQuery, violationRoot);

        TypedQuery<Violation> typedQuery = entityManager.createQuery(violationCriteriaQuery);
        long count = typedQuery.getResultList().size();
        typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        typedQuery.setMaxResults(page.getPageSize());

        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), column);
        Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        List<Violation> resultList = typedQuery.getResultList();

        return new PageImpl<>(resultList, pageable, count);
    }

    private void setOrder(String column, SortingOrder sortingOrder,
        CriteriaQuery<Violation> criteriaQuery,
        Root<Violation> violationRoot) {
        Expression<?> sortBy = violationRoot.get("violationDate");

        if (nonNull(column)) {
            switch (column) {
                case "orderId":
                    sortBy = violationRoot.get("order").get("id");
                    break;
                case "violationLevel":
                    sortBy = violationRoot.get("violationLevel");
                    break;
                default:
                    sortBy = violationRoot.get("violationDate");
            }
        }
        if (sortingOrder.equals(SortingOrder.DESC)) {
            criteriaQuery.orderBy(criteriaBuilder.desc(sortBy));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.asc(sortBy));
        }
    }
}
