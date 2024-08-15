package greencity.service.ubs;

import greencity.dto.pageble.PageableDto;
import greencity.dto.user.UserAgreementDetailDto;
import greencity.dto.user.UserAgreementDto;
import org.springframework.data.domain.Pageable;

public interface UserAgreementService {

    /**
     * Finds all user agreements with pagination.
     *
     * @return PageableDto containing a list of UserAgreementDetailDto
     */
    PageableDto<UserAgreementDetailDto> findAll(Pageable pageable);

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
    UserAgreementDetailDto create(UserAgreementDto userAgreementDto);

    /**
     * Retrieves a user agreement by ID.
     *
     * @param id ID of the user agreement
     * @return UserAgreementDetailDto with user agreement details
     */
    UserAgreementDetailDto read(Long id);

    /**
     * Updates an existing user agreement.
     *
     * @param id               ID of the user agreement to update
     * @param userAgreementDto DTO with updated user agreement details
     * @return UserAgreementDetailDto of the updated user agreement
     */
    UserAgreementDetailDto update(Long id, UserAgreementDto userAgreementDto);

    /**
     * Deletes a user agreement by ID.
     *
     * @param id ID of the user agreement to delete
     */
    void delete(Long id);
}
