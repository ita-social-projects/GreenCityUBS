package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.ubs.UBSuser;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private static final String USER = "user";
    private static final String UBS_USER = "ubsUser";
    private static final String ADDRESS = "address";
    private static final String EMPLOYEE_ORDER_POSITION = "employeeOrderPositions";
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
    public Page<Order> findAll(OrderPage orderPage, OrderSearchCriteria searchCriteria) {
        CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);

        Root<Order> orderRoot = criteriaQuery.from(Order.class);
        orderRoot.join("payment", JoinType.LEFT);
        orderRoot.join(USER, JoinType.LEFT);
        orderRoot.join("certificates", JoinType.LEFT);
        Join<Order, UBSuser> ubsUserJoin = orderRoot.join(UBS_USER, JoinType.LEFT);
        ubsUserJoin.join(ADDRESS, JoinType.LEFT);

        Predicate predicate = getPredicate(searchCriteria, orderRoot);
        criteriaQuery.where(predicate);
        setOrder(orderPage, criteriaQuery, orderRoot);

        TypedQuery<Order> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(orderPage.getPageNumber() * orderPage.getPageSize());
        typedQuery.setMaxResults(orderPage.getPageSize());

        Sort sort = Sort.by(orderPage.getSortDirection(), orderPage.getSortBy());
        Pageable pageable =
            PageRequest.of(orderPage.getPageNumber(), orderPage.getPageSize(), sort);

        long ordersCount = getOrdersCount(predicate);

        List<Order> resultList = typedQuery.getResultList();

        return new PageImpl<>(resultList, pageable, ordersCount);
    }

    private long getOrdersCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Order> countOrderRoot = countQuery.from(Order.class);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate getPredicate(OrderSearchCriteria sc, Root<Order> orderRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (nonNull(sc.getOrderStatus())) {
            CriteriaBuilder.In<OrderStatus> orderStatus = criteriaBuilder.in(orderRoot.get("orderStatus"));
            Arrays.stream(sc.getOrderStatus())
                .forEach(orderStatus::value);
            predicates.add(orderStatus);
        }
        if (nonNull(sc.getOrderPaymentStatus())) {
            CriteriaBuilder.In<OrderPaymentStatus> orderPaymentStatus =
                criteriaBuilder.in(orderRoot.get("orderPaymentStatus"));
            Arrays.stream(sc.getOrderPaymentStatus())
                .forEach(orderPaymentStatus::value);
            predicates.add(orderPaymentStatus);
        }
        if (nonNull(sc.getReceivingStation())) {
            CriteriaBuilder.In<String> receivingStation = criteriaBuilder.in(
                criteriaBuilder.upper(orderRoot.get("receivingStation")));
            Arrays.stream(sc.getReceivingStation())
                .map(String::toUpperCase)
                .forEach(receivingStation::value);
            predicates.add(receivingStation);
        }
        if (nonNull(sc.getDistricts())) {
            CriteriaBuilder.In<String> district =
                criteriaBuilder.in(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("district")));
            Arrays.stream(sc.getDistricts())
                .map(String::toUpperCase)
                .forEach(district::value);
            predicates.add(district);
        }
        if (nonNull(sc.getDateFrom()) && nonNull(sc.getDateTo())) {
            predicates.add(criteriaBuilder.between(orderRoot.get("orderDate"),
                LocalDateTime.of(LocalDate.parse(sc.getDateFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(sc.getDateTo()), LocalTime.MAX)));
        }
        if (nonNull(sc.getResponsibleCallerFirstName()) && nonNull(sc.getResponsibleCallerLastName())) {
            CriteriaBuilder.In<String> responsibleCallerLastName =
                criteriaBuilder
                    .in(criteriaBuilder.upper(orderRoot.get(EMPLOYEE_ORDER_POSITION).get("employee").get("lastName")));
            Arrays.stream(sc.getResponsibleCallerLastName())
                .map(String::toUpperCase)
                .forEach(responsibleCallerLastName::value);
            CriteriaBuilder.In<String> responsibleCallerFirstName =
                criteriaBuilder
                    .in(criteriaBuilder.upper(orderRoot.get(EMPLOYEE_ORDER_POSITION).get("employee").get("firstName")));
            Arrays.stream(sc.getResponsibleCallerFirstName())
                .map(String::toUpperCase)
                .forEach(responsibleCallerFirstName::value);
            Predicate position =
                criteriaBuilder.equal(orderRoot.get(EMPLOYEE_ORDER_POSITION).get("position").get("id"), 1L);
            Predicate finale = criteriaBuilder.and(responsibleCallerFirstName, responsibleCallerLastName, position);
            predicates.add(finale);
        }
        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc, orderRoot, predicates);
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void searchOnBigTable(OrderSearchCriteria sc, Root<Order> orderRoot, List<Predicate> predicates) {
        Predicate orderPredicate = formOrderLikePredicate(sc, orderRoot);
        Predicate userPredicate = formUserLikePredicate(sc, orderRoot);
        Predicate ubsUserPredicate = formUbsUserLikePredicate(sc, orderRoot);
        Predicate addressPredicate = formAddressLikePredicate(sc, orderRoot);

        Predicate predicate = criteriaBuilder.or(orderPredicate, userPredicate, ubsUserPredicate, addressPredicate);
        predicates.add(predicate);
    }

    private void setOrder(OrderPage orderPage, CriteriaQuery<Order> criteriaQuery, Root<Order> orderRoot) {
        Path<Object> sortBy = orderRoot.get("id");
        String[] split = orderPage.getSortBy().split("\\.");

        if (split.length == 1) {
            sortBy = orderRoot.get(split[0]);
        } else if (split.length == 2) {
            sortBy = orderRoot.get(split[0]).get(split[1]);
        } else if (split.length == 3) {
            sortBy = orderRoot.get(split[0]).get(split[1]).get(split[2]);
        }

        if (orderPage.getSortDirection().equals(Sort.Direction.ASC)) {
            criteriaQuery.orderBy(criteriaBuilder.asc(sortBy));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.desc(sortBy));
        }
    }

    private Predicate formOrderLikePredicate(OrderSearchCriteria sc, Root<Order> orderRoot) {
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get("comment")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get("note")),
                "%" + sc.getSearch().toUpperCase() + "%")
        );

    }

    private Predicate formUbsUserLikePredicate(OrderSearchCriteria sc, Root<Order> orderRoot) {
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get("firstName")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get("lastName")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get("phoneNumber")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get("email")),
                "%" + sc.getSearch().toUpperCase() + "%"));
    }

    private Predicate formUserLikePredicate(OrderSearchCriteria sc, Root<Order> orderRoot) {
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(USER).get("recipientName")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(USER).get("recipientSurname")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(USER).get("recipientPhone")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(USER).get("recipientEmail")),
                "%" + sc.getSearch().toUpperCase() + "%"));
    }

    private Predicate formAddressLikePredicate(OrderSearchCriteria sc, Root<Order> orderRoot) {
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("street")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("houseNumber")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("houseCorpus")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("entranceNumber")),
                "%" + sc.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(orderRoot.get(UBS_USER).get(ADDRESS).get("addressComment")),
                "%" + sc.getSearch().toUpperCase() + "%"));
    }


}
