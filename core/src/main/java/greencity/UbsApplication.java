package greencity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableFeignClients
public class UbsApplication {
    /**
     * Main method of SpringBoot app.
     */
    public static void main(String[] args) {
        SpringApplication.run(UbsApplication.class, args);
    }

    /**
     * Bean to return RestTemplate.
     *
     * @return {@link RestTemplate}.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
