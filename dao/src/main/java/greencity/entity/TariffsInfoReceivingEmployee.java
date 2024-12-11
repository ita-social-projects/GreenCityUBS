package greencity.entity;

import greencity.TariffsInfoReceivingEmployeeId;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@Table(name = "tariff_infos_receiving_employee_mapping")
@IdClass(TariffsInfoReceivingEmployeeId.class)
@AllArgsConstructor
@NoArgsConstructor
public class TariffsInfoReceivingEmployee {
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
