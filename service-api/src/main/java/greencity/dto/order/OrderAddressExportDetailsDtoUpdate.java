package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    @Length(max = 30)
    private String addressDistrict;
    @NotBlank
    @Length(max = 30)
    private String addressDistrictEng;
    @Length(min = 3, max = 40)
    @NotNull
    private String addressStreet;
    @Length(min = 3, max = 40)
    @NotNull
    private String addressStreetEng;
    @Length(min = 1, max = 4)
    private String addressHouseCorpus;
    @Length(min = 1, max = 4)
    private String addressEntranceNumber;
    @Length(max = 10)
    private String addressHouseNumber;
    @Length(max = 15)
    private String addressCity;
    @Length(max = 15)
    private String addressCityEng;
    @Length(max = 15)
    private String addressRegion;
    @Length(max = 15)
    private String addressRegionEng;
}
