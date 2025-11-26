package greencity.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpSubscribeDestMatchers("/topic/chats/**").hasRole("UBS_EMPLOYEE")
            .simpSubscribeDestMatchers("/topic/messages/**").hasRole("UBS_EMPLOYEE")
            .simpSubscribeDestMatchers("/topic/unread/**").hasRole("UBS_EMPLOYEE")
            .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
