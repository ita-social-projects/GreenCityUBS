package greencity.controller;

import greencity.service.ubs.TelegramUnreadCountNotifier;
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final TelegramUnreadCountNotifier telegramUnreadCountNotifier;

    /**
     * Handles client subscriptions to the "/app/unread" destination.
     *
     * <p>
     * When a client subscribes, this method triggers the notifier service to
     * calculate and broadcast the current unread message count to all subscribers
     * via the WebSocket message broker.
     * </p>
     */
    @AsyncListener(
        operation = @AsyncOperation(
            channelName = "/app/unread",
            description = "Subscription endpoint that triggers initial unread message count broadcast"))
    @StompAsyncOperationBinding
    @SubscribeMapping("/unread")
    public void onSubscribe() {
        telegramUnreadCountNotifier.notifyUnreadCount();
    }
}
