package greencity.entity.schedule;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public class Schedule {
    private String seconds = "0";
    private String minutes = "0";
    private String hours = "18";
    private String dayOfMonth = "*";
    private String month = "*";
    private String dayOfWeek = "?";
    private String year = "";
}
