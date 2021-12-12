package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
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

        orderRoot.join(USER, JoinType.LEFT);
        Join<Order, UBSuser> ubsUserJoin = orderRoot.join(UBS_USER, JoinType.LEFT);
        ubsUserJoin.join(ADDRESS, JoinType.LEFT);
        Predicate predicate = getPredicate(searchCriteria, orderRoot, criteriaQuery);

        criteriaQuery.select(orderRoot).distinct(true);
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

    private Predicate  getPredicate(OrderSearchCriteria sc, Root<Order> orderRoot, CriteriaQuery<Order> cq) {
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
        if (nonNull(sc.getOrderDateFrom()) && nonNull(sc.getOrderDateTo())) {
            predicates.add(criteriaBuilder.between(orderRoot.get("orderDate"),
                    LocalDateTime.of(LocalDate.parse(sc.getOrderDateFrom()), LocalTime.MIN),
                    LocalDateTime.of(LocalDate.parse(sc.getOrderDateTo()), LocalTime.MAX)));
        }
        if (nonNull(sc.getDeliverFromFrom()) && nonNull(sc.getDeliverFromTo())) {
            predicates.add(criteriaBuilder.between(orderRoot.get("deliverFrom"),
                    LocalDateTime.of(LocalDate.parse(sc.getDeliverFromFrom()), LocalTime.MIN),
                    LocalDateTime.of(LocalDate.parse(sc.getDeliverFromTo()), LocalTime.MAX)));
        }
        if (nonNull(sc.getDeliverToFrom()) && nonNull(sc.getDeliverToTo())) {
            predicates.add(criteriaBuilder.between(orderRoot.get("deliverTo"),
                    LocalDateTime.of(LocalDate.parse(sc.getDeliverToFrom()), LocalTime.MIN),
                    LocalDateTime.of(LocalDate.parse(sc.getDeliverToTo()), LocalTime.MAX)));
        }
        if (nonNull(sc.getPaymentDateFrom()) && nonNull(sc.getPaymentDateTo())) {
            Subquery<Payment> subQueryPayment = cq.subquery(Payment.class);
            Root<Order> paymentRoot = subQueryPayment.correlate(orderRoot);
            ListJoin<Order,Payment> join = paymentRoot.joinList("payment",JoinType.LEFT);
            subQueryPayment.select(join).where(
                    criteriaBuilder.between(
                            join.get("settlementDate").as(String.class),
                            sc.getPaymentDateFrom(),
                            sc.getPaymentDateTo()
                    )
            );
            predicates.add(criteriaBuilder.exists(subQueryPayment));
        }
        if (nonNull(sc.getResponsibleCallerId())) {
          predicates.add(filteredByEmployeeOrderPosition(1L, sc.getResponsibleCallerId(),orderRoot,cq));
        }
        if (nonNull(sc.getResponsibleLogiestManId())) {
          predicates.add(filteredByEmployeeOrderPosition(3L, sc.getResponsibleLogiestManId(),orderRoot,cq));
        }
        if (nonNull(sc.getResponsibleNavigatorId())) {
            predicates.add(filteredByEmployeeOrderPosition(4L, sc.getResponsibleNavigatorId(),orderRoot,cq));
        }
        if (nonNull(sc.getResponsibleDriverId())) {
            predicates.add(filteredByEmployeeOrderPosition(5L, sc.getResponsibleDriverId(),orderRoot,cq));
        }
        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc, orderRoot, predicates, cq);
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate filteredByEmployeeOrderPosition(Long idPosition, Long[] idEmployee,Root<Order> orderRoot,CriteriaQuery<Order> cq) {
        Subquery<EmployeeOrderPosition> subQueryOEP = cq.subquery(EmployeeOrderPosition.class);
        Root<Order> oepRoot = subQueryOEP.correlate(orderRoot);
        SetJoin<Order,EmployeeOrderPosition> join = oepRoot.joinSet("employeeOrderPositions",JoinType.LEFT);
        join.join("employee",JoinType.LEFT);
        join.join("position",JoinType.LEFT);

        CriteriaBuilder.In<Long> responsibleEmployeeId = criteriaBuilder.in(join.get("employee").get("id"));
        Arrays.stream(idEmployee)
                .forEach(responsibleEmployeeId::value);

        subQueryOEP.select(join).where(criteriaBuilder.and(
                criteriaBuilder.equal(join.get("position").get("id"), idPosition),
                responsibleEmployeeId));
        return criteriaBuilder.exists(subQueryOEP);

    }

    private void searchOnBigTable(OrderSearchCriteria sc, Root<Order> orderRoot, List<Predicate> predicates, CriteriaQuery<Order> cq) {
        String[] searchWord = sc.getSearch().split(" ");
        for (String s : searchWord) {
            predicates.add(criteriaBuilder.or(
                    formOrderLikePredicate(s, orderRoot),
                    formUserLikePredicate(s, orderRoot),
                    formUbsUserLikePredicate(s, orderRoot),
                    formAddressLikePredicate(s, orderRoot),
                    formPaymentLikePredicate(s, cq, orderRoot)
            ));
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

    private Predicate formPaymentLikePredicate(String s, CriteriaQuery<Order> cq, Root<Order> orderRoot) {
        Subquery<Integer> subQueryPayment = cq.subquery(Integer.class);
        Root<Order> paymentRoot = subQueryPayment.correlate(orderRoot);
        Join<Order, Payment> paymentJoin = paymentRoot.join("payment");
        subQueryPayment.select(criteriaBuilder.sum(paymentJoin.get("amount")));
        return criteriaBuilder.or(
                criteriaBuilder.like(subQueryPayment.as(String.class), "%" + s + "00" + "%")
        );
    }

}
