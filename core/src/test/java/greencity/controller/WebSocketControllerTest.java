package greencity.controller;

import static org.mockito.Mockito.verify;
import greencity.service.ubs.TelegramUnreadCountNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    @Mock
    private TelegramUnreadCountNotifier telegramUnreadCountNotifier;

    @InjectMocks
    private WebSocketController controller;

    @Test
    void onSubscribe_invokesNotifier() {
        controller.onSubscribe();

        verify(telegramUnreadCountNotifier).notifyUnreadCount();
    }
}
