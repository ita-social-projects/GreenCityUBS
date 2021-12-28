package greencity.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class LocationResponseDto {
    private Long id;
    private String name;
    private String languageCode;
}
