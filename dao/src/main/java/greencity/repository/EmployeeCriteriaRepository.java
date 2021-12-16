package greencity.repository;

import greencity.entity.enums.EmployeeStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
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
        return getAllEmployees(employeePage, employeeFilterCriteria);
    }

    /**
     * {@inheritDoc}
     */
    public Page<Employee> findAllActiveEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        return getAllActiveEmployees(employeePage, employeeFilterCriteria);
    }

    private Page<Employee> getAllEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);
        Predicate predicate = getAllPredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        criteriaQuery.where(predicate);
        setOrder(employeePage, criteriaQuery, employeeRoot);
        return processEmployees(employeePage, criteriaQuery, predicate);
    }

    private Page<Employee> getAllActiveEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        CriteriaQuery<Employee> criteriaQuery = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employeeRoot = criteriaQuery.from(Employee.class);
        Predicate predicate = getAllActivePredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        criteriaQuery.where(predicate);
        setOrder(employeePage, criteriaQuery, employeeRoot);
        return processEmployees(employeePage, criteriaQuery, predicate);
    }

    private Page<Employee> processEmployees(EmployeePage employeePage, CriteriaQuery<Employee> criteriaQuery,
        Predicate predicate) {
        TypedQuery<Employee> employeeTypedQuery = entityManager.createQuery(criteriaQuery);
        employeeTypedQuery.setFirstResult(employeePage.getPageNumber() * employeePage.getPageSize());
        employeeTypedQuery.setMaxResults(employeePage.getPageSize());
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        Pageable pageable = PageRequest.of(employeePage.getPageNumber(),
            employeePage.getPageSize(), sort);
        long employeesCount = getEmployeesCount(predicate);
        List<Employee> resultEmployees = employeeTypedQuery.getResultList();
        return new PageImpl<>(resultEmployees, pageable, employeesCount);
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

    private Predicate getAllPredicate(EmployeeFilterCriteria employeeFilterCriteria, Root<Employee> employeeRoot,
        CriteriaQuery<Employee> criteriaQuery) {
        List<Predicate> predicates = getBasicPredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }

    private Predicate getAllActivePredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<Employee> employeeRoot, CriteriaQuery<Employee> criteriaQuery) {
        List<Predicate> predicates = getBasicPredicate(employeeFilterCriteria, employeeRoot, criteriaQuery);
        CriteriaBuilder.In<EmployeeStatus> employeeStatusIn =
            criteriaBuilder.in(employeeRoot.get("employeeStatus"));
        employeeStatusIn.value(EmployeeStatus.ACTIVE);
        predicates.add(employeeStatusIn);
        return criteriaBuilder.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }

    private List<Predicate> getBasicPredicate(EmployeeFilterCriteria employeeFilterCriteria,
        Root<Employee> employeeRoot, CriteriaQuery<Employee> criteriaQuery) {
        List<Predicate> predicates = new ArrayList<>();
        if (Boolean.TRUE.equals(nonNullAndSize(employeeFilterCriteria.getReceivingStations()))) {
            filterByReceivingStation(employeeRoot, employeeFilterCriteria.getReceivingStations(),
                predicates, criteriaQuery);
        }
        if (Boolean.TRUE.equals(nonNullAndSize(employeeFilterCriteria.getEmployeePositions()))) {
            filterByEmployeePosition(employeeRoot, employeeFilterCriteria.getEmployeePositions(),
                predicates, criteriaQuery);
        }
        if (nonNull(employeeFilterCriteria.getSearch())) {
            searchEmployee(employeeFilterCriteria, employeeRoot, predicates);
        }
        return predicates;
    }

    private void filterByEmployeePosition(Root<Employee> employeeRoot, String[] filters,
        List<Predicate> predicates, CriteriaQuery<Employee> criteriaQuery) {
        Subquery<Position> subQuery = criteriaQuery.subquery(Position.class);
        Root<Employee> root = subQuery.correlate(employeeRoot);
        SetJoin<Employee, Position> join = root.joinSet("employeePosition", JoinType.LEFT);
        setFilterAndAddPredicate(join, filters, predicates, subQuery);
    }

    private void filterByReceivingStation(Root<Employee> employeeRoot, String[] filters,
        List<Predicate> predicates, CriteriaQuery<Employee> criteriaQuery) {
        Subquery<ReceivingStation> subQuery = criteriaQuery.subquery(ReceivingStation.class);
        Root<Employee> root = subQuery.correlate(employeeRoot);
        SetJoin<Employee, ReceivingStation> join = root.joinSet("receivingStation", JoinType.LEFT);
        setFilterAndAddPredicate(join, filters, predicates, subQuery);
    }

    private <T> void setFilterAndAddPredicate(SetJoin<Employee, T> join, String[] filters, List<Predicate> predicates,
        Subquery<T> subQueryOEP) {
        CriteriaBuilder.In<String> stringIn = criteriaBuilder.in(criteriaBuilder.upper(join.get("name")));
        Arrays.stream(filters).map(String::toUpperCase).forEach(stringIn::value);
        subQueryOEP.select(join).where(stringIn);
        predicates.add(criteriaBuilder.exists(subQueryOEP));
    }

    private void searchEmployee(EmployeeFilterCriteria employeeFilterCriteria,
        Root<Employee> employeeRoot, List<Predicate> predicates) {
        String[] searchWords = employeeFilterCriteria.getSearch().split(" ");
        Arrays.stream(searchWords).forEach(x -> predicates.add(fromEmployeeLikePredicate(x,
            employeeRoot)));
    }

    private Predicate fromEmployeeLikePredicate(String s, Root<Employee> employeeRoot) {
        Expression<String> firstName = criteriaBuilder.upper(employeeRoot.get("firstName"));
        Expression<String> lastName = criteriaBuilder.upper(employeeRoot.get("lastName"));
        Expression<String> phoneNumber = criteriaBuilder.upper(employeeRoot.get("phoneNumber"));
        Expression<String> email = criteriaBuilder.upper(employeeRoot.get("email"));
        String likeS = "%" + s.toUpperCase() + "%";
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
