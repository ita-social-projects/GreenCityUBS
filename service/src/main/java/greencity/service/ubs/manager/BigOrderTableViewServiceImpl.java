package greencity.service.ubs.manager;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.order.BigOrderTableDTO;
import greencity.dto.table.CustomTableViewDto;
import greencity.dto.user.UserVO;
import greencity.entity.parameters.CustomTableView;
import greencity.entity.table.TableColumnWidthForEmployee;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.user.UserNotFoundException;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.BigOrderTableRepository;
import greencity.repository.CustomTableViewRepo;
import greencity.repository.EmployeeRepository;
import greencity.repository.TableColumnWidthForEmployeeRepository;
import greencity.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class BigOrderTableViewServiceImpl implements BigOrderTableServiceView {
    private final BigOrderTableRepository bigOrderTableRepository;
    private final CustomTableViewRepo customTableViewRepo;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserRemoteClient userRemoteClient;
    private final TableColumnWidthForEmployeeRepository tableColumnWidthForEmployeeRepository;

    @Override
    public Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String email) {
        UserVO userVO = userRemoteClient.findNotDeactivatedByEmail(email).orElseThrow(() -> new UserNotFoundException(
            ErrorMessage.USER_WITH_THIS_EMAIL_DOES_NOT_EXIST));
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);
        var orders = bigOrderTableRepository.findAll(orderPage, searchCriteria, tariffsInfoIds,
            userVO.getLanguageVO().getCode());
        var orderList = new ArrayList<BigOrderTableDTO>();
        orders.forEach(o -> orderList.add(modelMapper.map(o, BigOrderTableDTO.class)));
        return new PageImpl<>(orderList, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    public void changeOrderTableView(String uuid, String titles) {
        Employee employeeByUuid = employeeRepository.findByUuid(uuid).orElse(null);
        if (nonNull(employeeByUuid)) {
            TableColumnWidthForEmployee tableByEmployeeId =
                    tableColumnWidthForEmployeeRepository.findByEmployeeId(employeeByUuid.getId()).orElse(null);
            if (nonNull(tableByEmployeeId) && tableByEmployeeId.getIsTableFreeze()) {
                return;
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
            TableColumnWidthForEmployee tableByEmployeeId =
                    tableColumnWidthForEmployeeRepository.findByEmployeeId(employeeByUuid.getId()).orElse(null);
            if (nonNull(tableByEmployeeId)) {
                tableByEmployeeId.setIsTableFreeze(value);
                tableColumnWidthForEmployeeRepository.save(tableByEmployeeId);
            }
        }
    }

    private CustomTableViewDto castTableViewToDto(String titles) {
        return CustomTableViewDto.builder()
            .titles(titles)
            .build();
    }
}
