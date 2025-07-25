package greencity.scheduler;

import greencity.service.ubs.UBSClientService;
import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class OrderExpiryJob implements Job {
    private final UBSClientService ubsClientService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Long orderId = jobDataMap.getLong("orderId");
        int pointsUsed = jobDataMap.getInt("pointsUsed");
        @SuppressWarnings("unchecked")
        HashSet<String> certificateCodes = (HashSet<String>) jobDataMap.get("certificateCodes");

        ubsClientService.unlockSpecifiedPointsAndCertificatesFromOrder(orderId, pointsUsed, certificateCodes);
    }
}
