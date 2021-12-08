package greencity.repository;

import greencity.entity.user.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides an interface to manage {@link Employee} entity.
 *
 * @author Mykola Danylko
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    /**
     * Method gets all {@link Employee} employee.
     *
     * @param pageable {@link Pageable}
     * @return list of {@link Employee}
     * @author Mykola Danylko
     */
    Page<Employee> findAll(Pageable pageable);

    /**
     * Method gets all active {@link Employee} employee.
     *
     * @param pageable {@link Pageable}
     * @return list of {@link Employee}
     * @author Yurii Kuzo
     */
    @Query(nativeQuery = true, value = "SELECT * FROM employees "
        + "WHERE employees.status = 'ACTIVE'")
    Page<Employee> findAllActiveEmployees(Pageable pageable);

    /**
     * Method checks if {@link String} phoneNumber already exist.
     *
     * @param phoneNumber {@link String}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Method checks if {@link String} email already exist.
     *
     * @param email {@link String}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    boolean existsByEmail(String email);

    /**
     * Method checks if {@link String} phoneNumber exist except current employee.
     *
     * @param phoneNumber {@link String}
     * @param id          {@link Long}
     * @return {@link Employee}
     * @author Mykola Danylko
     */
    @Query(nativeQuery = true, value = "SELECT * FROM employees "
        + "WHERE phone_number = :phoneNumber "
        + "AND id <> :id")
    Employee checkIfPhoneNumberUnique(String phoneNumber, Long id);

    /**
     * Method checks if {@link String} email exist except current employee.
     *
     * @param email {@link String}
     * @param id    {@link Long}
     * @return {@link Employee}
     * @author Mykola Danylko
     */
    @Query(nativeQuery = true, value = "SELECT * FROM employees "
        + "WHERE email = :email "
        + "AND id <> :id")
    Employee checkIfEmailUnique(String email, Long id);

    /**
     * Method return all employees depends from they positions.
     *
     * @param positionId {@link Integer}
     * @return {@link List}of{@link Employee}
     * @author Bohdan Fedorkiv
     */
    @Query(value = "SELECT * FROM EMPLOYEES "
        + "JOIN EMPLOYEE_POSITION "
        + "ON EMPLOYEES.ID = EMPLOYEE_POSITION.EMPLOYEE_ID "
        + "JOIN POSITIONS "
        + "ON EMPLOYEE_POSITION.POSITION_ID = POSITIONS.ID "
        + "WHERE POSITIONS.ID = :positionId", nativeQuery = true)
    List<Employee> getAllEmployeeByPositionId(Long positionId);

    /**
     * Method find employee by his firstName and lastName.
     *
     * @param firstName {@link String}
     * @param lastName  {@link String}
     * @return {@link Optional}of{@link Employee}
     * @author Bohdan Fedorkiv
     */
    @Query(value = "SELECT * FROM EMPLOYEES "
        + "WHERE EMPLOYEES.FIRST_NAME = :firstName "
        + "AND EMPLOYEES.LAST_NAME = :lastName ", nativeQuery = true)
    Optional<Employee> findByName(String firstName, String lastName);

    /**
     * Method find current position for Employee.
     *
     * @param employeeId {@link Long}.
     * @return {@link Long}.
     * @author Yuriy Bahlay.
     */
    @Query(value = "SELECT EMPLOYEE_POSITION.POSITION_ID FROM EMPLOYEE_POSITION "
        + "WHERE EMPLOYEE_ID = :employeeId", nativeQuery = true)
    Optional<Long> findPositionForEmployee(Long employeeId);

    /**
     * Method find employee by his email.
     *
     * @param email {@link String}.
     * @return {@link Optional}of{@link Employee}.
     * @author Liubomyr Pater.
     */
    @Query(value = "SELECT * FROM EMPLOYEES WHERE EMPLOYEES.EMAIL = :email", nativeQuery = true)
    Optional<Employee> findByEmail(@Param(value = "email") String email);
}