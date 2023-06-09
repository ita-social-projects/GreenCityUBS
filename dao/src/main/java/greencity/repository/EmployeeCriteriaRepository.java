package greencity.repository;

import greencity.entity.user.employee.EmployeeFilterView;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static greencity.enums.EmployeeStatus.*;
import static java.util.Arrays.*;

@Repository
public class EmployeeCriteriaRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

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

        Predicate predicate = composePredicateForFiltering(employeeFilterCriteria, employeeRoot);

        criteriaQuery.select(employeeRoot).where(predicate);

        setOrderBy(employeePage, criteriaQuery, employeeRoot);

        return processEmployees(employeePage, criteriaQuery);
    }

    private List<EmployeeFilterView> processEmployees(EmployeePage employeePage,
        CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        TypedQuery<EmployeeFilterView> employeeTypedQuery = entityManager.createQuery(criteriaQuery);
        employeeTypedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        employeeTypedQuery.setMaxResults(employeePage.getPageSize());
        return employeeTypedQuery.getResultList();
    }

    private void setOrderBy(EmployeePage employeePage, CriteriaQuery<EmployeeFilterView> criteriaQuery,
        Root<EmployeeFilterView> employeeRoot) {
        String[] split = employeePage.getSortBy().split("\\.");
        criteriaQuery.orderBy(stream(split)
            .map(x -> employeePage.getSortDirection().equals(Sort.Direction.ASC)
                ? criteriaBuilder.asc(employeeRoot.get(x))
                : criteriaBuilder.desc(employeeRoot.get(x)))
            .collect(Collectors.toList()));
    }

    private Predicate composePredicateForFiltering(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot) {
        List<Predicate> predicates = collectAllPredicatesToList(employeeFilterCriteria, employeeFilterViewRoot);
        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private List<Predicate> collectAllPredicatesToList(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot) {
        List<Predicate> predicates = new ArrayList<>();

        addSearchLinePredicates(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addEmployeeStatusPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addEmployeePositionPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addRegionPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addLocationPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        addCourierPredicate(employeeFilterCriteria, employeeFilterViewRoot, predicates);
        return predicates;
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
            predicates.add(employeeFilterViewRoot.get("positionId")
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
        return str != null && str.length() > 0;
    }
}
