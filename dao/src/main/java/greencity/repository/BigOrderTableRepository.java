package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.BigOrderTableViews;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    public BigOrderTableRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Method returns page with orders and additional info related to order.
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
        if (nonNull(sc.getOrderStatus())) {
            predicates.add(filterByOrderStatus(sc.getOrderStatus(), orderRoot));
        }
        if (nonNull(sc.getOrderPaymentStatus())) {
            predicates.add(filterByOrderPaymentStatus(sc.getOrderPaymentStatus(), orderRoot));
        }
        if (nonNull(sc.getRegion())) {
            predicates.add(filterByStringValue(sc.getRegion(), "region", orderRoot));
        }
        if (nonNull(sc.getCity())) {
            predicates.add(filterByStringValue(sc.getCity(), "settlement", orderRoot));
        }
        if (nonNull(sc.getDistricts())) {
            predicates.add(filterByStringValue(sc.getDistricts(), "district", orderRoot));
        }
        if (nonNull(sc.getOrderDateTo())) {
            predicates.add(filterByDate(sc.getOrderDateFrom(), sc.getOrderDateTo(), "orderDate", orderRoot));
        }
        if (nonNull(sc.getDeliveryDateTo())) {
            predicates.add(filterByDate(sc.getDeliveryDateFrom(), sc.getDeliveryDateTo(), "dateOfExport", orderRoot));
        }
        if (nonNull(sc.getPaymentDateTo())) {
            predicates.add(filterByDate(sc.getPaymentDateFrom(), sc.getPaymentDateTo(), "paymentDate", orderRoot));
        }
        if (nonNull(sc.getReceivingStation())) {
            predicates.add(filteredByLongValue("receivingStationId", sc.getReceivingStation(), orderRoot));
        }
        if (nonNull(sc.getResponsibleCallerId())) {
            predicates.add(filteredByLongValue("responsibleCallerId", sc.getResponsibleCallerId(), orderRoot));
        }
        if (nonNull(sc.getResponsibleLogicManId())) {
            predicates.add(filteredByLongValue("responsibleLogicManId", sc.getResponsibleLogicManId(), orderRoot));
        }
        if (nonNull(sc.getResponsibleNavigatorId())) {
            predicates.add(filteredByLongValue("responsibleDriverId", sc.getResponsibleNavigatorId(), orderRoot));
        }
        if (nonNull(sc.getResponsibleDriverId())) {
            predicates.add(filteredByLongValue("responsibleNavigatorId", sc.getResponsibleDriverId(), orderRoot));
        }
        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc, orderRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate filterByOrderStatus(OrderStatus[] filter, Root<BigOrderTableViews> orderRoot) {
        var orderStatus = criteriaBuilder.in(orderRoot.get("orderStatus").as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(orderStatus::value);
        return orderStatus;
    }

    private Predicate filterByOrderPaymentStatus(OrderPaymentStatus[] filter, Root<BigOrderTableViews> orderRoot) {
        var orderPaymentStatus = criteriaBuilder.in(orderRoot.get("orderPaymentStatus").as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(orderPaymentStatus::value);
        return orderPaymentStatus;
    }

    private Predicate filterByStringValue(String[] filter, String columnName, Root<BigOrderTableViews> orderRoot) {
        var location = criteriaBuilder.in(criteriaBuilder.upper(orderRoot.get(columnName)));
        Arrays.stream(filter)
            .map(String::toUpperCase)
            .forEach(location::value);
        return location;
    }

    private Predicate filterByDate(String from, String to, String columnName, Root<BigOrderTableViews> orderRoot) {
        var formatter = DateTimeFormatter.ofPattern("yyy-MM-d");
        var settlementDate = orderRoot.<LocalDate>get(columnName);

        if (from != null && !from.isEmpty()) {
            return criteriaBuilder.between(settlementDate.as(LocalDate.class),
                LocalDate.parse(from, formatter),
                LocalDate.parse(to, formatter));
        } else {
            return criteriaBuilder.lessThanOrEqualTo(settlementDate, LocalDate.parse(to, formatter));
        }
    }

    private Predicate filteredByLongValue(String nameColumn, Long[] id, Root<BigOrderTableViews> orderRoot) {
        var predicate = criteriaBuilder.in(orderRoot.<Long>get(nameColumn));
        Arrays.stream(id)
            .forEach(predicate::value);
        return predicate;
    }

    private void searchOnBigTable(OrderSearchCriteria sc, Root<BigOrderTableViews> root, List<Predicate> predicates) {
        var namesFields = BigOrderTableViews.class.getDeclaredFields();
        var searchWords = sc.getSearch().split("[+]");
        var listPredicates = new ArrayList<Predicate>();

        for (String searchWord : searchWords) {
            Arrays.stream(namesFields)
                .map(Field::getName)
                .map(root::get)
                .map(excretion -> criteriaBuilder.upper(excretion.as(String.class)))
                .map(p -> criteriaBuilder.like(p, "%" + searchWord.toUpperCase() + "%"))
                .forEach(listPredicates::add);
        }
        predicates.add(criteriaBuilder.or(listPredicates.toArray(Predicate[]::new)));
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
