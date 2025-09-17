package greencity.dto.notification;

import greencity.enums.UserCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCategoryDto {
    private UserCategory userCategory;
    private String descriptionUk;
    private String descriptionEn;
}
