package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.order.UserWithSomeOrderDetailDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.user.User;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private ValuesForUserTableServiceImpl valuesForUserTableService;

    @Test
    void checkGetAllFields() {
        User user = User.builder()
            .orders(new ArrayList<>())
            .violations(5)
            .currentPoints(8)
            .build();
        Pageable pageable = PageRequest.of(1, 1);
        Employee employee = ModelUtils.getEmployee();
        List<Long> tariffsInfo = List.of(1L, 2L, 3L);
        UserWithSomeOrderDetailDto userWithSomeOrderDetail = valuesForUserTableService.mapToDto(user);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong())).thenReturn(tariffsInfo);
        when(userRepository.getAllUsersByTariffsInfoId(anyLong())).thenReturn(tariffsInfo);
        when(userTableRepo.findAll(any(UserFilterCriteria.class), anyString(), any(SortingOrder.class),
            any(CustomerPage.class), Mockito.<Long>anyList())).thenReturn(new PageImpl<>(List.of(user), pageable, 1L));
        PageableDto actual = valuesForUserTableService.getAllFields(new CustomerPage(), "column", SortingOrder.ASC,
            new UserFilterCriteria(),
            employee.getEmail());
        verify(employeeRepository).findByEmail(anyString());
        verify(employeeRepository).findTariffsInfoForEmployee(anyLong());
        verify(userRepository, times(tariffsInfo.size())).getAllUsersByTariffsInfoId(anyLong());
        verify(userTableRepo).findAll(any(UserFilterCriteria.class), anyString(), any(SortingOrder.class),
            any(CustomerPage.class), Mockito.<Long>anyList());
        assertEquals(2L, actual.getTotalElements());
        assertEquals(2, actual.getTotalPages());
        assertEquals(List.of(userWithSomeOrderDetail), actual.getPage());
    }

    @Test
    void checkGetAllFieldsIfEmployeeIsNull() {
        CustomerPage customerPage = new CustomerPage();
        UserFilterCriteria userFilterCriteria = new UserFilterCriteria();
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> valuesForUserTableService
            .getAllFields(customerPage, "column", SortingOrder.ASC, userFilterCriteria, "email"));
        assertEquals(ErrorMessage.EMPLOYEE_NOT_FOUND, ex.getMessage());
    }

}