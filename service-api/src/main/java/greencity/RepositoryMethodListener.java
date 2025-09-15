package greencity;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class RepositoryMethodListener {
    private final Map<String, Long> methodNameToSumExecutionTimeSpeed = new HashMap<>();

    @Around("execution(* greencity.repository..*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            long executionTimeSum = executionTime + methodNameToSumExecutionTimeSpeed.getOrDefault(methodName, 0L);
            methodNameToSumExecutionTimeSpeed.put(methodName, executionTimeSum);

            Map<String, Long> sorted = methodNameToSumExecutionTimeSpeed.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            log.info("Repository: {}, method: {}, execution time: {}", className, methodName, executionTime);
            log.info("Execution time speed map: {}", sorted);
        }
    }
}
