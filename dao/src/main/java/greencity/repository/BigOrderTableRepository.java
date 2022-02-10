package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;

import java.util.*;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CustomCriteriaPredicate cp;

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    @Autowired
    public BigOrderTableRepository(EntityManager entityManager, CustomCriteriaPredicate customCriteriaPredicate) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.cp = customCriteriaPredicate;
    }

    /**
     * Method returns Page of BigOrderTableViews with orders and additional info
     * related to order.
     * 
     * @return Page
     * @author Kuzbyt Maksym
     */
    public Page<BigOrderTableViews> findAll(OrderPage orderPage, OrderSearchCriteria searchCriteria) {
        var criteriaQuery = criteriaBuilder.createQuery(BigOrderTableViews.class);
        var orderRoot = criteriaQuery.from(BigOrderTableViews.class);

        var predicate = getPredicate(searchCriteria, orderRoot);

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

    private Predicate getPredicate(OrderSearchCriteria sc, Root<BigOrderTableViews> orderRoot) {
        var predicates = new ArrayList<Predicate>();

        getPredicateByEnumValue(predicates, sc, orderRoot);
        getPredicateByStringValue(predicates, sc, orderRoot);
        getPredicateByDateFilter(predicates, sc, orderRoot);
        getPredicateByLongValue(predicates, sc, orderRoot);

        if (nonNull(sc.getSearch())) {
            predicates.add(cp.search(sc.getSearch(),orderRoot));
        }

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private void getPredicateByEnumValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        var filtersEnum = new HashMap<String, Enum<?>[]>();
        filtersEnum.put("orderStatus", sc.getOrderStatus());
        filtersEnum.put("orderPaymentStatus", sc.getOrderPaymentStatus());

        filtersEnum.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> cp.filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByStringValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        var filtersString = new HashMap<String, String[]>();
        filtersString.put("region", sc.getRegion());
        filtersString.put("settlement", sc.getCity());
        filtersString.put("district", sc.getDistricts());

        filtersString.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> cp.filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByLongValue(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        var filtersLong = new HashMap<String, Long[]>();
        filtersLong.put("receivingStationId", sc.getReceivingStation());
        filtersLong.put("responsibleCallerId", sc.getResponsibleCallerId());
        filtersLong.put("responsibleLogicManId", sc.getResponsibleLogicManId());
        filtersLong.put("responsibleNavigatorId", sc.getResponsibleNavigatorId());
        filtersLong.put("responsibleDriverId", sc.getResponsibleDriverId());

        filtersLong.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> cp.filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);
    }

    private void getPredicateByDateFilter(List<Predicate> predicates, OrderSearchCriteria sc,
        Root<BigOrderTableViews> orderRoot) {
        var dateFilter = new HashMap<String, DateFilter>();
        dateFilter.put("orderDate", sc.getOrderDate());
        dateFilter.put("dateOfExport", sc.getDeliveryDate());
        dateFilter.put("paymentDate", sc.getPaymentDate());

        dateFilter.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> cp.filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);
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
