package greencity.repository;

import greencity.entity.user.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {
    @Query("SELECT ua FROM UserAgreement ua ORDER BY ua.createdAt DESC LIMIT 1")
    Optional<UserAgreement> findLatestAgreement();
}
