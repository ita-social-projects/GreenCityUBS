package greencity.repository;

import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
    List<EmployeeOrderPosition> findAllByOrderId(Long orderId);

    /**
     * Method update OrderEmployeePosition.
     *
     * @author Sikhovskiy Rostyslav
     */
    @Transactional
    @Modifying
    @Query("update EmployeeOrderPosition e set e.employee=:employee where e.order=:order and e.position=:position")
    void update(Order order, Employee employee, Position position);

    /**
     * Method checks if exist record with current Order and Position id .
     *
     * @author Sikhovskiy Rostyslav
     */
    Boolean existsByOrderAndPosition(Order order, Position position);
}