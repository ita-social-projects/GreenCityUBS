package greencity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class EcoNumberDto {
    private String oldEcoNumber;
    private String newEcoNumber;
}
