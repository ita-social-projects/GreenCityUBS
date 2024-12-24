package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationsDtos {
    private Long locationId;
    private String nameEn;
    private String nameUk;
}
