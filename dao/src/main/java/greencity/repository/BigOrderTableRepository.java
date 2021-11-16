package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.ubs.UBSuser;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.hibernate.criterion.SubqueryExpression;
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
import java.util.Map;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private static final String USER = "user";
    private static final String UBS_USER = "ubsUser";
    private static final String ADDRESS = "address";
    private static final String EMPLOYEE_ORDER_POSITION = "employeeOrderPositions";
    private static final String PAYMENT = "payment";
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
        String[] searchWord = sc.getSearch().split(" ");
        for (String s : searchWord) {
            predicates.add(criteriaBuilder.or(
                formOrderLikePredicate(s, orderRoot),
                formUserLikePredicate(s, orderRoot),
                formUbsUserLikePredicate(s, orderRoot),
                formAddressLikePredicate(s, orderRoot)));
        }
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

    private Predicate formOrderLikePredicate(String s, Root<Order> orderRoot) {
        Expression<String> id = orderRoot.get("id").as(String.class);
        Expression<String> orderDate = orderRoot.get("orderDate").as(String.class);
        Expression<String> note = criteriaBuilder.upper(orderRoot.get("note"));
        Expression<String> comment = criteriaBuilder.upper(orderRoot.get("comment"));
        return criteriaBuilder.or(
            criteriaBuilder.like(id, "%" + s + "%"),
            criteriaBuilder.like(orderDate, "%" + s + "%"),
            criteriaBuilder.like(note, "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(comment, "%" + s.toUpperCase() + "%"));
    }

    private Predicate formUbsUserLikePredicate(String s, Root<Order> orderRoot) {
        Expression<String> firstName = orderRoot.get(UBS_USER).get("firstName");
        Expression<String> lastName = orderRoot.get(UBS_USER).get("lastName");
        Expression<String> phoneNumber = orderRoot.get(UBS_USER).get("phoneNumber");
        Expression<String> email = orderRoot.get(UBS_USER).get("email");
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(firstName), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(lastName), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(phoneNumber), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(email), "%" + s.toUpperCase() + "%"));
    }

    private Predicate formUserLikePredicate(String s, Root<Order> orderRoot) {
        Expression<String> firstName = orderRoot.get(USER).get("recipientName");
        Expression<String> lastName = orderRoot.get(USER).get("recipientSurname");
        Expression<String> recipientPhone = orderRoot.get(USER).get("recipientPhone");
        Expression<String> recipientEmail = orderRoot.get(USER).get("recipientEmail");
        Expression<String> violations = orderRoot.get(USER).get("violations").as(String.class);
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(firstName), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(lastName), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(recipientPhone), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(recipientEmail), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(violations, "%" + s + "%"));
    }

    private Predicate formAddressLikePredicate(String s, Root<Order> orderRoot) {
        Expression<String> street = orderRoot.get(UBS_USER).get(ADDRESS).get("street");
        Expression<String> houseNumber = orderRoot.get(UBS_USER).get(ADDRESS).get("houseNumber");
        Expression<String> houseCorpus = orderRoot.get(UBS_USER).get(ADDRESS).get("houseCorpus");
        Expression<String> entranceNumber = orderRoot.get(UBS_USER).get(ADDRESS).get("entranceNumber");
        Expression<String> addressComment = orderRoot.get(UBS_USER).get(ADDRESS).get("addressComment");
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.upper(street), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(houseNumber), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(houseCorpus), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(entranceNumber), "%" + s.toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(addressComment), "%" + s.toUpperCase() + "%"));
    }
}
