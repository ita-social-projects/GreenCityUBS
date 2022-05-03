package greencity.dto.service;

import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ServiceTranslationDto {
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private Long languageId;
}
