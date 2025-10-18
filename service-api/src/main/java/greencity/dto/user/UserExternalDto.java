package greencity.dto.user;

import greencity.dto.language.LanguageDTO;
import greencity.dto.location.UserLocationDto;
import greencity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public class UserExternalDto {
    private Long id;
    private String name;
    private String email;
    private String uuid;
    private Role role;
    private UserLocationDto userLocation;
    private LanguageDTO languageDTO;
}
