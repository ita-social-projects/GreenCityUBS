package greencity.service.ubs.point;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import greencity.dto.user.AllPointsUserDto;
import greencity.dto.user.PointsForUbsUserDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.user.User;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PointServiceImpl pointService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUuid("uuid-123");
    }

    @Test
    void findAllCurrentPointsForUser_userWithPointsAndHistory_success() {
        ChangeOfPoints change1 = new ChangeOfPoints();
        change1.setDate(LocalDateTime.now().minusDays(1));
        ChangeOfPoints change2 = new ChangeOfPoints();
        change2.setDate(LocalDateTime.now());

        user.setCurrentPoints(50);
        user.setChangeOfPointsList(List.of(change1, change2));

        PointsForUbsUserDto dto1 = new PointsForUbsUserDto();
        PointsForUbsUserDto dto2 = new PointsForUbsUserDto();
        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));
        when(modelMapper.map(change1, PointsForUbsUserDto.class)).thenReturn(dto1);
        when(modelMapper.map(change2, PointsForUbsUserDto.class)).thenReturn(dto2);

        AllPointsUserDto result = pointService.findAllCurrentPointsForUser("uuid-123");

        assertThat(result.getUserBonuses()).isEqualTo(50);
        assertThat(result.getUbsUserBonuses()).containsExactly(dto2, dto1);
    }

    @Test
    void findAllCurrentPointsForUser_userWithNullPoints_setsZero() {
        user.setCurrentPoints(null);
        user.setChangeOfPointsList(List.of());

        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));

        AllPointsUserDto result = pointService.findAllCurrentPointsForUser("uuid-123");

        assertThat(result.getUserBonuses()).isEqualTo(0);
    }

    @Test
    void findAllCurrentPointsForUser_userWithNullHistory_returnsEmptyList() {
        user.setCurrentPoints(10);
        user.setChangeOfPointsList(null);

        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.of(user));

        AllPointsUserDto result = pointService.findAllCurrentPointsForUser("uuid-123");

        assertThat(result.getUbsUserBonuses()).isEmpty();
    }

    @Test
    void findAllCurrentPointsForUser_userNotFound_throwsException() {
        when(userRepository.findUserByUuid("uuid-123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pointService.findAllCurrentPointsForUser("uuid-123"))
            .isInstanceOf(UserNotFoundException.class);
    }
}