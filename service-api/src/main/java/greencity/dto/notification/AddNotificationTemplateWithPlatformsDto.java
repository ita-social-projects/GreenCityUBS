package greencity.dto.notification;

import greencity.annotations.ValidSchedulePattern;
import greencity.enums.UserCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AddNotificationTemplateWithPlatformsDto {
    @NotNull
    @NotBlank
    private String titleUk;
    @ValidSchedulePattern
    private String schedule;
    @NotNull
    private UserCategory userCategory;
    @NotNull
    @NotBlank
    private String titleEn;
    @NotNull
    private List<AddNotificationPlatformDto> platforms = new ArrayList<>();
}
