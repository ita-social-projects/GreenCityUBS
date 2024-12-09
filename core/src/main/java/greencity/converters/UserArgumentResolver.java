package greencity.converters;

import greencity.annotations.CurrentUserUuid;
import greencity.client.UserRemoteClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRemoteClient userRemoteClient;

    public UserArgumentResolver(@Lazy UserRemoteClient userRemoteClient) {
        this.userRemoteClient = userRemoteClient;
    }

    /**
     * Method checks if parameter is {@link Long} and is annotated with
     * {@link CurrentUserUuid}.
     *
     * @param parameter method parameter
     * @return boolean
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUserUuid.class) != null
            && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        return principal != null ? userRemoteClient.findUuidByEmail(principal.getName()) : null;
    }
}
