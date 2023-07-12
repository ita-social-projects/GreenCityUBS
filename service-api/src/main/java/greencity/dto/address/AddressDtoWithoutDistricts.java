package greencity.dto.address;

import greencity.dto.location.api.DistrictDto;
import greencity.entity.coords.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

import static greencity.constant.ValidationConstant.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDtoWithoutDistricts implements Serializable {
    @NotNull
    @Min(1)
    private Long id;
    @Length(max = 30)
    @Pattern(regexp = CITY_UK_REGEXP)
    private String city;
    @Length(max = 30)
    @Pattern(regexp = CH_UA + "{1,30}")
    private String district;
    @Length(max = 30)
    @Pattern(regexp = CH_UA + "{1,30}")
    private String region;
    @Length(max = 4)
    @Pattern(regexp = CH_NUM + "{1,4}")
    private String entranceNumber;
    @Length(max = 5)
    @Pattern(regexp = CH_NUM + "{1,5}")
    private String houseCorpus;
    @Pattern(regexp = CH_NUM + "{1,10}")
    private String houseNumber;
    @Length(max = 50)
    @Pattern(regexp = CH_UA + "{1,50}")
    private String street;

    @Length(max = 255)
    private String addressComment;

    private Coordinates coordinates;

    private Boolean actual;
    @Length(max = 30)
    @Pattern(regexp = CITY_EN_REGEXP)
    private String cityEn;
    @Length(max = 30)
    @Pattern(regexp = CH_EN + "{1,30}")
    private String regionEn;
    @Length(max = 50)
    @Pattern(regexp = CH_EN + "{1,50}")
    private String streetEn;
    @Length(max = 30)
    @Pattern(regexp = CH_EN + "{1,30}")
    private String districtEn;

    private String placeId;
}