package greencity.service.ubs;

import greencity.dto.user.UserResponseDto;

import java.util.List;

public interface UserService {

    List<UserResponseDto> getAllUsers(int page, int size, String sort);

}
