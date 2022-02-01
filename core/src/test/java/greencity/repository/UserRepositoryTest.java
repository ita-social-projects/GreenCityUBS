package greencity.repository;

import greencity.IntegrationTestBase;
import greencity.UbsApplication;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import greencity.entity.user.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = UbsApplication.class)
public class UserRepositoryTest extends IntegrationTestBase {

    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderRepository orderRepository;

    @Test
    void findUserByOrderId() {
        // Only this test fails
        Order order = ModelUtils.getOrder();
        User user = ModelUtils.getUser();
        userRepository.save(user);
        order.setUser(user);
        orderRepository.save(order);
        Optional<User> actual = userRepository.findUserByOrderId(order.getId());
        Assertions.assertEquals(Optional.of(order.getUser()), actual);
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void findUserByUuid() {
        User user = ModelUtils.getUser();
        userRepository.save(user);
        Assertions.assertEquals(Optional.of(user), userRepository.findUserByUuid("uuid"));
        userRepository.deleteAll();
    }

    @Test
    void countTotalUsersViolations() {
        User user = ModelUtils.getUser();
        userRepository.save(user);
        Assertions.assertEquals(0, userRepository.countTotalUsersViolations(user.getId()));
        userRepository.deleteAll();
    }

    @Test
    void checkIfUserHasViolationForCurrentOrder() {
        User user = ModelUtils.getUser();
        userRepository.save(user);
        Assertions.assertEquals(0,
            userRepository.checkIfUserHasViolationForCurrentOrder(user.getId(), user.getOrders().get(0).getId()));
        userRepository.deleteAll();
    }

    @Test
    void getAllInactiveUsers() {
        List<User> users = new ArrayList<>();
        userRepository.saveAll(users);
        Assertions.assertEquals(users, userRepository.getAllInactiveUsers(LocalDate.of(2022, 1, 15), LocalDate.now()));
        userRepository.deleteAll();
    }
}
