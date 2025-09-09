package greencity.scheduler;

import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_CANCEL_EXCEPTION;

@Component
@DisallowConcurrentExecution
@Slf4j
@RequiredArgsConstructor
public class PaymentExpiryJob implements Job {
    private final UBSClientService ubsClientService;
    private final OrderRepository orderRepository;
    private final Scheduler quartzScheduler;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Long orderId = jobDataMap.getLong("orderId");
        int pointsUsed = jobDataMap.getInt("pointsUsed");
        @SuppressWarnings("unchecked")
        HashSet<String> certificateCodes = (HashSet<String>) jobDataMap.get("certificateCodes");

        log.info("Unlocking {} certificates and {} points from order {}",
            certificateCodes.size(), pointsUsed, orderId);
        Order order = ubsClientService.unlockSpecifiedPointsAndCertificatesFromOrder(
            orderId, pointsUsed, certificateCodes);
        order.setPaymentLink("");
        order.setPaymentLinkExpiry(null);
        orderRepository.save(order);
        try {
            quartzScheduler.deleteJob(jobExecutionContext.getJobDetail().getKey());
        } catch (SchedulerException e) {
            throw new IllegalStateException(PAYMENT_EXPIRY_CANCEL_EXCEPTION);
        }
        log.info("Successfully unlocked {} certificates and {} points from order {}",
            certificateCodes.size(), pointsUsed, orderId);
    }
}
