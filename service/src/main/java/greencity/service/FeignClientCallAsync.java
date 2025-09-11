package greencity.service;

import greencity.client.UserRemoteClient;
import greencity.dto.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class FeignClientCallAsync {
    private final UserRemoteClient userRemoteClient;

    /**
     * {@inheritDoc}
     */
    @Async
    public CompletableFuture<Optional<UserVO>> getRecordsAsync(String email) {
        return CompletableFuture.completedFuture(userRemoteClient.findNotDeactivatedByEmail(email));
    }
}
