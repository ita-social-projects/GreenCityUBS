package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationReceiverType {
    EMAIL("Email"), SITE("Site"), MOBILE("Mobile");

    private final String name;
}
