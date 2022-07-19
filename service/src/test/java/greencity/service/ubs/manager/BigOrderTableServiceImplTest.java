package greencity.service.ubs.manager;

import greencity.ModelUtils;
import greencity.dto.order.BigOrderTableDTO;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.filters.*;
import greencity.repository.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class BigOrderTableServiceImplTest {
    @InjectMocks
    private BigOrderTableViewServiceImpl bigOrderTableService;
    @Mock
    private BigOrderTableRepository bigOrderTableRepository;
    @Mock(lenient = true)
    CustomTableViewRepo customTableViewRepo;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void getOrders() {
        var orderPage = getOrderPage();
        var orderSearchCriteria = getOrderSearchCriteria();
        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
        Optional<User> user = Optional.of(ModelUtils.getUser());
        List<Long> tariffsInfoIds = new ArrayList<>();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(employee);
        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria, tariffsInfoIds)).thenReturn(Page.empty());

        bigOrderTableService.getOrders(orderPage, orderSearchCriteria, "test@gmail.com");

        verify(bigOrderTableRepository).findAll(orderPage, orderSearchCriteria, tariffsInfoIds);
    }
//    @Test
//    void getOrdersCorrectCalculateWhenAllValueNotNull() {
//        var pageView = ModelUtils.getBigOrderedTableViewPage();
//        var bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
//        var bigOrderTable = ModelUtils.getBigOrderTableDto();
//        var orderPage = getOrderPage();
//        var orderSearchCriteria = getOrderSearchCriteria();
//        Optional<Employee> employee = Optional.of(ModelUtils.getEmployee());
//        Optional<User> user = Optional.of(ModelUtils.getUser());
//        List<Long> tariffsInfoIds = new ArrayList<>();
//        when(userRepository.findByUuid("uuid")).thenReturn(user.get());
//        when(employeeRepository.findByEmail(user.get().getRecipientEmail())).thenReturn(employee);
//        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria, tariffsInfoIds)).thenReturn(Page.empty());
//        when(modelMapper.map(pageView.getContent().get(0), BigOrderTableDTO.class)).thenReturn(bigOrderTable);
//
//        assertEquals(bigOrderTableService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
//            bigOrderTableDTOPage.getContent());
//    }

    @Test
    void changeOrderTableView() {
        String uuid = "uuid1";
        CustomTableView customTableView = CustomTableView.builder()
            .titles("titles1,titles2")
            .uuid(uuid)
            .build();

        bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
        verify(customTableViewRepo).save(customTableView);
    }

    @Test
    void changeOrderTableView2() {
        String uuid = "uuid1";

        when(customTableViewRepo.existsByUuid(uuid)).thenReturn(Boolean.TRUE);
        bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
    }

    @Test
    void getCustomTableParametersForExistUuid() {
        CustomTableView customTableView = ModelUtils.getCustomTableView();
        when(customTableViewRepo.findByUuid("uuid1")).thenReturn(customTableView);
        when(customTableViewRepo.existsByUuid("uuid1")).thenReturn(Boolean.TRUE);
        bigOrderTableService.getCustomTableParameters(customTableView.getUuid());

        verify(customTableViewRepo).existsByUuid(customTableView.getUuid());
        Assertions.assertNotNull(customTableView);
    }

    @Test
    void getCustomTableParametersForNon_ExistUuid() {
        CustomTableView customTableView = ModelUtils.getCustomTableView();
        when(customTableViewRepo.findByUuid("uuid1")).thenReturn(customTableView);
        when(customTableViewRepo.existsByUuid("uuid1")).thenReturn(Boolean.FALSE);
        bigOrderTableService.getCustomTableParameters(customTableView.getUuid());

        verify(customTableViewRepo).existsByUuid(customTableView.getUuid());
        Assertions.assertNotNull(customTableView);
    }

    private OrderPage getOrderPage() {
        return new OrderPage().setPageNumber(1);
    }

    private OrderSearchCriteria getOrderSearchCriteria() {
        return new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-11-05"));
    }
}
