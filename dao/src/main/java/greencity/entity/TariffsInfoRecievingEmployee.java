package greencity.entity;

import greencity.TariffsInfoRecievingEmployeeId;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tariff_infos_receiving_employee_mapping")
@IdClass(TariffsInfoRecievingEmployeeId.class)
@AllArgsConstructor
@NoArgsConstructor
public class TariffsInfoRecievingEmployee {
    @Id
    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    @Id
    @ManyToOne
    @JoinColumn(name = "tariffs_info_id", referencedColumnName = "id")
    private TariffsInfo tariffsInfo;

    @Column(name = "has_chat")
    private Boolean hasChat;
}
