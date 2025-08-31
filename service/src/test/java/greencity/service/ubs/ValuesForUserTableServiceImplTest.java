package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.order.UserWithSomeOrderDetailAndChatIdDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.user.User;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ValuesForUserTableServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTableRepo userTableRepo;

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TelegramChatRepository telegramChatRepository;

    @InjectMocks
    private ValuesForUserTableServiceImpl service;

    private static final String TEST_EMAIL = "test@example.com";

    @Test
    void getAllFieldsShouldReturnPageableDtoWhenDataIsValidTest() {
        Long employeeId = 1L;
        List<Long> tariffsInfoIds = List.of(2L, 3L);
        List<Long> userIds = List.of(4L, 5L, 4L, 5L);
        User user1 = ModelUtils.createTestUser(4L, "John", "Doe", "john.doe@example.com", "+380123456789");
        User user2 = ModelUtils.createTestUser(5L, "Jane", "Smith", "jane.smith@example.com", "+380987654321");
        Page<User> mockPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 2), 2);

        CustomerPage page = new CustomerPage(0, 2);
        String columnName = "name";
        SortingOrder sortingOrder = SortingOrder.ASC;
        UserFilterCriteria filterCriteria = new UserFilterCriteria();

        Mockito.when(employeeRepository.findByEmail(TEST_EMAIL))
            .thenReturn(Optional.of(ModelUtils.createTestEmployee(employeeId)));
        Mockito.when(employeeRepository.findTariffsInfoForEmployee(employeeId))
            .thenReturn(tariffsInfoIds);
        Mockito.when(userRepository.getAllUsersByTariffsInfoId(Mockito.anyLong()))
            .thenReturn(List.of(4L, 5L));
        Mockito.when(userTableRepo.findAll(
            Mockito.eq(filterCriteria),
            Mockito.eq(columnName),
            Mockito.eq(sortingOrder),
            Mockito.eq(page),
            Mockito.anyList()))
            .thenReturn(mockPage);
        Mockito.when(telegramChatRepository.findByUser(Mockito.any())).thenReturn(Optional.empty());

        PageableDto<UserWithSomeOrderDetailAndChatIdDto> result =
            service.getAllFields(page, columnName, sortingOrder, filterCriteria, TEST_EMAIL);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).hasSize(2);
        assertThat(result.getPage())
            .extracting("clientName")
            .containsExactly("John Doe", "Jane Smith");
        assertThat(result.getPage()).allSatisfy(user -> assertThat(user.getChatId()).isNull());
        Mockito.verify(employeeRepository).findByEmail(TEST_EMAIL);
        Mockito.verify(employeeRepository).findTariffsInfoForEmployee(employeeId);
        Mockito.verify(userRepository, Mockito.times(tariffsInfoIds.size()))
            .getAllUsersByTariffsInfoId(Mockito.anyLong());
        Mockito.verify(userTableRepo).findAll(filterCriteria, columnName, sortingOrder, page, userIds);
        Mockito.verify(telegramChatRepository, Mockito.times(2)).findByUser(Mockito.any());
    }

    @Test
    void getAllFieldsShouldThrowExceptionWhenEmployeeNotFoundTest() {
        Mockito.when(employeeRepository.findByEmail(TEST_EMAIL))
            .thenReturn(Optional.empty());

        CustomerPage page = new CustomerPage(0, 2);
        String columnName = "name";
        SortingOrder sortingOrder = SortingOrder.ASC;
        UserFilterCriteria filterCriteria = new UserFilterCriteria();

        assertThatThrownBy(() -> service.getAllFields(page, columnName, sortingOrder, filterCriteria, TEST_EMAIL))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Employee with current id doesn't exist: ");

        Mockito.verify(employeeRepository).findByEmail(TEST_EMAIL);
        Mockito.verifyNoInteractions(userRepository, userTableRepo);
    }
}
