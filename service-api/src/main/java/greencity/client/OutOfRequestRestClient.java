package greencity.client;

import greencity.dto.NotificationDto;
import greencity.dto.UserVO;
import greencity.security.JwtTool;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OutOfRequestRestClient {
    private final RestTemplate restTemplate;
    @Value("${greencity.user-server-address}")
    @Setter
    private String greenCityUserServerAddress;
    @Autowired
    private JwtTool jwtTool;

    /**
     * Find user by email.
     */
    public Optional<UserVO> findUserByEmail(String email) {
        HttpEntity<String> entity = new HttpEntity<>(setHeader(email));
        UserVO body = restTemplate.exchange(greenCityUserServerAddress
            + "/user/findByEmail" + "?email="
            + email, HttpMethod.GET, entity, UserVO.class)
            .getBody();
        return Optional.ofNullable(body);
    }

    private HttpHeaders setHeader(String email) {
        String accessToken = jwtTool.createAccessToken(email, 1);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    /**
     * Send notification via email.
     */
    public void sendEmailNotification(NotificationDto notification, String email) {
        HttpHeaders httpHeaders = setHeader(email);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
        HttpEntity<NotificationDto> entity = new HttpEntity<>(notification, httpHeaders);
        restTemplate.exchange(greenCityUserServerAddress
            + "/email/notification",
            HttpMethod.POST, entity, NotificationDto.class);
    }
}
