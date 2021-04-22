package greencity.dto.viber.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class SenderDto {
    private String id;
    private String name;
    private String avatar;
    private String country;
    private String language;
}
