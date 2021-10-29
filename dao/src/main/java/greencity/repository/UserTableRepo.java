package greencity.repository;

import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import greencity.filters.UserFilterCriteria;
import org.springframework.data.domain.*;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@Repository
public class UserTableRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private static final String ORDERS = "orders";

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
        SortingOrder sortingOrder, Pageable page) {
        CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = criteriaQuery.from(User.class);
        userRoot.join(ORDERS, JoinType.INNER);
        criteriaQuery.groupBy(userRoot.get("id"));
        setOrder(column, sortingOrder, criteriaQuery, userRoot);
        if (userFilterCriteria != null) {
            Predicate predicate = getPredicate(userFilterCriteria, userRoot);
            criteriaQuery.where(predicate);
        }

        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(page.getPageNumber() * 10);
        typedQuery.setMaxResults(10);

        Sort sort = Sort.by(Sort.Direction.valueOf(sortingOrder.toString()), "recipientPhone");
        Pageable pageable = PageRequest.of(page.getPageNumber(), 10, sort);

        List<User> resultList = typedQuery.getResultList();

        return new PageImpl<>(resultList, pageable, resultList.size());
    }

    private Predicate getPredicate(UserFilterCriteria us, Root<User> userRoot) {
        List<Predicate> predicateList = new ArrayList<>();

        if (nonNull(us.getOrderDate())) {
            predicateList.add(orderDateFiltering(us.getOrderDate(), userRoot));
        }
        if (nonNull(us.getUserRegistrationDate())) {
            predicateList.add(userRegistrationDateFiltering(us.getUserRegistrationDate(), userRoot));
        }
        if (nonNull(us.getNumberOfViolations())) {
            predicateList.add(userViolationsFiltering(us.getNumberOfViolations(), userRoot));
        }
        if (nonNull(us.getNumberOfBonuses())) {
            predicateList.add(userBonusesFiltering(us.getNumberOfBonuses(), userRoot));
        }
        if (nonNull(us.getNumberOfOrders())) {
            predicateList.add(userBonusesFiltering(us.getNumberOfBonuses(), userRoot));
        }
        return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
    }

    private void setOrder(String column, SortingOrder sortingOrder, CriteriaQuery<User> criteriaQuery,
        Root<User> userRoot) {
        Expression<?> sortBy = userRoot.get("recipientName");

        if (nonNull(column)) {
            if (column.equals("orderDate")) {
                sortBy = criteriaBuilder.max(userRoot.getJoins().stream().findFirst().get().get("orderDate"));
            } else if (column.equals("number_of_orders")) {
                sortBy = criteriaBuilder.count(userRoot.getJoins().stream().findFirst().get().get("user"));
            } else if (column.equals("clientName")) {
                sortBy = userRoot.get("recipientName");
            } else {
                sortBy = userRoot.get(column);
            }
        }
        if (sortingOrder.equals(SortingOrder.DESC)) {
            criteriaQuery.orderBy(criteriaBuilder.desc(sortBy));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.asc(sortBy));
        }
    }

    private Predicate orderDateFiltering(String[] dates, Root<User> userRoot) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (dates.length == 1) {
            LocalDate number = LocalDate.parse(dates[0], df);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.getJoins().stream()
                .findFirst().get().get("orderDate").as(LocalDate.class), number);
        } else if (dates[0].equals("0")) {
            LocalDate number = LocalDate.parse(dates[1], df);
            return criteriaBuilder.lessThanOrEqualTo(userRoot.getJoins().stream()
                .findFirst().get().get("orderDate").as(LocalDate.class), number);
        } else {
            LocalDate number1 = LocalDate.parse(dates[0], df);
            LocalDate number2 = LocalDate.parse(dates[1], df);
            return criteriaBuilder.between(userRoot.getJoins().stream()
                .findFirst().get().get("orderDate").as(LocalDate.class), number1, number2);
        }
    }

    private Predicate userRegistrationDateFiltering(String[] dates, Root<User> userRoot) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (dates.length == 1) {
            LocalDateTime number = LocalDateTime.parse(dates[0], df);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get("dateOfRegistration").as(LocalDateTime.class),
                number);
        } else if (dates[0].equals("0")) {
            LocalDateTime number = LocalDateTime.parse(dates[1], df);
            return criteriaBuilder.lessThanOrEqualTo(userRoot.get("dateOfRegistration").as(LocalDateTime.class),
                number);
        } else {
            LocalDateTime number1 = LocalDateTime.parse(dates[0], df);
            LocalDateTime number2 = LocalDateTime.parse(dates[1], df);
            return criteriaBuilder.between(userRoot.get("dateOfRegistration").as(LocalDateTime.class), number1,
                number2);
        }
    }

    private Predicate userViolationsFiltering(String[] numberOfViolations, Root<User> userRoot) {
        if (numberOfViolations.length == 1) {
            int number = Integer.parseInt(numberOfViolations[0]);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get("violations"), number);
        } else {
            int number1 = Integer.parseInt(numberOfViolations[0]);
            int number2 = Integer.parseInt(numberOfViolations[1]);
            return criteriaBuilder.between(userRoot.get("violations"), number1, number2);
        }
    }

    private Predicate userBonusesFiltering(String[] bonuses, Root<User> userRoot) {
        if (bonuses.length == 1) {
            int number = Integer.parseInt(bonuses[0]);
            return criteriaBuilder.greaterThanOrEqualTo(userRoot.get("currentPoints"), number);
        } else {
            int number1 = Integer.parseInt(bonuses[0]);
            int number2 = Integer.parseInt(bonuses[1]);
            return criteriaBuilder.between(userRoot.get("currentPoints"), number1, number2);
        }
    }
}
