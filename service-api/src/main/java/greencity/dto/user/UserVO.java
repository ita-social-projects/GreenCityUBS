package greencity.dto.user;

import greencity.dto.language.LanguageVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
