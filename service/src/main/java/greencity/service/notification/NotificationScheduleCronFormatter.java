package greencity.service.notification;

import greencity.entity.schedule.NotificationSchedule;
import greencity.service.NotificationScheduleFormatterService;
import org.springframework.stereotype.Service;

@Service
public class NotificationScheduleCronFormatter implements NotificationScheduleFormatterService {
    /**
     * {@inheritDoc}
     */
    @Override
    public String format(NotificationSchedule ob) {
        return String.join(" ",
            checkNull(ob.getSeconds()),
            checkNull(ob.getMinutes()),
            checkNull(ob.getHours()),
            checkNull(ob.getDayOfMonth()),
            checkNull(ob.getMonth()),
            checkNull(ob.getDayOfWeek()),
            checkNull(ob.getYear())).trim();
    }

    private String checkNull(String value) {
        return null == value ? "*" : value;
    }
}
