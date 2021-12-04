package greencity.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderAddressExportDetailsDtoUpdate implements Serializable {
    @NotNull
    @Min(1)
    private Long addressId;
    @NotNull
    private Long orderId;
    @NotBlank
    @Length(max = 30)
    private String addressDistrict;
    @Length(min = 3, max = 40)
    @NotNull
    private String addressStreet;
    @Length(min = 1, max = 4)
    private String addressHouseCorpus;
    @Length(min = 1, max = 4)
    private String addressEntranceNumber;
    @Length(max = 5)
    private String addressHouseNumber;
    @Length(max = 15)
    private String addressCity;
    @Length(max = 15)
    private String addressRegion;
}
