package greencity.service.ubs.manager;

import java.util.ArrayList;
import java.util.List;
import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.order.OrderCountDto;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.BigOrderTableRepository;
import greencity.repository.CustomTableViewRepo;
import greencity.repository.EmployeeRepository;
import greencity.repository.TableColumnWidthForEmployeeRepository;
import greencity.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.table.CustomTableViewDto;
import greencity.entity.parameters.CustomTableView;
import greencity.dto.filters.OrderPage;
import greencity.dto.filters.OrderSearchCriteria;
import lombok.AllArgsConstructor;
import static greencity.constant.ErrorMessage.CANNOT_CHANGE_ORDER_TABLE_VIEW;
import static greencity.constant.ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.TABLE_COLUMN_WIDTH_BY_EMPLOYEE_ID_NOT_FOUND;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class BigOrderTableViewServiceImpl implements BigOrderTableServiceView {
    private final BigOrderTableRepository bigOrderTableRepository;
    private final CustomTableViewRepo customTableViewRepo;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final UserRemoteClient userRemoteClient;
    private final TableColumnWidthForEmployeeRepository tableColumnWidthForEmployeeRepository;
    private final UserRepository userRepository;

    @Override
    public Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String email) {
        String uuid = userRepository.findUuidByRecipientEmail(email)
            .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        String languageCode = userRemoteClient.findUserLanguageByUuid(uuid);
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);
        var orders = bigOrderTableRepository.findAll(orderPage, searchCriteria, tariffsInfoIds,
            languageCode);
        var orderList = new ArrayList<BigOrderTableDTO>();
        orders.forEach(o -> orderList.add(modelMapper.map(o, BigOrderTableDTO.class)));
        return new PageImpl<>(orderList, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    public void changeOrderTableView(String uuid, String titles) {
        Employee employeeByUuid = employeeRepository.findByUuid(uuid).orElse(null);
        if (nonNull(employeeByUuid)) {
            TableColumnWidthForEmployee tableByEmployeeId = tableColumnWidthForEmployeeRepository
                .findByEmployeeId(employeeByUuid.getId()).orElse(null);
            if (nonNull(tableByEmployeeId) && tableByEmployeeId.isTableFreeze()) {
                throw new BadRequestException(CANNOT_CHANGE_ORDER_TABLE_VIEW);
            }
        }

        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            customTableViewRepo.update(uuid, titles);
        } else {
            CustomTableView customTableView = CustomTableView.builder()
                .uuid(uuid)
                .titles(titles)
                .build();
            customTableViewRepo.save(customTableView);
        }
    }

    @Override
    @Cacheable(value = "OrdersViewParameters", key = "#uuid")
    public CustomTableViewDto getCustomTableParameters(String uuid) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            return castTableViewToDto(customTableViewRepo.findByUuid(uuid).getTitles());
        } else {
            return CustomTableViewDto.builder()
                .titles(" ")
                .build();
        }
    }

    @Override
    public void changeIsFreezeStatus(String uuid, Boolean value) {
        Employee employeeByUuid = employeeRepository.findByUuid(uuid).orElse(null);
        if (nonNull(employeeByUuid)) {
            TableColumnWidthForEmployee tableByEmployeeId = tableColumnWidthForEmployeeRepository
                .findByEmployeeId(employeeByUuid.getId()).orElse(null);
            if (nonNull(tableByEmployeeId)) {
                tableByEmployeeId.setTableFreeze(value);
                tableColumnWidthForEmployeeRepository.save(tableByEmployeeId);
            }
            throw new EntityNotFoundException(TABLE_COLUMN_WIDTH_BY_EMPLOYEE_ID_NOT_FOUND);
        }
        throw new EntityNotFoundException(EMPLOYEE_WITH_UUID_NOT_FOUND);
    }

    @Override
    public OrderCountDto getTotalNumberOfOrdersByEmployee(String email) {
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);

        return new OrderCountDto(bigOrderTableRepository.getOrdersCountByTariffs(tariffsInfoIds));
    }

    private CustomTableViewDto castTableViewToDto(String titles) {
        return CustomTableViewDto.builder()
            .titles(titles)
            .build();
    }
}
