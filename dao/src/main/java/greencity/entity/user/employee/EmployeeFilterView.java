package greencity.entity.user.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "employees_filters")
@IdClass(EmployeeFilterViewId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeFilterView {
    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @Id
    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "image_path")
    private String image;

    @Column(name = "tariffs_info_id")
    private Long tariffsInfoId;

    @Column(name = "employee_status")
    private String employeeStatus;

    @Column(name = "position_name")
    private String positionName;

    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_name_en")
    private String regionNameEn;

    @Column(name = "region_name_uk")
    private String regionNameUk;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name_en")
    private String locationNameEn;

    @Column(name = "location_name_uk")
    private String locationNameUk;

    @Column(name = "receiving_station_id")
    private Long receivingStationId;

    @Column(name = "receiving_station_name")
    private String receivingStationName;

    @Column(name = "courier_id")
    private Long courierId;

    @Column(name = "courier_name_en")
    private String courierNameEn;

    @Column(name = "courier_name_uk")
    private String courierNameUk;
}
