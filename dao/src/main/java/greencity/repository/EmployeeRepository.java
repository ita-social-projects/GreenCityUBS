package greencity.repository;

import greencity.entity.user.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

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
     * @param id {@link Long}
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
     * @param id {@link Long}
     * @return {@link Employee}
     * @author Mykola Danylko
     */
    @Query(nativeQuery = true, value = "SELECT * FROM employees "
            + "WHERE email = :email "
            + "AND id <> :id")
    Employee checkIfEmailUnique(String email, Long id);
}
