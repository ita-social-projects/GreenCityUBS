package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
public class BagRepositoryTest extends IntegrationTestBase {
    @Autowired
    private BagRepository bagRepository;

    @Test
    void findById() {
        Assertions.assertNull(bagRepository.findBagByOrderId(1L));
    }
}
