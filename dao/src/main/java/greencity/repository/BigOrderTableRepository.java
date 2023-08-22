package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CustomCriteriaPredicate criteriaPredicate;
    private final OrderFilterDataProvider orderFilterDataProvider;

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    @Autowired
    public BigOrderTableRepository(EntityManager entityManager, CustomCriteriaPredicate customCriteriaPredicate) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaPredicate = customCriteriaPredicate;
        this.orderFilterDataProvider = new OrderFilterDataProvider();
    }

    /**
     * Method returns Page of BigOrderTableViews with orders and additional info
     * related to order.
     * 
     * @return Page
     * @author Kuzbyt Maksym
     */
    public Page<BigOrderTableViews> findAll(OrderPage orderPage, OrderSearchCriteria searchCriteria,
        List<Long> tariffsInfoIds) {
        var criteriaQuery = criteriaBuilder.createQuery(BigOrderTableViews.class);
        var orderRoot = criteriaQuery.from(BigOrderTableViews.class);

        var predicate = getPredicate(searchCriteria, orderRoot, tariffsInfoIds);

        criteriaQuery.select(orderRoot).where(predicate);
        sort(orderPage, criteriaQuery, orderRoot);

        var typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(orderPage.getPageNumber() * orderPage.getPageSize());
        typedQuery.setMaxResults(orderPage.getPageSize());

        var resultList = typedQuery.getResultList();

        var sort = Sort.by(orderPage.getSortDirection(), orderPage.getSortBy());
        var pageable = PageRequest.of(orderPage.getPageNumber(), orderPage.getPageSize(), sort);
        var ordersCount = getOrdersCount(predicate);

        return new PageImpl<>(resultList, pageable, ordersCount);
    }

    private Predicate getPredicate(OrderSearchCriteria sc, Root<BigOrderTableViews> orderRoot,
        List<Long> tariffsInfoIds) {
        var predicates = new ArrayList<Predicate>();

        getPredicateByEnumValue(predicates, sc, orderRoot);
        getPredicateByStringValue(predicates, sc, orderRoot);
        getPredicateByDateFilter(predicates, sc, orderRoot);
        getPredicateByLongValue(predicates, sc, orderRoot);
        getPredicateByTariffsInfoId(predicates, tariffsInfoIds, orderRoot);

        if (nonNull(sc.getSearch())) {
            predicates.add(criteriaPredicate.search(sc.getSearch(), orderRoot));
        }

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private void getPredicateByEnumValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        orderFilterDataProvider.getFiltersEnum().entrySet().stream()
            .filter(e -> e.getValue().apply(sc) != null)
            .map(e -> criteriaPredicate.filter(e.getValue().apply(sc), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByStringValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        orderFilterDataProvider.getFiltersString().entrySet().stream()
            .filter(e -> e.getValue().apply(sc) != null)
            .map(e -> criteriaPredicate.filter(e.getValue().apply(sc), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByLongValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        orderFilterDataProvider.getFiltersLong().entrySet().stream()
            .filter(e -> e.getValue().apply(sc) != null)
            .map(e -> criteriaPredicate.filter(e.getValue().apply(sc), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByDateFilter(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        orderFilterDataProvider.getFiltersDateFilter().entrySet().stream()
            .filter(e -> e.getValue().apply(sc) != null)
            .map(e -> criteriaPredicate.filter(e.getValue().apply(sc), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByTariffsInfoId(List<Predicate> predicates, List<Long> tariffsInfoIds,
        Root<BigOrderTableViews> orderRoot) {
        predicates.add(criteriaPredicate.filter(tariffsInfoIds, orderRoot, "tariffsInfoId"));
    }

    private void sort(OrderPage orderPage, CriteriaQuery<BigOrderTableViews> cq, Root<BigOrderTableViews> root) {
        if (orderPage.getSortDirection().equals(Sort.Direction.ASC)) {
            cq.orderBy(criteriaBuilder.asc(root.get(orderPage.getSortBy())));
        } else {
            cq.orderBy(criteriaBuilder.desc(root.get(orderPage.getSortBy())));
        }
    }

    private long getOrdersCount(Predicate predicate) {
        var countQuery = criteriaBuilder.createQuery(Long.class);
        var countOrderRoot = countQuery.from(BigOrderTableViews.class);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
