package greencity.service.ubs;

import greencity.dto.PageableDto;
import greencity.dto.UserViolationsDto;
import greencity.dto.UserViolationsWithUserName;
import greencity.entity.enums.SortingOrder;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.repository.UserRepository;
import greencity.repository.UserViolationsTableRepo;
import greencity.repository.ViolationRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserViolationsServiceImpl implements UserViolationsService {
    private ViolationRepository violationRepository;
    private UserRepository userRepository;
    private UserViolationsTableRepo userViolationsTableRepo;

    @Override
    public UserViolationsWithUserName getAllViolations(Pageable page, Long userId, String columnName,
        SortingOrder sortingOrder) {
        String username = getUsername(userId);
        Long numberOfViolations = violationRepository.getNumberOfViolationsByUser(userId);

        Page<Violation> violationPage = userViolationsTableRepo.findAll(userId, columnName, sortingOrder, page);

        List<UserViolationsDto> userViolationsList =
            violationPage.getContent()
                .stream()
                .map(this::getAllViolations)
                .collect(Collectors.toList());

        return UserViolationsWithUserName.builder()
            .userViolationsDto(new PageableDto<>(userViolationsList, numberOfViolations,
                violationPage.getPageable().getPageNumber(), violationPage.getTotalPages()))
            .fullName(username)
            .build();
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
