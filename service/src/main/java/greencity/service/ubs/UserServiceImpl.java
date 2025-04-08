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

    private static final String ASC = "asc";
    private static final String DESC = "desc";

    private final UserClient userClient;

    public UserServiceImpl(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public List<UserResponseDto> getAllUsers(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            sort = "id,desc";
        }

        String[] sortParams = sort.split(",");
        if (sortParams.length != 2) {
            throw new IllegalArgumentException("Sort parameter must be in the format 'field,direction'");
        }

        String fieldName = sortParams[0].trim();
        String direction = sortParams[1].trim().toLowerCase();

        if (!direction.equals(ASC) && !direction.equals(DESC)) {
            throw new IllegalArgumentException("Sort direction must be either 'asc' or 'desc'");
        }

        Sort.Direction sortDirection = direction.equals(ASC) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, fieldName));

        return userClient.getAllUsers(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
