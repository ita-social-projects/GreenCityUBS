package greencity.dto.order;

import greencity.dto.location.CoordinatesDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

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
    @Length(min = 2, max = 75)
    private String districtUk;
    @NotBlank
    @Length(min = 2, max = 75)
    private String districtEn;
    @Length(min = 3, max = 75)
    @NotNull
    private String streetUk;
    @Length(min = 3, max = 75)
    @NotNull
    private String streetEn;
    @Length(max = 4)
    private String houseCorpus;
    @Length(max = 4)
    private String entranceNumber;
    @Length(max = 10)
    private String houseNumber;
    @Length(min = 2, max = 75)
    private String cityUk;
    @Length(min = 2, max = 75)
    private String cityEn;
    @Length(min = 2, max = 75)
    private String regionUk;
    @Length(min = 2, max = 75)
    private String regionEn;
    @Length(max = 255)
    private String addressComment;
    @NotNull
    private CoordinatesDto coordinates;
    @NotNull
    private String placeId;
}
