package greencity.validator.response;

import lombok.Getter;
import lombok.Setter;
import static greencity.constant.ValidationConstant.VALIDATION_RESPONSE_HEADER;
import static greencity.constant.ValidationConstant.VIOLATION_CHUNK;

public class ManualPaymentRequestValidationResponse {
    @Getter
    private boolean isValid = true;
    @Setter
    private String violationMessage;

    public void invalidate() {
        isValid = false;
    }

    @Override
    public String toString() {
        return VALIDATION_RESPONSE_HEADER + VIOLATION_CHUNK.formatted(violationMessage);
    }
}
