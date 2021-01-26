package greencity.converters;


import greencity.annotations.CurrentUserId;
import greencity.client.RestClient;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.security.Principal;

@Component
@AllArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private RestClient restClient;

    /**
     * Method checks if parameter is {@link Long} and is annotated with
     * {@link CurrentUserId}.
     *
     * @param parameter method parameter
     * @return boolean
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUserId.class) != null
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Principal principal = webRequest.getUserPrincipal();
        return principal != null ? restClient.findIdByEmail(principal.getName()) : null;
    }
}
