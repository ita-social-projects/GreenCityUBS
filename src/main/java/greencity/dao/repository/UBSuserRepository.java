package greencity.dao.repository;

import greencity.dao.entity.user.ubs.UBSuser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UBSuserRepository extends CrudRepository<UBSuser, Long> {

    @Query("SELECT u FROM UBSuser u LEFT JOIN FETCH u.userAddress address WHERE u.user.id = :userId")
    List<UBSuser> getAllByUserId(Long userId);
}
