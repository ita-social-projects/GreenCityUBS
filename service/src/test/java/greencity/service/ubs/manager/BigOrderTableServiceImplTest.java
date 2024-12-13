package greencity.service.ubs.manager;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.language.LanguageVO;
import greencity.dto.user.UserVO;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.BadRequestException;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.BigOrderTableRepository;
import greencity.repository.CustomTableViewRepo;
import greencity.repository.EmployeeRepository;
import greencity.repository.TableColumnWidthForEmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getTestTableColumnWidth;
import static greencity.ModelUtils.getTestTableColumnWidthWithIsTableFreezeTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BigOrderTableServiceImplTest {
    @InjectMocks
    private BigOrderTableViewServiceImpl bigOrderTableService;
    @Mock
    private BigOrderTableRepository bigOrderTableRepository;
    @Mock(strictness = Mock.Strictness.LENIENT)
    CustomTableViewRepo customTableViewRepo;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    TableColumnWidthForEmployeeRepository tableColumnWidthForEmployeeRepository;

    @Test
    void getOrders() {
        var orderPage = getOrderPage();
        var orderSearchCriteria = getOrderSearchCriteria();
        Optional<Employee> employee = Optional.of(getEmployee());
        List<Long> tariffsInfoIds = new ArrayList<>();
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(employee);
        UserVO userVO = new UserVO().setLanguageVO(new LanguageVO(null, "eng"));
        when(userRemoteClient.findNotDeactivatedByEmail("test@gmail.com")).thenReturn(Optional.of(userVO));
        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria, tariffsInfoIds, "eng"))
            .thenReturn(Page.empty());

        bigOrderTableService.getOrders(orderPage, orderSearchCriteria, "test@gmail.com");

        verify(bigOrderTableRepository).findAll(orderPage, orderSearchCriteria, tariffsInfoIds, "eng");
    }

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
    void changeOrderTableViewForEmployeeTableWhenIsTableFreezeTrue() {
        String uuid = "uuid1";

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(getEmployee()));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(getEmployee().getId()))
            .thenReturn(Optional.ofNullable(getTestTableColumnWidthWithIsTableFreezeTrue()));

        Assertions.assertThrows(BadRequestException.class, () -> bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2"),
                "should throw BadRequestException");

        verify(employeeRepository).findByUuid(uuid);
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(getEmployee().getId());
    }

    @Test
    void changeOrderTableViewForEmployeeTableWhenIsTableFreezeFalse() {
        String uuid = "Test";

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(getEmployee()));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(getEmployee().getId()))
            .thenReturn(Optional.ofNullable(getTestTableColumnWidth()));

        bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2");

        verify(employeeRepository).findByUuid(uuid);
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(getEmployee().getId());
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

    @Test
    void changeIsFreezeStatusTest() {
        when(employeeRepository.findByUuid("Test")).thenReturn(Optional.ofNullable(getEmployee()));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(getEmployee().getId()))
                .thenReturn(Optional.ofNullable(getTestTableColumnWidth()));
        when(tableColumnWidthForEmployeeRepository.save(getTestTableColumnWidthWithIsTableFreezeTrue()))
                .thenReturn(getTestTableColumnWidthWithIsTableFreezeTrue());

        TableColumnWidthForEmployee byUuid1 = bigOrderTableService.changeIsFreezeStatus("Test", true);

        verify(employeeRepository).findByUuid("Test");
        verify(tableColumnWidthForEmployeeRepository).findByEmployeeId(getEmployee().getId());
        verify(tableColumnWidthForEmployeeRepository).save(getTestTableColumnWidthWithIsTableFreezeTrue());

        Assertions.assertTrue(byUuid1.isTableFreeze(), "Should be true");
    }

    @Test
    void changeIsFreezeStatusForNon_ExistUuidTest() {
        when(employeeRepository.findByUuid("Non_Exist")).thenReturn(Optional.empty());

        TableColumnWidthForEmployee byUuid1 = bigOrderTableService.changeIsFreezeStatus("Non_Exist", true);

        verify(employeeRepository).findByUuid("Non_Exist");
        verify(tableColumnWidthForEmployeeRepository, times(0)).findByEmployeeId(getEmployee().getId());
        verify(tableColumnWidthForEmployeeRepository, times(0)).save(getTestTableColumnWidthWithIsTableFreezeTrue());

        Assertions.assertNull(byUuid1, "Should be null");
    }

    private OrderPage getOrderPage() {
        return new OrderPage().setPageNumber(1);
    }

    private OrderSearchCriteria getOrderSearchCriteria() {
        return new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-11-05"));
    }
}
