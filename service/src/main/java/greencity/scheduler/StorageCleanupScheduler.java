package greencity.scheduler;

import greencity.service.files.StorageCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageCleanupScheduler {
    private final StorageCleanupService storageCleanupService;

    @Scheduled(cron = "0 0 3 ? * SAT")
    public void scheduleStorageCleanup() {
        storageCleanupService.cleanUp();
    }
}
