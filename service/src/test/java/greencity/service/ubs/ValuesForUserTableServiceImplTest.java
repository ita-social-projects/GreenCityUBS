package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.order.UserWithSomeOrderDetailDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.user.employee.Employee;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.EmployeeRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValuesForUserTableServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    UserTableRepo userTableRepo;
    @Mock
    EmployeeRepository employeeRepository;
    @InjectMocks
    ValuesForUserTableServiceImpl valuesForUserTableService;
    @Test
    void getAllFields() {
        CustomerPage page = new CustomerPage();
        SortingOrder sortingOrder = SortingOrder.ASC;
        UserFilterCriteria userFilterCriteria = new UserFilterCriteria();
        Optional<Employee> employee = ModelUtils.getOptionalEmployee();
        List<Long> tariffsInfoIds = List.of(1L, 2L);
        PageableDto<UserWithSomeOrderDetailDto> expectedResult = ModelUtils.getUserWithSomeOrderDetailDto();

        when(employeeRepository.findByEmail("employee@gmail.com")).thenReturn(employee);
        when(employeeRepository.findTariffsInfoForEmployee(1L)).thenReturn(tariffsInfoIds);
        when(userRepository.getAllUsersByTariffsInfoId(1L)).thenReturn(List.of(3L));
        when(userRepository.getAllUsersByTariffsInfoId(2L)).thenReturn(List.of(4L));
        when(userTableRepo.findAll(eq(userFilterCriteria), anyString(), eq(sortingOrder),eq(page), eq(List.of(3L,4L))))
                .thenReturn(ModelUtils.getUsersPage());

        PageableDto<UserWithSomeOrderDetailDto> result = valuesForUserTableService
                .getAllFields(page, "", sortingOrder, userFilterCriteria, "employee@gmail.com");

        assertEquals(result, expectedResult);
    }
}