package greencity.repository;

import greencity.entity.user.employee.EmployeeFilterView;
import greencity.entity.user.employee.Employee;
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
    public Page<EmployeeFilterView> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        return getAllEmployees(employeePage, employeeFilterCriteria);
    }

    private Page<EmployeeFilterView> getAllEmployees(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        
        CriteriaQuery<EmployeeFilterView> criteriaQuery = criteriaBuilder.createQuery(EmployeeFilterView.class);
        Root<EmployeeFilterView> employeeRoot = criteriaQuery.from(EmployeeFilterView.class);
        Predicate predicate = getAllPredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        criteriaQuery
                .select(employeeRoot)
                .where(predicate);
        setOrder(employeePage, criteriaQuery, employeeRoot);
        return processEmployees(employeePage, criteriaQuery, predicate);
    }
    
    private Page<EmployeeFilterView> processEmployees(
            EmployeePage employeePage, 
            CriteriaQuery<EmployeeFilterView> criteriaQuery, 
            Predicate predicate) {
        
        TypedQuery<EmployeeFilterView> employeeTypedQuery = entityManager.createQuery(criteriaQuery);
        employeeTypedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        employeeTypedQuery.setMaxResults(employeePage.getPageSize());
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        Pageable pageable = PageRequest.of(employeePage.getPageNumber(),
            employeePage.getPageSize(), sort);
        long employeesCount = getEmployeesCount(predicate);
        List<EmployeeFilterView> resultEmployees = employeeTypedQuery.getResultList();
        return new PageImpl<>(resultEmployees, pageable, employeesCount);
    }

    private long getEmployeesCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Employee> countOrderRoot = countQuery.from(Employee.class);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void setOrder(
            EmployeePage employeePage, 
            CriteriaQuery<EmployeeFilterView> criteriaQuery, 
            Root<EmployeeFilterView> employeeRoot) {
        
        String[] split = employeePage.getSortBy().split("\\.");
        criteriaQuery.orderBy(stream(split)
            .map(x -> employeePage.getSortDirection().equals(Sort.Direction.ASC)
                ? criteriaBuilder.asc(employeeRoot.get(x))
                : criteriaBuilder.desc(employeeRoot.get(x)))
            .collect(Collectors.toList()));
    }

    private Predicate getAllPredicate(
            EmployeeFilterCriteria employeeFilterCriteria,
            Root<EmployeeFilterView> employeeRoot, 
            CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        
        List<Predicate> predicates = getBasicPredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }
    
    private List<Predicate> getBasicPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<EmployeeFilterView> employeeFilterViewRoot, CriteriaQuery<EmployeeFilterView> criteriaQuery) {
        List<Predicate> predicates = new ArrayList<>();
        if (isStringNotNullAndNotEmpty(employeeFilterCriteria.getSearchLine())) {

            List<Expression<String>> toUpperCaseExpressions =
                    extractPossibleExpressionsForSearchLineFiltering(employeeFilterViewRoot);

            predicates.addAll(
                    getAllSearchLinePredicates(employeeFilterCriteria.getSearchLine(), toUpperCaseExpressions));
        }
        // todo: Probably will be replaced by employee status filtering
//        if (isStringNotNullAndNotEmpty(employeeFilterCriteria.getContact())) {
//            predicates.add(criteriaBuilder.equal(
//                    employeeFilterViewRoot.get("phoneNumber"),
//                    employeeFilterCriteria.getContact()));
//        }
        if (employeeFilterCriteria.getEmployeeStatus() != null) {
            predicates.add(criteriaBuilder.equal(
                    employeeFilterViewRoot.get("employeeStatus"),
                    employeeFilterCriteria.getEmployeeStatus()));
        }
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getPositions())) {
            predicates.add(employeeFilterViewRoot.get("positionId")
                    .in(employeeFilterCriteria.getPositions()));
        }
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getRegions())) {
            predicates.add(employeeFilterViewRoot.get("regionId")
                    .in(employeeFilterCriteria.getRegions()));
        }
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getLocations())) {
            predicates.add(employeeFilterViewRoot.get("locationId")
                    .in(employeeFilterCriteria.getLocations()));
        }
        if (isListNotNullAndNotEmpty(employeeFilterCriteria.getCouriers())) {
            predicates.add(employeeFilterViewRoot.get("courierId")
                    .in(employeeFilterCriteria.getCouriers()));
        }

        return predicates;
    }

    private List<Predicate> getAllSearchLinePredicates(String searchLine, List<Expression<String>> toUpperCaseExpressions) {
        return stream(searchLine.split("\\s"))
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

    private Predicate mapSearchArgumentToPredicate(
            String argument, List<Expression<String>> toUpperCaseExpressions) {

        var pattern = "%" + argument + "%";
        var searchLinePredicates = toUpperCaseExpressions.stream()
                .map(stringExpression -> criteriaBuilder.like(stringExpression, pattern))
                .toArray(Predicate[]::new);

        return criteriaBuilder.or(searchLinePredicates);
    }

    private boolean isListNotNullAndNotEmpty(List<?> parameter) {
        return parameter != null && parameter.size() > 0;
    }

    private boolean isStringNotNullAndNotEmpty(String str) {
        return str != null && str.length() > 0;
    }
}
