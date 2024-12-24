package greencity.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleDto {
    private String key;
    private String ua;
    private String en;
}
