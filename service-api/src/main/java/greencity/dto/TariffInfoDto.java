package greencity.dto;

import greencity.dto.courier.CourierDto;
import greencity.enums.CourierLimit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class TariffInfoDto {
    private Long tariffInfoId;
    private Long min;
    private Long max;
    private CourierLimit courierLimit;
    private CourierDto courierDto;
    private String limitDescription;
    private String tariffNameUk;
    private String tariffNameEn;
}