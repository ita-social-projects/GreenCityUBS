package greencity.service.ubs;

import greencity.dto.FieldsForUsersTableDto;
import greencity.dto.UserWithSomeOrderDetailDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ValuesForUserTableServiceImpl implements ValuesForUserTableService {
    UserRepository userRepository;

    @Override
    public FieldsForUsersTableDto getAllFields() {
        List<User> users = userRepository.findAllUsersWhoMadeAtLeastOneOrder();
        List<UserWithSomeOrderDetailDto> fields = new ArrayList<>();
        for (User u : users) {
            UserWithSomeOrderDetailDto allFieldsFromTableDto = mapToDto(u);
            fields.add(allFieldsFromTableDto);
        }
        FieldsForUsersTableDto fieldsForUsersTableDto = new FieldsForUsersTableDto();
        fieldsForUsersTableDto.setUserList(fields);
        fieldsForUsersTableDto.setResultNumber(fields.size());

        return fieldsForUsersTableDto;
    }

    private UserWithSomeOrderDetailDto mapToDto(User u) {
        UserWithSomeOrderDetailDto allFieldsFromTableDto = new UserWithSomeOrderDetailDto();
        if (u.getRecipientName() != null && u.getRecipientSurname() != null) {
            allFieldsFromTableDto.setClientName(u.getRecipientName() + " " + u.getRecipientSurname());
        } else {
            allFieldsFromTableDto.setClientName("");
        }
        allFieldsFromTableDto.setEmail(u.getRecipientEmail());
        if (u.getRecipientEmail() != null) {
            allFieldsFromTableDto.setPhone(u.getRecipientPhone());
        } else {
            allFieldsFromTableDto.setPhone("");
        }
        allFieldsFromTableDto.setNumberOfOrders(u.getOrders().size());
        allFieldsFromTableDto.setViolation(u.getViolations());
        if (u.getDateOfRegistration() != null) {
            allFieldsFromTableDto
                .setRegistrationDate(u.getDateOfRegistration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            allFieldsFromTableDto.setRegistrationDate("");
        }
        allFieldsFromTableDto.setUserBonuses(u.getCurrentPoints().toString());
        Optional<Order> optional =
            u.getOrders().stream().max((o1, o2) -> o1.getOrderDate().compareTo(o2.getOrderDate()));
        if (optional.isPresent()) {
            allFieldsFromTableDto
                .setLastOrderDate(optional
                    .get().getOrderDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        return allFieldsFromTableDto;
    }
}
