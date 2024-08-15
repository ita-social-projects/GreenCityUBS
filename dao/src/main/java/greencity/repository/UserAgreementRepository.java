package greencity.repository;

import greencity.entity.user.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for {@link UserAgreement} entity. Provides methods to
 * perform CRUD operations on {@link UserAgreement} and additional custom
 * queries.
 */
@Repository
public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {
    /**
     * Retrieves the most recent UserAgreement based on the created date. Orders by
     * creation date in descending order and limits the result to one record.
     *
     * @return an {@link Optional} containing the latest {@link UserAgreement} if it
     *         exists, otherwise empty
     */
    @Query("SELECT ua FROM UserAgreement ua ORDER BY ua.createdAt DESC LIMIT 1")
    Optional<UserAgreement> findLatestAgreement();
}
