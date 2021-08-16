package greencity.client;

import greencity.dto.UserVO;
import greencity.security.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OutOfRequestRestClient {
    private static final String TOKEN_HEADER_NAME = "X-Viber-Auth-Token";
    private final RestTemplate restTemplate;
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    @Value("${ubs.viber.bot.token}")
    private String viberBotToken;
    @Value("${ubs.viber.bot.url}")
    private String viberBotUrl;
    @Autowired
    private JwtTool jwtTool;

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
}
