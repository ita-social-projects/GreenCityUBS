package greencity.client;

import greencity.dto.*;
import greencity.constant.RestTemplateLinks;
import greencity.dto.UbsTableCreationDto;
import greencity.dto.UserViolationMailDto;
import greencity.dto.viber.dto.SendMessageToUserDto;
import greencity.dto.viber.enums.EventTypes;
import greencity.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestClient {
    private static final String TOKEN_HEADER_NAME = "X-Viber-Auth-Token";
    private final RestTemplate restTemplate;
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    @Value("${ubs.viber.bot.token}")
    private String viberBotToken;
    @Value("${ubs.viber.bot.url}")
    private String viberBotUrl;
    private final HttpServletRequest httpServletRequest;

    /**
     * Method return intermediate response from fondy (redirect on payment page).
     *
     * @param dto of {@link User}
     * @author Volodymyr Hutei
     */
    public String getDataFromFondy(PaymentRequestDto dto) {
        JSONObject request = new JSONObject();
        request.put("amount", dto.getAmount());
        request.put("currency", dto.getCurrency());
        request.put("order_desc", dto.getOrderDescription());
        request.put("order_id", dto.getOrderId());
        request.put("merchant_id", dto.getMerchantId());
        request.put("signature", dto.getSignature());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);

        return restTemplate.exchange(RestTemplateLinks.FONDY_LINK, HttpMethod.POST, entity,
            String.class).getBody();
    }

    /**
     * Method find user id by email.
     *
     * @param email of {@link User}
     * @author Orest Mamchuk
     */
    public Long findIdByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + "/user/findIdByEmail" + "?email=" + email, HttpMethod.GET, entity, Long.class)
            .getBody();
    }

    /**
     * Method finds uuid for ubs record creation.
     *
     * @return {@link UbsTableCreationDto} containing uuid.
     * @author Oleh Bilonizhka
     */
    public UbsTableCreationDto getDataForUbsTableRecordCreation() {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UbsTableCreationDto dto = restTemplate.exchange(greenCityUserServerAddress
            + "/user/createUbsRecord", HttpMethod.GET, entity, UbsTableCreationDto.class).getBody();
        assert dto != null;
        return dto;
    }

    /**
     * Method finds user uuid by email.
     *
     * @param email of {@link User}.
     */
    public String findUuidByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        return restTemplate.exchange(greenCityUserServerAddress
            + "/user/findUuidByEmail" + "?email=" + email, HttpMethod.GET, entity, String.class)
            .getBody();
    }

    /**
     * Updates last activity time for a given user.
     *
     * @param userId               - {@link User}'s id
     * @param userLastActivityTime - new {@link User}'s last activity time
     * @author Orest Mamchuk
     */
    public void updateUserLastActivityTime(Long userId, Date userLastActivityTime) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSSSS");
        String strDate = dateFormat.format(userLastActivityTime);
        restTemplate.exchange(greenCityUserServerAddress + "/user/"
            + userId + "/updateUserLastActivityTime/" + strDate,
            HttpMethod.PUT, entity, Object.class);
    }

    /**
     * Method makes headers for RestTemplate.
     *
     * @return {@link HttpEntity}
     */
    private HttpHeaders setHeader() {
        String accessToken = httpServletRequest.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        return headers;
    }

    /**
     * The method makes headers for communication with the Viber bot.
     *
     * @return {@link HttpEntity}
     */
    private HttpHeaders setHeadersForViberBot() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(TOKEN_HEADER_NAME, viberBotToken);
        return httpHeaders;
    }

    /**
     * The method sets the URL of the Viber bot to which Viber requests will be
     * sent.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    public ResponseEntity<String> getAccountInfo() {
        String jsonString = new JSONObject().toString();
        HttpEntity<String> entity = new HttpEntity<>(jsonString, setHeadersForViberBot());
        return restTemplate.exchange(RestTemplateLinks.GET_ACCOUNT_INFO, HttpMethod.POST, entity, String.class);
    }

    /**
     * The method removes Viber bot url.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    public ResponseEntity<String> removeWebHook() {
        String jsonString = new JSONObject()
            .put("url", "").toString();
        HttpEntity<String> entity = new HttpEntity<>(jsonString, setHeadersForViberBot());
        return restTemplate.exchange(RestTemplateLinks.SET_WEBHOOK, HttpMethod.POST, entity, String.class);
    }

    /**
     * The method sets the URL of the Viber bot to which Viber requests will be
     * sent.
     *
     * @return {@link String} - which contains the status of success or failure.
     */
    public ResponseEntity<String> setWebhook() {
        String jsonString = new JSONObject()
            .put("url", viberBotUrl)
            .put("event_types", new EventTypes[] {EventTypes.delivered, EventTypes.seen,
                EventTypes.failed, EventTypes.subscribed, EventTypes.unsubscribed, EventTypes.conversation_started})
            .toString();

        HttpEntity<String> entity = new HttpEntity<>(jsonString, setHeadersForViberBot());
        return restTemplate.exchange(RestTemplateLinks.SET_WEBHOOK, HttpMethod.POST, entity, String.class);
    }

    /**
     * The method sends a welcome message to user and is performed pre-registration
     * of user.
     *
     * @param sendMessageToUserDto {@link SendMessageToUserDto}
     * @return @return {@link String} - which contains the status of success or
     *         failure.
     */
    public ResponseEntity<String> sendWelcomeMessage(SendMessageToUserDto sendMessageToUserDto) {
        HttpEntity<SendMessageToUserDto> entity = new HttpEntity<>(sendMessageToUserDto, setHeadersForViberBot());
        return restTemplate.exchange(RestTemplateLinks.SEND_MESSAGE, HttpMethod.POST, entity, String.class);
    }

    /**
     * The method sends a message to user and is performed registration of user.
     *
     * @param sendMessageToUserDto {@link SendMessageToUserDto}
     * @return @return @return {@link String} - which contains the status of success
     *         or failure.
     */
    public ResponseEntity<String> sentMessage(SendMessageToUserDto sendMessageToUserDto) {
        HttpEntity<SendMessageToUserDto> entity = new HttpEntity<>(sendMessageToUserDto, setHeadersForViberBot());
        return restTemplate.exchange(RestTemplateLinks.SEND_MESSAGE, HttpMethod.POST, entity, String.class);
    }

    /**
     * Method that allow you to find not 'DEACTIVATED' {@link UserVO} by email.
     *
     * @param email - {@link UserVO}'s email
     * @return {@link UserVO}
     * @author Orest Mamchuk
     */
    public Optional<UserVO> findNotDeactivatedByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UserVO body = restTemplate.exchange(greenCityUserServerAddress
            + "user/findNotDeactivatedByEmail" + "?email="
            + email, HttpMethod.GET, entity, UserVO.class)
            .getBody();
        assert body != null;
        return Optional.of(body);
    }

    /**
     * Method that allow you to find User by uuid {@link UbsCustomersDto}.
     *
     * @param uuid - {@link UbsCustomersDto}'s uuid
     * @return {@link UbsCustomersDto}
     * @author Nazar Struk
     */
    public Optional<UbsCustomersDto> findUserByUUid(String uuid) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader());
        UbsCustomersDto body = restTemplate.exchange(greenCityUserServerAddress + "user/findByUuId"
            + "?uuid=" + uuid, HttpMethod.GET, entity, UbsCustomersDto.class).getBody();
        assert body != null;
        return Optional.of(body);
    }

    /**
     * Method that send request to greenCityUser part for sending email.
     *
     * @param message {@link UserViolationMailDto} contain information about
     *                violation.
     * @author Veremchuk Zakhar.
     */
    public void sendViolationOnMail(UserViolationMailDto message) {
        HttpHeaders httpHeaders = new HttpHeaders(setHeader());
        HttpEntity<UserViolationMailDto> entity = new HttpEntity<>(message, httpHeaders);
        restTemplate.exchange(greenCityUserServerAddress + "/email/sendUserViolation",
            HttpMethod.POST, entity, Object.class).getBody();
    }
}
