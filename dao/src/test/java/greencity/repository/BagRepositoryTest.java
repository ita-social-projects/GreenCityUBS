package greencity.repository;

import greencity.IntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BagRepositoryTest extends IntegrationTestBase {
    @Autowired
    private BagRepository bagRepository;

    @Test
    void findById(){
        Assertions.assertNull(bagRepository.findBagByOrderId(1L));
    }
}
