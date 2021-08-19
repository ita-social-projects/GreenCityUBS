package greencity.repository;

import greencity.entity.user.employee.EmployeeOrderPosition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmployeeOrderPositionRepository extends CrudRepository<EmployeeOrderPosition, Long> {
    /**
     * Method find employeeOrderPosition entity by his orderId.
     *
     * @param orderId {@link Long}
     * @return {@link List}of{@link EmployeeOrderPosition}
     * @author Bohdan Fedorkiv
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM employee_order_position WHERE order_id = :orderId ")
    List<EmployeeOrderPosition> findAllByOrderId(Long orderId);
}