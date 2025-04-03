package greencity.service.ubs;

import greencity.client.UserClient;
import greencity.dto.user.UserResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserClient userClient;

    public UserServiceImpl(UserClient userClient) {
        this.userClient = userClient;
    }

    public List<UserResponseDto> getAllUsers(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String fieldName = sortParams[0];
        String direction = sortParams[1];

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, fieldName));

        return userClient.getAllUsers(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
