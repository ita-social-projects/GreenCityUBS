package greencity.dto.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.constant.ValidationConstant;
import greencity.dto.position.PositionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSignUpDto {
    @NotBlank
    private String name;

    @NotBlank
    @Email(regexp = ValidationConstant.EMAIL_REGEXP)
    private String email;

    @JsonIgnore
    private String password;

    private String uuid;

    private List<PositionDto> positions;

    @JsonProperty("isUbs")
    private boolean isUbs;
}
