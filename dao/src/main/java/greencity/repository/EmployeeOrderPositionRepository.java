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
     * Method count if Employee already assigned for current Order.
     *
     * @param orderId    {@link Long}.
     * @param employeeId {@link Long}.
     * @param positionId {@link Long}.
     * @return {@link List}of{@link EmployeeOrderPosition}
     * @author Yuriy Bahlay.
     */
    @Query(" SELECT COUNT(eop.id) "
        + " FROM EmployeeOrderPosition eop "
        + " WHERE eop.order.id = :orderId "
        + " AND eop.employee.id = :employeeId "
        + " AND eop.position.id = :positionId")
    int countEmployeeByIdAndOrderIdAndPositionId(@Param("orderId") Long orderId,
        @Param("employeeId") Long employeeId,
        @Param("positionId") Long positionId);
}