package greencity.enums;

import java.util.Arrays;

public enum EmployeeStatus {
    ACTIVE,
    INACTIVE;

    /**
     * This is method which checks if an EmployeeStatus with that name exists.
     *
     * @param statusName {@link String}.
     * @return boolean.
     */
    public static boolean employeeStatusExist(String statusName) {
        return Arrays.stream(EmployeeStatus.values())
            .anyMatch(status -> status.name().equals(statusName));
    }
}
