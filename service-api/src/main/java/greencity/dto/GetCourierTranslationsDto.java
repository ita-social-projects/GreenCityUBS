package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class GetCourierTranslationsDto {
    private Long id;
    private Long minAmountOfBigBags;
    private Long maxAmountOfBigBags;
    private Long minPriceOfOrder;
    private Long maxPriceOfOrder;
    private Long locationId;
    private String courierLimit;
    private String name;
    private String languageCode;
    private String limitDescription;
}
