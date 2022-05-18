package greencity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.maps.GeoApiContext;

// CHECKSTYLE:OFF
@Configuration
public class GoogleApiConfiguration {
    @Value("${greencity.authorization.googleApiKey}")
    private String GOOGLE_API_KEY;

    /**
     * Method create ApiContext.
     *
     * @return {@link GeoApiContext}
     */
    @Bean
    GeoApiContext context() {
        return new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build();
    }
}
