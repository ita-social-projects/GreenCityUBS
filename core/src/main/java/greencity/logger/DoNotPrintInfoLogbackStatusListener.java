package greencity.logger;

import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.Status;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DoNotPrintInfoLogbackStatusListener extends OnConsoleStatusListener {
    private static final int LOG_LEVEL_WARN = Status.WARN;
    private static final int LOG_LEVEL_ERROR = Status.ERROR;

    @Override
    public void addStatusEvent(Status status) {
        if (status.getLevel() == LOG_LEVEL_WARN || status.getLevel() == LOG_LEVEL_ERROR) {
            super.addStatusEvent(status);
        }
    }

    @Override
    public void start() {
        final List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
        for (Status status : statuses) {
            if (status.getLevel() == LOG_LEVEL_WARN || status.getLevel() == LOG_LEVEL_ERROR) {
                super.start();
            }
        }
    }
}