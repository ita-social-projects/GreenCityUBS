package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonoBankStatuses {
    CREATED(1, "created"),
    PROCESSING(2, "processing"),
    HOLD(3, "hold"),
    SUCCESS(4, "success"),
    FAILURE(5, "failure"),
    REVERSED(6, "reversed"),
    EXPIRED(7, "expired");

    private final int value;
    private final String name;
}
