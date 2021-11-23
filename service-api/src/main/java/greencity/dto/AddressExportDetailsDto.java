package greencity.dto;

import lombok.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class AddressExportDetailsDto {
    private Long id;
    private String addressCity;
    private String addressDistrict;
    private String addressRegion;
    private Long addressEntranceNumber;
    private Long addressHouseCorpus;
    private Long addressHouseNumber;
    private String addressStreet;
}
