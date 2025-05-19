package greencity.service.ubs.manager;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.dto.language.LanguageVO;
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
import jakarta.persistence.EntityNotFoundException;
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
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BigOrderTableViewServiceImplTest {
    private static final String USER_EMAIL = "test@gmail.com";
    private static final String NOT_EXISTS_USER_EMAIL = "not_exists_email@some.com";
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
        LanguageVO languageVO = LanguageVO.builder()
                .id(1L)
                .code("eng")
                .build();
        Optional<Employee> employee = Optional.of(getEmployee());
        List<Long> tariffsInfoIds = new ArrayList<>();
        when(employeeRepository.findByEmail(USER_EMAIL)).thenReturn(employee);
        when(userRemoteClient.findLanguageByEmail(USER_EMAIL)).thenReturn(languageVO);
        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria, tariffsInfoIds, "eng"))
            .thenReturn(Page.empty());

        bigOrderTableService.getOrders(orderPage, orderSearchCriteria, USER_EMAIL);

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

        assertThrows(BadRequestException.class,
            () -> bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2"),
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
        String nonExistUuid = "Non_Exist";
        when(employeeRepository.findByUuid(nonExistUuid)).thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> bigOrderTableService.changeIsFreezeStatus(nonExistUuid, true),
            "Should throw EntityNotFoundException");

        verify(employeeRepository).findByUuid(nonExistUuid);
        verify(tableColumnWidthForEmployeeRepository, times(0)).findByEmployeeId(getEmployee().getId());
        verify(tableColumnWidthForEmployeeRepository, times(0)).save(getTestTableColumnWidthWithIsTableFreezeTrue());

    }

    @Test
    void changeIsFreezeStatusForNon_ExistEmployeeTableTest() {
        String uuid = "Test";
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.ofNullable(getEmployee()));
        when(tableColumnWidthForEmployeeRepository.findByEmployeeId(getEmployee().getId()))
            .thenReturn(Optional.empty());

        assertThrows(
            EntityNotFoundException.class,
            () -> bigOrderTableService.changeIsFreezeStatus(uuid, true),
            "Should throw EntityNotFoundException");

        verify(employeeRepository).findByUuid(uuid);
        verify(tableColumnWidthForEmployeeRepository, times(1)).findByEmployeeId(getEmployee().getId());

    }

    @Test
    void getTotalNumberOfOrdersByEmployeeWithValidEmailTest() {
        Employee employee = ModelUtils.getEmployee();
        List<Long> tariffsInfoIds = List.of(1L, 2L, 3L);
        when(employeeRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(employee.getId())).thenReturn(tariffsInfoIds);
        when(bigOrderTableRepository.getOrdersCountByTariffs(tariffsInfoIds)).thenReturn(10L);

        bigOrderTableService.getTotalNumberOfOrdersByEmployee(USER_EMAIL);

        verify(employeeRepository, times(1)).findByEmail(USER_EMAIL);
        verify(employeeRepository, times(1)).findTariffsInfoForEmployee(employee.getId());
        verify(bigOrderTableRepository, times(1)).getOrdersCountByTariffs(tariffsInfoIds);
    }

    @Test
    void getTotalNumberOfOrdersByEmployeeWithNotValidEmailTest() {
        when(employeeRepository.findByEmail(NOT_EXISTS_USER_EMAIL))
            .thenThrow(new EntityNotFoundException(EMPLOYEE_NOT_FOUND));

        assertThrows(EntityNotFoundException.class,
            () -> bigOrderTableService.getTotalNumberOfOrdersByEmployee(NOT_EXISTS_USER_EMAIL));

        verify(employeeRepository, times(1)).findByEmail(anyString());
        verify(employeeRepository, times(0)).findTariffsInfoForEmployee(anyLong());
        verify(bigOrderTableRepository, times(0)).getOrdersCountByTariffs(anyList());
    }

    private OrderPage getOrderPage() {
        return new OrderPage().setPageNumber(1);
    }

    private OrderSearchCriteria getOrderSearchCriteria() {
        return new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-11-05"));
    }
}
