package greencity.service.ubs;

import greencity.dto.UserViolationsDto;
import greencity.dto.UserWithViolationsDto;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.repository.UserRepository;
import greencity.repository.ViolationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserViolationsServiceImpl implements UserViolationsService {
    private ViolationRepository violationRepository;
    private UserRepository userRepository;

    @Override
    public UserWithViolationsDto getAllViolations(Long userId) {
        String username = getUsername(userId);
        Long numberOfViolations = violationRepository.getNumberOfViolationsByUser(userId);
        List<UserViolationsDto> userViolationsList;
        userViolationsList = violationRepository
            .getAllViolationsByUserId(userId)
            .stream()
            .map(this::getAllViolations)
            .collect(Collectors.toList());
        return new UserWithViolationsDto(username, numberOfViolations, userViolationsList);
    }

    private UserViolationsDto getAllViolations(Violation violation) {
        return UserViolationsDto.builder()
            .violationDate(violation.getViolationDate())
            .orderId(violationRepository.getOrderIdByViolationId(violation.getId()))
            .violationLevel(violation.getViolationLevel())
            .build();
    }

    private String getUsername(Long userID) {
        User currentUser = userRepository.getOne(userID);
        return currentUser.getRecipientName() + " " + currentUser.getRecipientSurname();
    }
}
