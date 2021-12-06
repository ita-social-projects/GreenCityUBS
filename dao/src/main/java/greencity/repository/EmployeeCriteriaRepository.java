package greencity.repository;

import greencity.entity.user.employee.Employee;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

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
    public Page<Employee> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);
        Predicate predicate = getPredicate(employeeFilterCriteria, employeeRoot);
        criteriaQuery.where(predicate);
        setOrder(employeePage, criteriaQuery, employeeRoot);
        TypedQuery<Employee> employeeTypedQuery = entityManager.createQuery(criteriaQuery);
        employeeTypedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        employeeTypedQuery.setMaxResults(employeePage.getPageSize());
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        Pageable pageable = PageRequest.of(employeePage.getPageNumber(),
            employeePage.getPageSize(), sort);
        long employeesCount = getEmployeesCount(predicate);
        List<Employee> resulEmployees = employeeTypedQuery.getResultList();
        return new PageImpl<>(resulEmployees, pageable, employeesCount);
    }

    private long getEmployeesCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Employee> countOrderRoot = countQuery.from(Employee.class);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private void setOrder(EmployeePage employeePage, CriteriaQuery<Employee> criteriaQuery,
        Root<Employee> employeeRoot) {
        String[] split = employeePage.getSortBy().split("\\.");
        criteriaQuery.orderBy(Arrays.stream(split)
            .map(x -> employeePage.getSortDirection().equals(Sort.Direction.ASC)
                ? criteriaBuilder.asc(employeeRoot.get(x))
                : criteriaBuilder.desc(employeeRoot.get(x)))
            .collect(Collectors.toList()));
    }

    private Predicate getPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<Employee> employeeRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (Boolean.TRUE.equals(nonNullAndSize(employeeFilterCriteria.getReceivingStations()))) {
            CriteriaBuilder.In<String> stringIn = criteriaBuilder.in(criteriaBuilder.upper(
                employeeRoot.get("receivingStation").get("name")));
            Arrays.stream(employeeFilterCriteria.getReceivingStations()).map(String::toUpperCase)
                .forEach(stringIn::value);
            predicates.add(stringIn);
        }
        if (Boolean.TRUE.equals(nonNullAndSize(employeeFilterCriteria.getEmployeePositions()))) {
            CriteriaBuilder.In<String> stringIn = criteriaBuilder.in(criteriaBuilder.upper(
                employeeRoot.get("employeePosition").get("name")));
            Arrays.stream(employeeFilterCriteria.getEmployeePositions()).map(String::toUpperCase)
                .forEach(stringIn::value);
            predicates.add(stringIn);
        }
        if (nonNull(employeeFilterCriteria.getSearch())) {
            searchEmployee(employeeFilterCriteria, employeeRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }

    private void searchEmployee(EmployeeFilterCriteria employeeFilterCriteria,
        Root<Employee> employeeRoot,
        List<Predicate> predicates) {
        String[] searchWords = employeeFilterCriteria.getSearch().split(" ");
        Arrays.stream(searchWords).forEach(x -> predicates.add(fromEmployeeLikePredicate(x,
            employeeRoot)));
    }

    private Predicate fromEmployeeLikePredicate(String s, Root<Employee> employeeRoot) {
        Expression<String> firstName = criteriaBuilder.upper(employeeRoot.get("firstName"));
        Expression<String> lastName = criteriaBuilder.upper(employeeRoot.get("lastName"));
        Expression<String> phoneNumber = criteriaBuilder.upper(employeeRoot.get("phoneNumber"));
        Expression<String> email = criteriaBuilder.upper(employeeRoot.get("email"));
        String likeS = "%" + s + "%";
        return criteriaBuilder.or(
            criteriaBuilder.like(firstName, likeS),
            criteriaBuilder.like(lastName, likeS),
            criteriaBuilder.like(phoneNumber, likeS),
            criteriaBuilder.like(email, likeS));
    }

    private Boolean nonNullAndSize(String[] strings) {
        return nonNull(strings) && strings.length > 0;
    }
}
