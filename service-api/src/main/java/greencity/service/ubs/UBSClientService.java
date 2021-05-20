package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.user.User;

import java.util.List;

public interface UBSClientService {
    /**
     * Method validates received payment response.
     *
     * @param dto {@link PaymentResponseDto} - response order data.
     */
    void validatePayment(PaymentResponseDto dto);

    /**
     * Methods returns all available for order bags and current user's bonus points.
     *
     * @param uuid current {@link User}'s uuid.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
    UserPointsAndAllBagsDto getFirstPageData(String uuid, String language);

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
     * @return {@link PaymentRequestDto} which contains data to pay order out.
     * @author Oleh Bilonizhka
     */
    String saveFullOrderToDB(OrderResponseDto dto, String uuid);

    /**
     * Methods form payment page for LiqPay system.
     *
     * @param requestDto {@link PaymentRequestDto} request order data;
     * @return {@link String} html payment page.
     * @author Volodymyr Hutei
     */
    String formLiqPayPage(PaymentRequestDto requestDto);
}
