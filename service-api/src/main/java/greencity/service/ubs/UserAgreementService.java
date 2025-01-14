package greencity.service.ubs;

import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import java.util.List;

/**
 * Service interface for managing user agreements.
 */
public interface UserAgreementService {
    /**
     * Retrieves the IDs of all {@link greencity.entity.user.UserAgreement} entities
     * sorted by creation date in ascending order.
     *
     * @return a list of IDs for all entities, sorted from oldest to newest
     */
    List<Long> findAllIdSortedByAsc();

    /**
     * Finds the latest user agreement.
     *
     * @return UserAgreementDetailDto of the latest user agreement
     */
    UserAgreementDto findLatest();

    /**
     * Creates a new user agreement.
     *
     * @param userAgreementDto DTO with user agreement details
     * @return UserAgreementDetailDto of the created user agreement
     */
    UserAgreementDetailDto create(UserAgreementDto userAgreementDto, String authorEmail);

    /**
     * Retrieves a user agreement by ID.
     *
     * @param id ID of the user agreement
     * @return UserAgreementDetailDto with user agreement details
     */
    UserAgreementDetailDto read(Long id);

    /**
     * Deletes a user agreement by ID.
     *
     * @param id ID of the user agreement to delete
     */
    void delete(Long id);
}
