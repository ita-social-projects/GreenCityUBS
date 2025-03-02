package greencity.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {
    /**
     *
     * {@inheritDoc}
     */
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // set the core pool size
        executor.setMaxPoolSize(10); // max pool size
        executor.setThreadNamePrefix("ext-async-"); // give an optional name to your threads
        executor.initialize();
        return executor;
    }
}