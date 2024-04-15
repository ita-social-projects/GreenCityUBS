package greencity.entity;

import greencity.TariffsInfoRecievingEmployeeId;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Table(name = "tariff_infos_receiving_employee_mapping")
@Entity
@IdClass(TariffsInfoRecievingEmployeeId.class)
@Data
public class TarriffsInfoRecievingEmployee {
    @Id
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @Id
    @ManyToOne
    @JoinColumn(name = "tariffs_info_id", referencedColumnName = "id")
    private TariffsInfo tariffsInfo;

    @Column(name = "has_chat")
    private boolean hasChat;
}
