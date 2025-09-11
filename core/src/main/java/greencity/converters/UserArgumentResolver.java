package greencity.converters;

import greencity.annotations.CurrentUserUuid;
import greencity.client.UserRemoteClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.security.Principal;

@Component
@AllArgsConstructor
@Slf4j
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Lazy
    @Autowired
    private UserRemoteClient userRemoteClient;

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
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        return principal != null ? userRemoteClient.findUuidByEmail(principal.getName()) : null;
    }
}
