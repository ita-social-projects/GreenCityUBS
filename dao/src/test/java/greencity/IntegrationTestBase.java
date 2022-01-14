package greencity;


import greencity.initializer.PostgersInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;


@ActiveProfiles("test")
@ContextConfiguration(initializers = {
        PostgersInitializer.Initializer.class
})
@Transactional
public abstract class IntegrationTestBase {

    @BeforeAll
    static void init() {
        PostgersInitializer.postgreSQLContainer.start();
    }
}
