package greencity.dto;

import lombok.*;

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
    private String addressEntranceNumber;
    private String addressHouseCorpus;
    private String addressHouseNumber;
    private String addressStreet;
}
