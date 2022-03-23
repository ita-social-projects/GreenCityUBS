package greencity.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AddressInfoDto {
    private String addressCity;
    private String addressRegion;
    private String addressStreet;
    private String addressDistinct;
    private String addressComment;
}