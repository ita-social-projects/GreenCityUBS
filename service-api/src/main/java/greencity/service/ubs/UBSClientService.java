package greencity.service.ubs;

import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import greencity.entity.user.User;
import java.util.List;

public interface UBSClientService {
    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
    UserPointsAndAllBagsDto getFirstPageData(String uuid);

    /**
     * Methods returns all saved user data.
     *
     * @param uuid current {@link User}'s uuid.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    List<PersonalDataDto> getSecondPageData(String uuid);

    /**
     * Methods return status of entered certificate, empty string if absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto} which contains status.
     * @author Oleh Bilonizhka
     */
    CertificateDto checkCertificate(String code);

    /**
     * Methods saves all entered by user data to database.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @author Oleh Bilonizhka
     */
    void saveFullOrderToDB(OrderResponseDto dto, String uuid);
}
