package greencity.client;

import greencity.dto.user.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "user-service", url = "${greencity.redirect.user-server-address}")
public interface UserClient {
    @GetMapping("/user/findAll")
    List<UserResponseDto> getAllUsers(@RequestParam int page, @RequestParam int size, @RequestParam String sort);
}
