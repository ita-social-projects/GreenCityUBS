package greencity.service.ubs;

import greencity.dto.order.UserWithSomeOrderDetailAndChatIdDto;
import greencity.dto.pageble.PageableDto;
import greencity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.filters.CustomerPage;
import greencity.filters.UserFilterCriteria;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
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
    private final TelegramChatRepository telegramChatRepository;
    private static final String DATE_FORMAT = "dd-MM-yyyy";

    @Override
    public PageableDto<UserWithSomeOrderDetailAndChatIdDto> getAllFields(CustomerPage page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria, String email) {
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        List<Long> tariffsInfoIds = employeeRepository.findTariffsInfoForEmployee(employeeId);
        List<Long> usId = new ArrayList<>();
        for (Long id : tariffsInfoIds) {
            usId.addAll(userRepository.getAllUsersByTariffsInfoId(id));
        }
        Page<User> users = userTableRepo.findAll(userFilterCriteria, columnName, sortingOrder, page, usId);
        List<UserWithSomeOrderDetailAndChatIdDto> fields = new ArrayList<>();
        for (User u : users) {
            UserWithSomeOrderDetailAndChatIdDto allFieldsFromTableDto = mapToDtoV2(u);
            fields.add(allFieldsFromTableDto);
        }

        return new PageableDto<>(fields, users.getTotalElements(),
            users.getPageable().getPageNumber(), users.getTotalPages());
    }

    private UserWithSomeOrderDetailAndChatIdDto mapToDtoV2(User u) {
        final UserWithSomeOrderDetailAndChatIdDto allFieldsFromTableDto = new UserWithSomeOrderDetailAndChatIdDto();
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
            u.getOrders().stream().max(Comparator.comparing(Order::getOrderDate));
        if (optional.isPresent()) {
            allFieldsFromTableDto
                .setLastOrderDate(optional
                    .get().getOrderDate().toLocalDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        }
        allFieldsFromTableDto.setStatus(u.getStatus());
        telegramChatRepository.findByUser(u)
            .ifPresent(chat -> allFieldsFromTableDto.setChatId(chat.getId()));
        return allFieldsFromTableDto;
    }
}
