package greencity.repository;

import greencity.entity.table.TableColumnWidthForEmployee;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TableColumnWidthForEmployeeRepository extends CrudRepository<TableColumnWidthForEmployee, Long> {
    /**
     * method that returns {@link TableColumnWidthForEmployee} by employeeId.
     *
     * @param employeeId id of {@link greencity.entity.user.employee.Employee}
     * @return {@link TableColumnWidthForEmployee}
     * @author Oleh Kulbaba
     */
    Optional<TableColumnWidthForEmployee> findByEmployeeId(Long employeeId);
}
