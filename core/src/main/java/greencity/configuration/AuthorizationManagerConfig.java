package greencity.configuration;

import greencity.constant.AppConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
public class AuthorizationManagerConfig {
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages =
            MessageMatcherDelegatingAuthorizationManager.builder();

        messages
            .simpDestMatchers("/topic/chats/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .simpDestMatchers("/topic/messages/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .simpDestMatchers("/topic/unread/**").hasRole(AppConstant.UBS_EMPLOYEE)
            .anyMessage().authenticated();

        return messages.build();
    }
}
