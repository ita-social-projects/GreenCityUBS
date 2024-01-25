package greencity.service.ubs;

import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.UserViolationsWithUserName;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.enums.SortingOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

public interface ViolationService {
    /**
     * Method returns all violations by userId.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Roman Sulymka
     */
    UserViolationsWithUserName getAllViolations(Pageable page, Long userId, String columnName,
        SortingOrder sortingOrder);

    /**
     * Method for adding violation for user.
     *
     * @param add            {@link AddingViolationsToUserDto}
     * @param multipartFiles {@link MultipartFile}
     * @param email          {@link String}.
     * @author Nazar Struk
     */
    void addUserViolation(AddingViolationsToUserDto add, MultipartFile[] multipartFiles, String email);

    /**
     * Method returns detailed information about user violation by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ViolationDetailInfoDto};
     * @author Rusanovscaia Nadejda
     */
    Optional<ViolationDetailInfoDto> getViolationDetailsByOrderId(Long orderId);

    /**
     * Method deletes violation from database by orderId.
     *
     * @param orderId {@link Long}
     * @param uuid    {@link String}.
     * @author Nadia Rusanovscaia
     */
    void deleteViolation(Long orderId, String uuid);

    /**
     * Method for adding violation for user.
     *
     * @param add            {@link AddingViolationsToUserDto}
     * @param multipartFiles {@link MultipartFile}
     * @author Bohdan Melnyk
     */
    void updateUserViolation(UpdateViolationToUserDto add, MultipartFile[] multipartFiles, String uuid);
}
