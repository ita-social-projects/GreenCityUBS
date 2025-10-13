package greencity.scheduler;

import greencity.service.ubs.payment.ProcessPaymentService;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@Slf4j
@RequiredArgsConstructor
public class PaymentExpiryJob implements Job {
    private final ProcessPaymentService processPaymentService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Long orderId = jobDataMap.getLong("orderId");
        int pointsUsed = jobDataMap.getInt("pointsUsed");
        @SuppressWarnings("unchecked")
        HashSet<String> certificateCodes = (HashSet<String>) jobDataMap.get("certificateCodes");

        log.info("Unlocking {} certificates and {} points from order {}",
            certificateCodes.size(), pointsUsed, orderId);
        processPaymentService.expirePaymentAttempt(orderId, pointsUsed, certificateCodes);
        log.info("Successfully unlocked {} certificates and {} points from order {}",
            certificateCodes.size(), pointsUsed, orderId);
    }
}
