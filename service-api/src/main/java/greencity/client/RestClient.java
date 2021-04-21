package greencity.client;

import greencity.dto.UbsCustomersDto;
import greencity.dto.UbsTableCreationDto;
import greencity.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import greencity.dto.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestClient {
    private final RestTemplate restTemplate;
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    private final HttpServletRequest httpServletRequest;

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
}
