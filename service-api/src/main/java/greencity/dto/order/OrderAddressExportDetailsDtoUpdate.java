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
    private Long id;
    @NotBlank
    @Length(max = 30)
    private String district;
    @NotBlank
    @Length(max = 30)
    private String districtEn;
    @Length(min = 3, max = 40)
    @NotNull
    private String street;
    @Length(min = 3, max = 40)
    @NotNull
    private String streetEn;
    @Length(min = 1, max = 4)
    private String houseCorpus;
    @Length(min = 1, max = 4)
    private String entranceNumber;
    @Length(max = 10)
    private String houseNumber;
    @Length(max = 15)
    private String city;
    @Length(max = 15)
    private String cityEn;
    @Length(max = 15)
    private String region;
    @Length(max = 15)
    private String regionEn;
}
