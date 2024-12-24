package greencity.dto.user;

import greencity.dto.language.LanguageVO;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserVO {
    private Long id;
    private String email;
    private LanguageVO languageVO;
}
