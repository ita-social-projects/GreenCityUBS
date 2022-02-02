package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Sql(scripts = "sqlFiles/additionalBagsInfoRepo/insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "sqlFiles/additionalBagsInfoRepo/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
class AdditionalBagsInfoRepoTest extends IntegrationTestBase {
    @Autowired
    AdditionalBagsInfoRepo additionalBagsInfoRepo;

    @Test
    void findById() {
        List<Map<String, Object>> expected = new ArrayList<>();
        Assertions.assertEquals(expected, additionalBagsInfoRepo.getAdditionalBagInfo(1L, "test@mail.com"));
    }
}
