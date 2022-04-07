package greencity.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class AllActiveLocationsDto {
    private Long regionId;
    private String nameUk;
    private String nameEn;
    private List<LocationsDtos> locations;



}
