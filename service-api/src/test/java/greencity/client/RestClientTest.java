package greencity.client;

import greencity.ModelUtils;
import greencity.dto.UserVO;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RestClientTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private java.lang.Object Object;
    @Value("${greencityuser.server.address}")
    private String greenCityUserServerAddress;
    @InjectMocks
    private RestClient restClient;
    private final String AUTHORIZATION = "Authorization";

    @Test
    void findByEmail() {
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress +
            "/user/findIdByEmail" + "?email=" + "taras@gmail.com", HttpMethod.GET,
            entity, Long.class)).thenReturn(ResponseEntity.ok(13L));

        assertEquals(13L, restClient.findIdByEmail("taras@gmail.com"));
    }

    @Test
    void findNotDeactivatedByEmail() {
        String email = "test@gmail.com";
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        UserVO userVO = ModelUtils.getUserVO();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress
            + "user/findNotDeactivatedByEmail" + "?email="
            + email, HttpMethod.GET, entity, UserVO.class)).thenReturn(ResponseEntity.ok(userVO));

        assertEquals(Optional.of(userVO), restClient.findNotDeactivatedByEmail(email));
    }

    @Test
    void updateUserLastActivityTime() {
        Date date1 = new Date();
        String accessToken = "accessToken";
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION, accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSSSS");
        String strDate = dateFormat.format(date1);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(accessToken);
        when(restTemplate.exchange(greenCityUserServerAddress + "/user/" + 13L
            + "/updateUserLastActivityTime/"
            + strDate, HttpMethod.PUT, entity, Object.class)).thenReturn(ResponseEntity.ok(Object));

        restClient.updateUserLastActivityTime(13L, date1);
        verify(restTemplate).exchange(greenCityUserServerAddress + "/user/" + 13L
            + "/updateUserLastActivityTime/"
            + strDate, HttpMethod.PUT, entity, Object.class);
    }

}