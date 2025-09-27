package greencity;

import greencity.initializer.PostgresInitializer;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@ContextConfiguration(initializers = {
    PostgresInitializer.Initializer.class
})
@Transactional
public abstract class IntegrationTestBase {

    @BeforeAll
    static void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kyiv"));
        PostgresInitializer.postgreSQLContainer.start();
    }
}
