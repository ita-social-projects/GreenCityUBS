package greencity.repository;

import greencity.entity.user.employee.Employee;
import greencity.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
     * Method gets all employees.
     *
     * @param pageable {@link Pageable}
     * @return list of {@link Employee}
     * @author Mykola Danylko
     */
    Page<Employee> findAll(Pageable pageable);

    /**
     * Method checks if employee with phoneNumber already exists.
     *
     * @param phoneNumber {@link String}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Method checks if employee with email already exists.
     *
     * @param email {@link String}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    boolean existsByEmail(String email);

    /**
     * Method checks if employee exists with phoneNumber and id.
     *
     * @param phoneNumber {@link String}
     * @param id          {@link Long}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    boolean existsByPhoneNumberAndId(String phoneNumber, Long id);

    /**
     * Method checks if employee exists with email and id.
     *
     * @param email {@link String}
     * @param id    {@link Long}
     * @return {@link Boolean}
     * @author Mykola Danylko
     */
    Boolean existsByEmailAndId(String email, Long id);

    /**
     * Method that return list of Employee by an email except with current id.
     *
     * @param email {@link String}
     * @param id    {@link Long}
     * @return list of employee;
     * @author Nikita Korzh
     */
    List<Employee> findEmployeesByEmailAndIdNot(String email, Long id);

    /**
     * Method return all employees depends on their positions.
     *
     * @param positionId {@link Integer}
     * @return {@link List}of{@link Employee}
     * @author Bohdan Fedorkiv
     */
    List<Employee> findAllByEmployeePositionId(Long positionId);

    /**
     * Method find employee by his firstName and lastName.
     *
     * @param firstName {@link String}
     * @param lastName  {@link String}
     * @return {@link Optional}of{@link Employee}
     * @author Bohdan Fedorkiv
     */
    Optional<Employee> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Method find current position for Employee.
     *
     * @param employeeId {@link Long}.
     * @return {@link Optional} of {@link Long}.
     * @author Yuriy Bahlay.
     */
    Optional<Long> findPositionById(Long employeeId);

    /**
     * Method find employee by his email.
     *
     * @param email {@link String}.
     * @return {@link Optional}of{@link Employee}.
     * @author Liubomyr Pater.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Method that return employee by his uuid.
     *
     * @param uuid {@link String}
     * @return employee.
     */
    Optional<Employee> findByUuid(String uuid);

    /**
     * Method find current tariffsInfo for Employee.
     *
     * @param employeeId {@link Long}.
     * @return {@link List} of {@link Long}.
     * @author Hlazova Nataliia.
     */
    @Query(
        value = "SELECT TARIFF_INFOS_RECEIVING_EMPLOYEE_MAPPING.TARIFFS_INFO_ID "
            + "FROM TARIFF_INFOS_RECEIVING_EMPLOYEE_MAPPING "
            + "WHERE EMPLOYEE_ID = :employeeId",
        nativeQuery = true)
    List<Long> findTariffsInfoForEmployee(Long employeeId);

    /**
     * Method returns true or false if Employee exists by email with
     * EmployeeStatus.INACTIVE.
     *
     * @param email {@link String}.
     * @return boolean.
     * @author Olena Sotnik.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true "
        + "ELSE false END "
        + "FROM Employee e "
        + "WHERE e.email = ?1 "
        + "AND e.employeeStatus = 'INACTIVE'")
    boolean existsByEmailAndInactiveStatus(String email);

    /**
     * Method returns true or false if Employee exists by email with
     * EmployeeStatus.ACTIVE.
     *
     * @param email {@link String}.
     * @return boolean.
     * @author Olena Sotnik.
     */
    @Query(value = "SELECT CASE WHEN COUNT(e) > 0 THEN true "
        + "ELSE false END "
        + "FROM Employee e "
        + "WHERE e.email = ?1 "
        + "AND e.employeeStatus = 'ACTIVE'")
    boolean existsByEmailAndActiveStatus(String email);
}