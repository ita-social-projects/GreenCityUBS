package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.order.UserWithSomeOrderDetailDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.user.User;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.EmployeeRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValuesForUserTableServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTableRepo userTableRepo;

    @Mock
    private EmployeeRepository employeeRepository;

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

        when(employeeRepository.findByEmail(TEST_EMAIL))
            .thenReturn(Optional.of(ModelUtils.createTestEmployee(employeeId)));
        when(employeeRepository.findTariffsInfoForEmployee(employeeId))
            .thenReturn(tariffsInfoIds);
        when(userRepository.getAllUsersByTariffsInfoId(anyLong()))
            .thenReturn(List.of(4L, 5L));
        when(userTableRepo.findAll(
            eq(filterCriteria),
            eq(columnName),
            eq(sortingOrder),
            eq(page),
            anyList()))
                .thenReturn(mockPage);

        PageableDto<UserWithSomeOrderDetailDto> result =
            service.getAllFields(page, columnName, sortingOrder, filterCriteria, TEST_EMAIL);

        assertNotNull(result);
        assertEquals(2, result.getPage().size());

        verify(employeeRepository).findByEmail(TEST_EMAIL);
        verify(employeeRepository).findTariffsInfoForEmployee(employeeId);
        verify(userRepository, times(tariffsInfoIds.size()))
            .getAllUsersByTariffsInfoId(anyLong());
        verify(userTableRepo).findAll(filterCriteria, columnName, sortingOrder, page, userIds);
    }

    @Test
    void getAllFieldsShouldThrowExceptionWhenEmployeeNotFoundTest() {
        when(employeeRepository.findByEmail(TEST_EMAIL))
            .thenReturn(Optional.empty());

        CustomerPage page = new CustomerPage(0, 2);
        String columnName = "name";
        SortingOrder sortingOrder = SortingOrder.ASC;
        UserFilterCriteria filterCriteria = new UserFilterCriteria();

        assertThrows(EntityNotFoundException.class,
            () -> service.getAllFields(page, columnName, sortingOrder, filterCriteria, TEST_EMAIL));

        verify(employeeRepository).findByEmail(TEST_EMAIL);
        verifyNoInteractions(userRepository, userTableRepo);
    }
}
