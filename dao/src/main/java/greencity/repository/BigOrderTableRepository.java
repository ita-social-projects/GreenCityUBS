package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.enums.OrderStatusSortingTranslation;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import java.util.*;
import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CustomCriteriaPredicate criteriaPredicate;
    private final OrderFilterDataProvider orderFilterDataProvider;
    private static final String ORDER_STATUS = "orderStatus";

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
        List<Long> tariffsInfoIds, String userLanguage) {
        var criteriaQuery = criteriaBuilder.createQuery(BigOrderTableViews.class);
        var orderRoot = criteriaQuery.from(BigOrderTableViews.class);

        var predicate = getPredicate(searchCriteria, orderRoot, tariffsInfoIds);

        criteriaQuery.select(orderRoot).where(predicate);
        sort(orderPage, criteriaQuery, orderRoot, userLanguage);

        var typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(orderPage.getPageNumber() * orderPage.getPageSize());
        typedQuery.setMaxResults(orderPage.getPageSize());

        var resultList = typedQuery.getResultList();

        var sort = Sort.by(orderPage.getSortDirection(), orderPage.getSortBy());
        var pageable = PageRequest.of(orderPage.getPageNumber(), orderPage.getPageSize(), sort);
        var ordersCount = getOrdersCount(searchCriteria, tariffsInfoIds);

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
            .map(e -> {
                Long[] values = e.getValue().apply(sc);
                Predicate predicate;
                if (Arrays.stream(values).anyMatch(value -> value != null && value != -1L)) {
                    predicate = criteriaPredicate.filter(values, orderRoot, e.getKey());
                } else {
                    predicate = criteriaPredicate.filter(orderRoot, e.getKey());
                }
                return predicate;
            })
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

    private void sort(OrderPage orderPage, CriteriaQuery<BigOrderTableViews> cq, Root<BigOrderTableViews> root,
        String userLanguage) {
        if (userLanguage.equals("ua")
            && (orderPage.getSortBy().equals(ORDER_STATUS) || orderPage.getSortBy().equals("orderPaymentStatus"))) {
            sortingForUkrainianLocalization(orderPage, cq, root);
        } else {
            defaultSorting(orderPage, cq, root, Optional.empty());
        }
    }

    private long getOrdersCount(OrderSearchCriteria searchCriteria, List<Long> tariffsInfoIds) {
        var countQuery = criteriaBuilder.createQuery(Long.class);
        var countOrderRoot = countQuery.from(BigOrderTableViews.class);
        var countPredicate = getPredicate(searchCriteria, countOrderRoot, tariffsInfoIds);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(countPredicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void defaultSorting(OrderPage orderPage, CriteriaQuery<BigOrderTableViews> cq,
        Root<BigOrderTableViews> root, Optional<Expression<Integer>> sortingOrderUkrainianLocalization) {
        Expression<?> expression = sortingOrderUkrainianLocalization.orElseGet(() -> root.get(orderPage.getSortBy()));
        if (orderPage.getSortDirection().equals(Sort.Direction.ASC)) {
            cq.orderBy(criteriaBuilder.asc(expression));
        } else {
            cq.orderBy(criteriaBuilder.desc(expression));
        }
    }

    private void sortingForUkrainianLocalization(OrderPage orderPage, CriteriaQuery<BigOrderTableViews> cq,
        Root<BigOrderTableViews> root) {
        if (ORDER_STATUS.equals(orderPage.getSortBy())) {
            EnumMap<OrderStatusSortingTranslation, Integer> sortOrderMap =
                OrderStatusSortingTranslation.getOrderMapSortedByAsc();
            CriteriaBuilder.Case<Integer> selectCase = criteriaBuilder.selectCase();
            Expression<Integer> otherwiseExpression =
                criteriaBuilder.literal(OrderStatusSortingTranslation.OTHER.getSortOrder());
            for (Map.Entry<OrderStatusSortingTranslation, Integer> entry : sortOrderMap.entrySet()) {
                selectCase = selectCase.when(
                    criteriaBuilder.equal(root.get(ORDER_STATUS), entry.getKey().name()),
                    entry.getValue());
            }
            Expression<Integer> sortOrder = selectCase.otherwise(otherwiseExpression);
            defaultSorting(orderPage, cq, root, Optional.of(sortOrder));
        } else {
            defaultSorting(orderPage, cq, root, Optional.empty());
        }
    }
}