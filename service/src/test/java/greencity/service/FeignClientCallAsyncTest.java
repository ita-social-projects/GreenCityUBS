package greencity.service;

import greencity.client.UserRemoteClient;
import greencity.dto.user.UserVO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeignClientCallAsyncTest {
    @Mock
    private UserRemoteClient userRemoteClient;

    @Test
    public void testGetRecordsAsync() throws Exception {
        String email = "example@example.com";
        UserVO userVO = new UserVO();
        when(userRemoteClient.findNotDeactivatedByEmail(email)).thenReturn(Optional.of(userVO));
        FeignClientCallAsync feignClient = new FeignClientCallAsync(userRemoteClient);
        CompletableFuture<Optional<UserVO>> result = feignClient.getRecordsAsync(email);
        Optional<UserVO> userResult = result.get();

        assertEquals(Optional.of(userVO), userResult);
    }
}
