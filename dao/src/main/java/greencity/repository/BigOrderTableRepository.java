package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
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

        sort(orderPage, criteriaQuery, orderRoot);
        criteriaQuery.select(orderRoot).where(predicate);

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
        if (nonNull(sc.getOrderDate())) {
            predicates.add(filterByLocalDateValue(sc.getOrderDate(), orderRoot, "orderDate"));
        }
        if (nonNull(sc.getDeliveryDate())) {
            predicates.add(
                filterByLocalDateValue(sc.getDeliveryDate(), orderRoot, "dateOfExport"));
        }
        if (nonNull(sc.getPaymentDate())) {
            predicates.add(filterByLocalDateValue(sc.getPaymentDate(), orderRoot, "paymentDate"));
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
            predicates.add(filteredByLongValue(sc.getResponsibleNavigatorId(), orderRoot, "responsibleNavigatorId"));
        }
        if (nonNull(sc.getResponsibleDriverId())) {
            predicates.add(filteredByLongValue(sc.getResponsibleDriverId(), orderRoot, "responsibleDriverId"));
        }
        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc.getSearch(), orderRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate filterByEnum(Enum<?>[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(root.get(columnName).as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(predicate::value);
        return predicate;
    }

    private Predicate filterByStringValue(String[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(criteriaBuilder.upper(root.get(columnName)));
        Arrays.stream(filter)
            .map(String::toUpperCase)
            .forEach(predicate::value);
        return predicate;
    }

    private Predicate filterByLocalDateValue(@NotNull DateFilter df, Root<?> root, String columnName) {
        var column = root.<LocalDate>get(columnName).as(LocalDate.class);
        var to = df.getTo();
        var from = df.getFrom();
        var format = DateTimeFormatter.ofPattern("yyy-MM-d");
        if (from == null && to != null) {
            return criteriaBuilder.lessThanOrEqualTo(column, LocalDate.parse(to, format));
        }
        if (from != null && to == null) {
            return criteriaBuilder.greaterThanOrEqualTo(column, LocalDate.parse(from, format));
        }
        return criteriaBuilder.between(column, LocalDate.parse(Objects.requireNonNull(from), format),
            LocalDate.parse(Objects.requireNonNull(to), format));
    }

    private Predicate filteredByLongValue(Long[] id, Root<?> root, String nameColumn) {
        var predicate = criteriaBuilder.in(root.<Long>get(nameColumn));
        Arrays.stream(id)
            .forEach(predicate::value);
        return predicate;
    }

    private void searchOnBigTable(String[] searchWords, Root<?> root, List<Predicate> predicates) {
        var fields = BigOrderTableViews.class.getDeclaredFields();
        var listPredicates = new ArrayList<Predicate>();

        for (String searchWord : searchWords) {
            Arrays.stream(fields)
                .map(Field::getName)
                .map(root::get)
                .map(path -> criteriaBuilder.upper(path.as(String.class)))
                .map(expression -> criteriaBuilder.like(expression, "%" + searchWord.toUpperCase() + "%"))
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
