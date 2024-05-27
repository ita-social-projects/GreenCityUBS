package greencity.notificator.scheduler;

import greencity.constant.AppConstant;
import greencity.enums.NotificationType;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationTaskScheduler {
    private final TaskScheduler taskScheduler;

    public ScheduledFuture<?> scheduleNotification(
        Runnable task, String schedule, NotificationType notificationType) {
        return scheduleTask(task, schedule, notificationType);
    }

    private ScheduledFuture<?> scheduleTask(Runnable task, String schedule, NotificationType notificationType) {
        ScheduledFuture<?> scheduledFuture = null;
        if (isExpressionCorrect(schedule, notificationType)) {
            scheduledFuture = taskScheduler.schedule(task, new CronTrigger(schedule));
            log.info(AppConstant.NOTIFICATOR_SUCCESSFULLY_START_LOG_MESSAGE, notificationType, schedule);
        }
        return scheduledFuture;
    }

    private boolean isExpressionCorrect(String schedule, NotificationType notificationType) {
        try {
            CronExpression.parse(schedule);
            return true;
        } catch (Exception e) {
            log.info(AppConstant.NOTIFICATOR_START_IS_FAILED_LOG_MESSAGE, notificationType, schedule);
            return false;
        }
    }
}