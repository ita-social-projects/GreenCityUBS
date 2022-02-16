package greencity.entity.enums;

public enum LocationStatus {
    ACTIVE("Активно"), DEACTIVATED("Неактивно");

    private String status;

    LocationStatus() {
    }

    LocationStatus(String status) {
        this.status = status;
    }

    /**
     * This is method get status value.
     *
     * @return {@link LocationStatus}.
     */
    public String getStatus() {
        return status;
    }
}
