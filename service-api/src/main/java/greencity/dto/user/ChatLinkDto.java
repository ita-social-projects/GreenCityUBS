package greencity.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record ChatLinkDto(
    Long userId,
    @JsonProperty("link") @Length(max = 255) @NotBlank @Pattern(regexp = "^https://my\\.binotel\\.ua.*",
        message = "Link must start with 'https://my.binotel.ua'") String link) {
}
