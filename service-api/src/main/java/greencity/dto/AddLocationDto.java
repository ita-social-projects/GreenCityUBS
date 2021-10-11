package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddLocationDto {
    private String locationName;
    private Long minAmountOfBag;
    private String languageCode;
}
