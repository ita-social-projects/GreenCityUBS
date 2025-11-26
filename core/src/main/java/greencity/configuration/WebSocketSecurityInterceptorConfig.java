package greencity.configuration;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityInterceptorConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRemoteClient userRemoteClient;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");

                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    } else {
                        log.error("Websocket authentication failed. Token is null or doesn't start with Bearer");
                        throw new AccessDeniedException("Access denied");
                    }
                    ((ProviderManager) authenticationManager).setEraseCredentialsAfterAuthentication(false);
                    Authentication authentication = authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(token, null));
                    String uuid = userRepository.findUuidByRecipientEmail((String) authentication.getPrincipal())
                        .orElseThrow(
                            () -> new UserNotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
                    boolean exists = userRemoteClient.checkIfUserExistsByUuid(uuid);
                    if (!exists) {
                        log.error("Websocket authentication failed. User with uuid {} doesn't exist", uuid);
                        throw new AccessDeniedException("Access denied");
                    }
                    log.debug("User successfully authenticate with websocket - {}", authentication.getPrincipal());
                    accessor.setUser(authentication);
                }
                return message;
            }
        });
    }
}