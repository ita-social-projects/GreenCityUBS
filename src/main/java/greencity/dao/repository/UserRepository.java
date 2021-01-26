package greencity.dao.repository;

import greencity.dao.entity.user.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
