package greencity.repository;

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
            predicates.add(filterByEnum(sc.getOrderStatus(), orderRoot, "orderStatus"));
        }
        if (nonNull(sc.getOrderPaymentStatus())) {
            predicates.add(filterByEnum(sc.getOrderPaymentStatus(), orderRoot, "orderPaymentStatus"));
        }
        if (nonNull(sc.getRegion())) {
            predicates.add(filterByStringValue(sc.getRegion(), orderRoot, "region"));
        }
        if (nonNull(sc.getCity())) {
            predicates.add(filterByStringValue(sc.getCity(), orderRoot, "settlement"));
        }
        if (nonNull(sc.getDistricts())) {
            predicates.add(filterByStringValue(sc.getDistricts(), orderRoot, "district"));
        }
        if (nonNull(sc.getOrderDateTo())) {
            predicates.add(filterByLocalDateValue(sc.getOrderDateFrom(), sc.getOrderDateTo(), orderRoot, "orderDate"));
        }
        if (nonNull(sc.getDeliveryDateTo())) {
            predicates.add(
                filterByLocalDateValue(sc.getDeliveryDateFrom(), sc.getDeliveryDateTo(), orderRoot, "dateOfExport"));
        }
        if (nonNull(sc.getPaymentDateTo())) {
            predicates
                .add(filterByLocalDateValue(sc.getPaymentDateFrom(), sc.getPaymentDateTo(), orderRoot, "paymentDate"));
        }
        if (nonNull(sc.getReceivingStation())) {
            predicates.add(filteredByLongValue(sc.getReceivingStation(), orderRoot, "receivingStationId"));
        }
        if (nonNull(sc.getResponsibleCallerId())) {
            predicates.add(filteredByLongValue(sc.getResponsibleCallerId(), orderRoot, "responsibleCallerId"));
        }
        if (nonNull(sc.getResponsibleLogicManId())) {
            predicates.add(filteredByLongValue(sc.getResponsibleLogicManId(), orderRoot, "responsibleLogicManId"));
        }
        if (nonNull(sc.getResponsibleNavigatorId())) {
            predicates.add(filteredByLongValue(sc.getResponsibleNavigatorId(), orderRoot, "responsibleDriverId"));
        }
        if (nonNull(sc.getResponsibleDriverId())) {
            predicates.add(filteredByLongValue(sc.getResponsibleDriverId(), orderRoot, "responsibleNavigatorId"));
        }
        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc, orderRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate filterByEnum(Enum<?>[] filter, Root<BigOrderTableViews> orderRoot, String columnName) {
        var orderStatus = criteriaBuilder.in(orderRoot.get(columnName).as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(orderStatus::value);
        return orderStatus;
    }

    private Predicate filterByStringValue(String[] filter, Root<BigOrderTableViews> orderRoot, String columnName) {
        var location = criteriaBuilder.in(criteriaBuilder.upper(orderRoot.get(columnName)));
        Arrays.stream(filter)
            .map(String::toUpperCase)
            .forEach(location::value);
        return location;
    }

    private Predicate filterByLocalDateValue(String from, String to, Root<BigOrderTableViews> orderRoot,
        String columnName) {
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

    private Predicate filteredByLongValue(Long[] id, Root<BigOrderTableViews> orderRoot, String nameColumn) {
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
