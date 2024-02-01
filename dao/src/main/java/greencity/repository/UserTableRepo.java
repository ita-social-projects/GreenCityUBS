package greencity.repository;

import greencity.enums.SortingOrder;
import greencity.entity.user.User;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static java.util.Objects.nonNull;

@Repository
public class UserTableRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private static final String ORDERS = "orders";
    private static final String ORDER_DATE = "orderDate";
    private static final String DATE_OF_REGISTRATION = "dateOfRegistration";
    private static final String RECIPIENT_NAME = "recipientName";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String RECIPIENT_PHONE = "recipientPhone";
    private static final String POINTS = "currentPoints";
    private static final String VIOLATIONS = "violations";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */

    public UserTableRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Finds list of User who have made at least one order.
     *
     * @return {@link Page} of {@link User}.
     * @author - Stepan Tehlivets.
     */

    public Page<User> findAll(UserFilterCriteria userFilterCriteria, String column,
        SortingOrder sortingOrder, CustomerPage page, List<Long> usId) {
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        userRoot.join(ORDERS, JoinType.INNER);
        criteriaQuery.groupBy(userRoot.get("id"));
        setOrder(column, sortingOrder, criteriaQuery, userRoot);
        if (userFilterCriteria != null) {
            Predicate predicateWhere = getPredicateForWhereAnd(userFilterCriteria, userRoot);
            Predicate predicateWhere2 = getPredicateForWhereOr(userRoot, usId);
            Predicate predicateHaving = getPredicateForHaving(userFilterCriteria, userRoot);
            criteriaQuery.where(predicateWhere, predicateWhere2);
            criteriaQuery.having(predicateHaving);
        }

        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        long usersCount = typedQuery.getResultList().size();
        typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        typedQuery.setMaxResults(page.getPageSize());

        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), RECIPIENT_PHONE);
        Pageable pageable = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);

        List<User> resultList = typedQuery.getResultList();

        return new PageImpl<>(resultList, pageable, usersCount);
    }

    private Predicate getPredicateForWhereAnd(UserFilterCriteria us, Root<User> userRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        if (nonNull(us.getUserRegistrationDate())) {
            predicateList.add(userRegistrationDateFiltering(us.getUserRegistrationDate(), userRoot));
        }
        if (nonNull(us.getNumberOfViolations())) {
            predicateList.add(userViolationsFiltering(us.getNumberOfViolations(), userRoot));
        }
        if (nonNull(us.getNumberOfBonuses())) {
            predicateList.add(userBonusesFiltering(us.getNumberOfBonuses(), userRoot));
        }
        return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
    }

    private Predicate getPredicateForWhereOr(Root<User> userRoot, List<Long> usId) {
        List<Predicate> predicateList = new ArrayList<>(userTariffsInfoFiltering(usId, userRoot));
        return criteriaBuilder.or(predicateList.toArray(new Predicate[0]));
    }

    private void searchUsers(UserFilterCriteria us, Root<User> userRoot, List<Predicate> predicateList) {
        Optional<Join<User, ?>> orderJoin = userRoot.getJoins().stream().findFirst();
        if (orderJoin.isPresent()) {
            Expression<Long> expression = criteriaBuilder.count(orderJoin.get().get("user"));
            Predicate predicate = criteriaBuilder.or(
                criteriaBuilder.like((userRoot.get(DATE_OF_REGISTRATION)).as(String.class),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like(
                    criteriaBuilder.max(orderJoin.get().get(ORDER_DATE)).as(String.class),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like((userRoot.get(VIOLATIONS).as(String.class)),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like(criteriaBuilder.upper(userRoot.get(RECIPIENT_NAME)),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like(criteriaBuilder.upper(userRoot.get(RECIPIENT_EMAIL)),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like((userRoot.get(RECIPIENT_PHONE)),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like(expression.as(String.class),
                    anyMatch(us.getSearch())),
                criteriaBuilder.like((userRoot.get(POINTS).as(String.class)),
                    anyMatch(us.getSearch())));
            predicateList.add(predicate);
        }
    }

    private Predicate getPredicateForHaving(UserFilterCriteria us, Root<User> userRoot) {
        List<Predicate> predicateList = new ArrayList<>();

        if (nonNull(us.getOrderDate())) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_FORMAT);
            Optional<Join<User, ?>> orderJoin = userRoot.getJoins().stream().findFirst();
            if (us.getOrderDate().length == 1) {
                LocalDate number = LocalDate.parse(us.getOrderDate()[0], df);
                orderJoin.ifPresent(userJoin -> predicateList.add(criteriaBuilder.greaterThanOrEqualTo(
                    criteriaBuilder.max(userJoin.get(ORDER_DATE)).as(LocalDate.class),
                    number)));
            } else if (us.getOrderDate()[0].equals("0")) {
                LocalDate number = LocalDate.parse(us.getOrderDate()[1], df);
                orderJoin.ifPresent(userJoin -> predicateList.add(criteriaBuilder
                    .lessThanOrEqualTo(criteriaBuilder.max(userJoin.get(ORDER_DATE)).as(LocalDate.class), number)));
            } else {
                LocalDate number1 = LocalDate.parse(us.getOrderDate()[0], df);
                LocalDate number2 = LocalDate.parse(us.getOrderDate()[1], df);
                if (orderJoin.isPresent()) {
                    orderJoin.ifPresent(userJoin -> predicateList.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.max(userJoin.get(ORDER_DATE)).as(LocalDate.class),
                        number1)));
                    orderJoin.ifPresent(userJoin -> predicateList.add(criteriaBuilder.lessThanOrEqualTo(
                        criteriaBuilder.max(userJoin.get(ORDER_DATE)).as(LocalDate.class), number2)));
                }
            }
        }
        if (nonNull(us.getNumberOfOrders())) {
            predicateList.add(userOrdersFiltering(us.getNumberOfOrders(), userRoot));
        }

        if (nonNull(us.getSearch())) {
            searchUsers(us, userRoot, predicateList);
        }

        return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
    }

    private void setOrder(String column, SortingOrder sortingOrder, CriteriaQuery<User> criteriaQuery,
        Root<User> userRoot) {
        Expression<?> sortBy = userRoot.get(RECIPIENT_NAME);
        Optional<Join<User, ?>> first = userRoot.getJoins().stream().findFirst();
        if (nonNull(column)) {
            switch (column) {
                case ORDER_DATE:
                    if (first.isPresent()) {
                        sortBy = criteriaBuilder.max(first.get().get(ORDER_DATE));
                    }
                    break;
                case "number_of_orders":
                    if (first.isPresent()) {
                        sortBy = criteriaBuilder.count(first.get().get("user"));
                    }
                    break;
                case "clientName":
                    sortBy = userRoot.get(RECIPIENT_NAME);
                    break;
                default:
                    sortBy = userRoot.get(column);
                    break;
            }
        }
        if (sortingOrder.equals(SortingOrder.DESC)) {
            criteriaQuery.orderBy(criteriaBuilder.desc(sortBy));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.asc(sortBy));
        }
    }

    private List<Predicate> userTariffsInfoFiltering(List<Long> usId, Root<User> userRoot) {
        List<Predicate> predicateList = new ArrayList<>();
        for (Long id : usId) {
            predicateList.add(criteriaBuilder.equal(userRoot.get("id"), id));
        }
        return predicateList;
    }

    private Predicate userRegistrationDateFiltering(String[] dates, Root<User> userRoot) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(DATE_FORMAT);
        if (dates.length == 1) {
            LocalDate number = LocalDate.parse(dates[0], df);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get(DATE_OF_REGISTRATION).as(LocalDate.class),
                number);
        } else if (dates[0].equals("0")) {
            LocalDate number = LocalDate.parse(dates[1], df);
            return criteriaBuilder.lessThanOrEqualTo(userRoot.get(DATE_OF_REGISTRATION).as(LocalDate.class),
                number);
        } else {
            LocalDate number1 = LocalDate.parse(dates[0], df);
            LocalDate number2 = LocalDate.parse(dates[1], df);
            return criteriaBuilder.between(userRoot.get(DATE_OF_REGISTRATION).as(LocalDate.class), number1,
                number2);
        }
    }

    private Predicate userViolationsFiltering(String[] numberOfViolations, Root<User> userRoot) {
        if (numberOfViolations.length == 1) {
            int number = Integer.parseInt(numberOfViolations[0]);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get(VIOLATIONS), number);
        } else {
            int number1 = Integer.parseInt(numberOfViolations[0]);
            int number2 = Integer.parseInt(numberOfViolations[1]);
            return criteriaBuilder.between(userRoot.get(VIOLATIONS), number1, number2);
        }
    }

    private Predicate userBonusesFiltering(String[] bonuses, Root<User> userRoot) {
        if (bonuses.length == 1) {
            int number = Integer.parseInt(bonuses[0]);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get(POINTS), number);
        } else {
            int number1 = Integer.parseInt(bonuses[0]);
            int number2 = Integer.parseInt(bonuses[1]);
            return criteriaBuilder.between(userRoot.get(POINTS), number1, number2);
        }
    }

    private Predicate userOrdersFiltering(String[] bonuses, Root<User> userRoot) {
        Optional<Join<User, ?>> orderJoin = userRoot.getJoins().stream().findFirst();
        Expression<Long> expression = null;
        if (orderJoin.isPresent()) {
            expression = criteriaBuilder.count(orderJoin.get().get("user"));
        }
        if (bonuses.length == 1) {
            long number = Integer.parseInt(bonuses[0]);
            return criteriaBuilder.greaterThanOrEqualTo(expression, number);
        } else {
            long number1 = Integer.parseInt(bonuses[0]);
            long number2 = Integer.parseInt(bonuses[1]);
            return criteriaBuilder.between(expression, number1, number2);
        }
    }

    private String anyMatch(String search) {
        return "%" + search.toUpperCase() + "%";
    }
}
