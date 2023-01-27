package greencity.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class RegionDto {
    private Long regionId;
    private String nameEn;
    private String nameUk;
}
