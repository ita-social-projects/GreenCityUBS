package greencity.optimization;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class ExecutionTimeAspect {
	@Around("@annotation(greencity.optimization.MeasureExecutionTime)")
	public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.nanoTime();
		Object proceed = joinPoint.proceed();
		long endTime = System.nanoTime();
		double duration = (endTime - startTime) / 1_000_000_000.0;

		log.info("Duration of method execution: 'getParametersForOrdersTable' : {} seconds", duration);

		return proceed;
	}
}
