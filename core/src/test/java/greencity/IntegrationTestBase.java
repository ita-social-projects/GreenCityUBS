package greencity;

import greencity.initializer.PostgresInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import jakarta.transaction.Transactional;

@ActiveProfiles("test")
@ContextConfiguration(initializers = {
    PostgresInitializer.Initializer.class
})
@Transactional
public abstract class IntegrationTestBase {

    @BeforeAll
    static void init() {
        PostgresInitializer.postgreSQLContainer.start();
    }
}
