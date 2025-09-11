package greencity.dto.viber.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class UserDto {
    private String id;
    private String name;
    private String avatar;
    private String country;
    private String language;
}
