package greencity.entity.user.employee;


import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class EmployeeFilterViewId implements Serializable {

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "position_id")
    private Long positionId;
}
