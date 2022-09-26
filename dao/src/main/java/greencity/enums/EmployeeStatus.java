package greencity.enums;

public enum EmployeeStatus {
    ACTIVE(1),
    INACTIVE(2);

    private int statusValue;

    EmployeeStatus(final int value) {
        this.statusValue = value;
    }

    /**
     * Method for getting a value of employeeStatus, which is used for translating
     * order status wo different languages.
     *
     * @return {@link int} employeeStatus value.
     */
    public int getNumValue() {
        return statusValue;
    }

    /**
     * This is method which convert value from num to enum.
     *
     * @param value {@link Long}.
     * @return {@link String}.
     */
    public static String getConvertedEnumFromLongToEnum(Long value) {
        for (EmployeeStatus employeeStatus : EmployeeStatus.values()) {
            if (employeeStatus.getNumValue() == value) {
                return employeeStatus.toString();
            }
        }
        return "";
    }
}
