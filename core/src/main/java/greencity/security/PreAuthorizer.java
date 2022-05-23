package greencity.security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.List;

@Component(value="preAuthorizer")
public class PreAuthorizer{
    Logger log = LoggerFactory.getLogger(PreAuthorizer.class);
    public boolean authoriseEmployee(String authority, Authentication authentication) {
        List<String> authorities = (List<String>)authentication.getCredentials();
        if(authorities.contains(authority)){
            return true;
        }else {
            log.warn("User: \"{}\" doesn't have \'{}\' authority.",authentication.getPrincipal(), authority);
            return false;
        }
    }
}
