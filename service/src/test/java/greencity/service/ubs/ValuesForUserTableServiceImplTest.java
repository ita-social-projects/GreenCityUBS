package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.enums.SortingOrder;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.EmployeeRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import org.glassfish.hk2.utilities.Stub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.QPageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    void getAllFields(){
        User user = User.builder()
                .orders(new ArrayList<>())
                .violations(5)
                .currentPoints(8)
                .build();
        Pageable pageable = PageRequest.of(1,1);
        Employee employee = ModelUtils.getEmployee();
        List<Long> tariffsInfo = employee.getTariffInfos().stream().map(t -> t.getId()).collect(Collectors.toList());
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong())).thenReturn(tariffsInfo);
        when(userRepository.getAllUsersByTariffsInfoId(anyLong())).thenReturn(tariffsInfo);
        when(userTableRepo.findAll(any(UserFilterCriteria.class), anyString(), any(SortingOrder.class) ,any(CustomerPage.class), Mockito.<Long>anyList())).thenReturn(new PageImpl<>(List.of(user), pageable, 1L));
        valuesForUserTableService.getAllFields(new CustomerPage(), "column", SortingOrder.ASC, new UserFilterCriteria(), employee.getEmail());

        verify(userTableRepo).findAll(any(UserFilterCriteria.class), anyString(), any(SortingOrder.class) ,any(CustomerPage.class), Mockito.<Long>anyList());
    }

}