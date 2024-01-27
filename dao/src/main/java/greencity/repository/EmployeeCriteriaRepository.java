package greencity.repository;

import greencity.entity.user.employee.EmployeeFilterView;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Expression;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static greencity.enums.EmployeeStatus.employeeStatusExist;
import static java.util.Arrays.stream;

@Repository
public class EmployeeCriteriaRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private static final String POSITION_ID = "positionId";
    private static final String EMPLOYEE_ID = "employeeId";

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    public EmployeeCriteriaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    public List<EmployeeFilterView> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        return getAllEmployees(employeePage, employeeFilterCriteria);
    }

    private List<EmployeeFilterView> getAllEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        CriteriaQuery<EmployeeFilterView> criteriaQuery = criteriaBuilder.createQuery(EmployeeFilterView.class);
        Root<EmployeeFilterView> employeeRoot = criteriaQuery.from(EmployeeFilterView.class);
        Predicate predicate = composePredicateForFiltering(employeeFilterCriteria, employeeRoot, criteriaQuery);
        criteriaQuery.select(employeeRoot).where(predicate).orderBy(getOrderBy(employeePage, employeeRoot));
        return processEmployees(employeePage, criteriaQuery);
    }

    private List<EmployeeFilterView> processEmployees(EmployeePage employeePage,
        CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        TypedQuery<EmployeeFilterView> employeeTypedQuery = entityManager.createQuery(criteriaQuery);
        employeeTypedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        employeeTypedQuery.setMaxResults(employeePage.getPageSize());
        return employeeTypedQuery.getResultList();
    }

    private Order getOrderBy(EmployeePage employeePage, Root<EmployeeFilterView> root) {
        return employeePage.getSortDirection().equals(Sort.Direction.ASC)
            ? criteriaBuilder.asc(root.get(employeePage.getSortBy()))
            : criteriaBuilder.desc(root.get(employeePage.getSortBy()));
    }

    private Predicate composePredicateForFiltering(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        List<Predicate> predicates = collectAllPredicatesToList(
            employeeFilterCriteria, employeeFilterViewRoot, criteriaQuery);
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private List<Predicate> collectAllPredicatesToList(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        List<Predicate> predicates = new ArrayList<>();

        addPredicateDistinctUniqueEmployeesFromQuery(criteriaQuery, employeeFilterViewRoot, predicates);
        addSearchLinePredicates(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addEmployeeStatusPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addEmployeePositionPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addRegionPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addLocationPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addCourierPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        return predicates;
    }

    private void addPredicateDistinctUniqueEmployeesFromQuery(CriteriaQuery<EmployeeFilterView> criteriaQuery,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        Subquery<Long> subQuery = criteriaQuery.subquery(Long.class);
        Root<EmployeeFilterView> subQueryRoot = subQuery.from(EmployeeFilterView.class);
        subQuery.select(criteriaBuilder.min(subQueryRoot.get(POSITION_ID)))
            .where(criteriaBuilder.equal(subQueryRoot.get(EMPLOYEE_ID), employeeFilterViewRoot.get(EMPLOYEE_ID)));
        predicates.add(criteriaBuilder.equal(employeeFilterViewRoot.get(POSITION_ID), subQuery));
    }

    private void addCourierPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getCouriers())) {
            predicates.add(employeeFilterViewRoot.get("courierId")
                .in(employeeFilterCriteria.getCouriers()));
        }
    }

    private void addLocationPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getLocations())) {
            predicates.add(employeeFilterViewRoot.get("locationId")
                .in(employeeFilterCriteria.getLocations()));
        }
    }

    private void addRegionPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getRegions())) {
            predicates.add(employeeFilterViewRoot.get("regionId")
                .in(employeeFilterCriteria.getRegions()));
        }
    }

    private void addEmployeePositionPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getPositions())) {
            predicates.add(employeeFilterViewRoot.get(POSITION_ID)
                .in(employeeFilterCriteria.getPositions()));
        }
    }

    private void addEmployeeStatusPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (employeeStatusExist(employeeFilterCriteria.getEmployeeStatus())) {
            predicates.add(criteriaBuilder.equal(
                employeeFilterViewRoot.get("employeeStatus"),
                employeeFilterCriteria.getEmployeeStatus()));
        }
    }

    private void addSearchLinePredicates(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot,
        List<Predicate> predicates) {
        if (isStringNotNullAndNotEmpty(employeeFilterCriteria.getSearchLine())) {
            List<Expression<String>> toUpperCaseExpressions =
                extractPossibleExpressionsForSearchLineFiltering(employeeFilterViewRoot);

            predicates.addAll(
                getAllSearchLinePredicates(employeeFilterCriteria.getSearchLine(), toUpperCaseExpressions));
        }
    }

    private List<Predicate> getAllSearchLinePredicates(String searchLine,
        List<Expression<String>> toUpperCaseExpressions) {
        return stream(searchLine.split("\\s+"))
            .map(searchArgument -> mapSearchArgumentToPredicate(searchArgument, toUpperCaseExpressions))
            .collect(Collectors.toList());
    }

    private List<Expression<String>> extractPossibleExpressionsForSearchLineFiltering(
        Root<EmployeeFilterView> employeeFilterViewRoot) {
        List<Expression<String>> expressions = new ArrayList<>();
        expressions.add(criteriaBuilder.upper(employeeFilterViewRoot.get("firstName")));
        expressions.add(criteriaBuilder.upper(employeeFilterViewRoot.get("lastName")));
        expressions.add(criteriaBuilder.upper(employeeFilterViewRoot.get("email")));
        expressions.add(criteriaBuilder.upper(employeeFilterViewRoot.get("phoneNumber")));
        return expressions;
    }

    private Predicate mapSearchArgumentToPredicate(String argument, List<Expression<String>> toUpperCaseExpressions) {
        var pattern = "%" + argument + "%";
        var searchLinePredicates = toUpperCaseExpressions.stream()
            .map(stringExpression -> criteriaBuilder.like(stringExpression, pattern))
            .toArray(Predicate[]::new);
        return criteriaBuilder.or(searchLinePredicates);
    }

    private boolean isListNotNullAndNotEmpty(List<?> parameter) {
        return parameter != null && !parameter.isEmpty();
    }

    private boolean isStringNotNullAndNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
