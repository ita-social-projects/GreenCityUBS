package greencity.repository;

import greencity.entity.user.employee.EmployeeOrderPosition;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
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

    /**
     * Method find current position for Employee in EmployeeOrderPosition table.
     *
     * @param employeeId {@link Long}.
     * @return {@link Long}.
     * @author Yuriy Bahlay.
     */
    @Query(" SELECT eop.position.id "
        + " FROM EmployeeOrderPosition eop "
        + " WHERE eop.employee.id = :employeeId ")
    Long findPositionOfEmployeeAssignedForOrder(@Param("employeeId") Long employeeId);

    /**
     * Method verify if Employee already assigned for current Order.
     *
     * @param orderId    {@link Long}.
     * @param employeeId {@link Long}.
     * @return {@link Boolean}.
     * @author Yuriy Bahlay.
     */
    boolean existsByOrderIdAndEmployeeId(@Param("orderId") Long orderId,
        @Param("employeeId") Long employeeId);
}