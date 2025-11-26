package greencity.configuration;

import greencity.constant.AppConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    @Override
    protected void configureInbound(final MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpSubscribeDestMatchers("/topic/chats/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .simpSubscribeDestMatchers("/topic/messages/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .simpSubscribeDestMatchers("/topic/unread/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
