package greencity.scheduler;

import static greencity.ModelUtils.getOrder;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_GROUP;
import static greencity.constant.QuartzConstants.PAYMENT_EXPIRY_JOB_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentExpiryJobTest {
    @Mock
    private UBSClientService ubsClientService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Scheduler quartzScheduler;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDetail jobDetail;

    @InjectMocks
    private PaymentExpiryJob paymentExpiryJob;

    @Test
    void executePaymentExpiryJobTest() {
        Order order = getOrder();
        order.setPaymentLink("testInvoice");
        order.setPaymentLinkExpiry(LocalDateTime.now());
        Long orderId = order.getId();
        Integer pointsToUse = order.getPointsToUse();

        HashSet<String> certificateCodes = new HashSet<>(List.of("1111-1111"));

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("orderId", orderId);
        jobDataMap.put("pointsUsed", pointsToUse);
        jobDataMap.put("certificateCodes", certificateCodes);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(ubsClientService.unlockSpecifiedPointsAndCertificatesFromOrder(orderId, pointsToUse, certificateCodes))
            .thenReturn(order);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getKey()).thenReturn(JobKey.jobKey(PAYMENT_EXPIRY_JOB_KEY + orderId, PAYMENT_EXPIRY_JOB_GROUP));

        paymentExpiryJob.execute(jobExecutionContext);

        assertEquals("", order.getPaymentLink());
        assertNull(order.getPaymentLinkExpiry());

        verify(ubsClientService).unlockSpecifiedPointsAndCertificatesFromOrder(orderId, pointsToUse, certificateCodes);
        verify(orderRepository).save(order);
    }
}
