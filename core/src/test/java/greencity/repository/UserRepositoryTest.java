package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.user.User;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Sql(scripts = "/sqlFiles/userRepo/insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sqlFiles/userRepo/delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
@Ignore
class UserRepositoryTest extends IntegrationTestBase {

    @Autowired
    UserRepository userRepository;

    // @Test
    void findUserByOrderId() {
        User user = ModelUtils.getUser();
        Optional<User> actual = userRepository.findUserByOrderId(1L);

        Assertions.assertEquals(Optional.of(user.getRecipientName()), Optional.of(actual.get().getRecipientName()));
        Assertions.assertEquals(Optional.of(user.getRecipientSurname()),
            Optional.of(actual.get().getRecipientSurname()));
        Assertions.assertEquals(Optional.of(user.getRecipientEmail()), Optional.of(actual.get().getRecipientEmail()));
        Assertions.assertEquals(Optional.of(user.getRecipientPhone()), Optional.of(actual.get().getRecipientPhone()));
        Assertions.assertEquals(Optional.of(user.getCurrentPoints()), Optional.of(actual.get().getCurrentPoints()));
        Assertions.assertEquals(Optional.of(user.getViolations()), Optional.of(actual.get().getViolations()));
    }

    // @Test
    void findUserByUuid() {
        User user = ModelUtils.getUser();
        Optional<User> actual = userRepository.findUserByUuid("a3669bb0-842d-11ec-a8a3-0242ac120002");

        Assertions.assertEquals(Optional.of(user.getRecipientName()), Optional.of(actual.get().getRecipientName()));
        Assertions.assertEquals(Optional.of(user.getRecipientSurname()),
            Optional.of(actual.get().getRecipientSurname()));
        Assertions.assertEquals(Optional.of(user.getRecipientEmail()), Optional.of(actual.get().getRecipientEmail()));
        Assertions.assertEquals(Optional.of(user.getRecipientPhone()), Optional.of(actual.get().getRecipientPhone()));
        Assertions.assertEquals(Optional.of(user.getCurrentPoints()), Optional.of(actual.get().getCurrentPoints()));
        Assertions.assertEquals(Optional.of(user.getViolations()), Optional.of(actual.get().getViolations()));
    }

    // @Test
    void countTotalUsersViolations() {
        Assertions.assertEquals(0, userRepository.countTotalUsersViolations(1L));
    }

    // @Test
    void checkIfUserHasViolationForCurrentOrder() {
        Assertions.assertEquals(0,
            userRepository.checkIfUserHasViolationForCurrentOrder(1L, 1L));
    }

    // @Test
    void getAllInactiveUsers() {
        List<User> users = ModelUtils.getUsers();
        List<User> actual = userRepository.getAllInactiveUsers(LocalDate.of(2022, 1, 15), LocalDate.now());

        Assertions.assertEquals(users.get(0).getRecipientName(), actual.get(0).getRecipientName());
        Assertions.assertEquals(users.get(0).getRecipientSurname(), actual.get(0).getRecipientSurname());
        Assertions.assertEquals(users.get(0).getRecipientEmail(), actual.get(0).getRecipientEmail());
        Assertions.assertEquals(users.get(0).getRecipientPhone(), actual.get(0).getRecipientPhone());
    }
}
