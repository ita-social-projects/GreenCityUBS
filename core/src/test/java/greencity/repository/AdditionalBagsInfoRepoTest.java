package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
class AdditionalBagsInfoRepoTest extends IntegrationTestBase {
    @Autowired
    BagRepository additionalBagsInfo;

    // @Test
    void findById() {
        List<Map<String, Object>> expected = new ArrayList<>();
        Assertions.assertEquals(expected, additionalBagsInfo.getAdditionalBagInfo(1L, "test@mail.com"));
    }
}
