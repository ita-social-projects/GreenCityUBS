package greencity.entity.user.employee;

import greencity.enums.EmployeeStatus;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "employees_position_view")
@IdClass(EmployeeFilterViewId.class)
@Data
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

    @Column(name = "image")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_status")
    private EmployeeStatus employeeStatus;

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
