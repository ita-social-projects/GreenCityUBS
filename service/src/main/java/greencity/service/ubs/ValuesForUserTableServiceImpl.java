package greencity.service.ubs;

import com.google.common.annotations.VisibleForTesting;
import greencity.dto.order.UserWithSomeOrderDetailDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.*;
import greencity.repository.UserTableRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;

@Service
@AllArgsConstructor
public class ValuesForUserTableServiceImpl implements ValuesForUserTableService {
    UserRepository userRepository;
    UserTableRepo userTableRepo;
    private final EmployeeRepository employeeRepository;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Override
    public PageableDto<UserWithSomeOrderDetailDto> getAllFields(CustomerPage page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria, String email) {
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);
        List<Long> usId = new ArrayList<>();
        for (Long id : tariffsInfoIds) {
            usId.addAll(userRepository.getAllUsersByTariffsInfoId(id));
        }
        Page<User> users = userTableRepo.findAll(userFilterCriteria, columnName, sortingOrder, page, usId);
        List<UserWithSomeOrderDetailDto> fields = new ArrayList<>();
        for (User u : users) {
            UserWithSomeOrderDetailDto allFieldsFromTableDto = mapToDto(u);
            fields.add(allFieldsFromTableDto);
        }

        return new PageableDto<>(fields, users.getTotalElements(),
            users.getPageable().getPageNumber(), users.getTotalPages());
    }

    @VisibleForTesting
    UserWithSomeOrderDetailDto mapToDto(User u) {
        final UserWithSomeOrderDetailDto allFieldsFromTableDto = new UserWithSomeOrderDetailDto();
        StringBuilder name = new StringBuilder();
        allFieldsFromTableDto.setUserId(u.getId());
        if (u.getRecipientName() != null) {
            name.append(u.getRecipientName());
        }
        if (name.length() != 0) {
            name.append(" ");
        }
        if (u.getRecipientSurname() != null) {
            name.append(u.getRecipientSurname());
        }
        allFieldsFromTableDto.setClientName(name.toString());
        allFieldsFromTableDto.setEmail(u.getRecipientEmail());
        if (u.getRecipientPhone() != null) {
            if (!u.getRecipientPhone().contains("+380")) {
                allFieldsFromTableDto.setPhone("+380" + u.getRecipientPhone());
            } else {
                allFieldsFromTableDto.setPhone(u.getRecipientPhone());
            }
        } else {
            allFieldsFromTableDto.setPhone("");
        }
        allFieldsFromTableDto.setNumberOfOrders(u.getOrders().size());
        allFieldsFromTableDto.setViolation(u.getViolations());
        if (u.getDateOfRegistration() != null) {
            allFieldsFromTableDto
                .setRegistrationDate(u.getDateOfRegistration().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        } else {
            allFieldsFromTableDto.setRegistrationDate("");
        }
        allFieldsFromTableDto.setUserBonuses(u.getCurrentPoints().toString());
        Optional<Order> optional =
            u.getOrders().stream().max((o1, o2) -> o1.getOrderDate().compareTo(o2.getOrderDate()));
        if (optional.isPresent()) {
            allFieldsFromTableDto
                .setLastOrderDate(optional
                    .get().getOrderDate().toLocalDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        }
        return allFieldsFromTableDto;
    }
}
