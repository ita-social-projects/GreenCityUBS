package greencity.security;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component(value = "preAuthorizer")
@Slf4j
public class PreAuthorizer {
    /**
     * Method for authorising employee.
     */
    public boolean hasAuthority(String authority, Authentication authentication) {
        List<String> authorities = (List<String>) authentication.getCredentials();
        if (authorities.contains(authority)) {
            return true;
        } else {
            log.warn("User: \"{}\" doesn't have \'{}\' authority.", authentication.getPrincipal(), authority);
            return false;
        }
    }
}
