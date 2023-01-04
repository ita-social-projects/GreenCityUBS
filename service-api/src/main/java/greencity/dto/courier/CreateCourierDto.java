package greencity.dto.courier;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@ToString
public class CreateCourierDto {
    @NotNull
    private String nameEn;

    @NotNull
    private String nameUk;
}
