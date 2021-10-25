package greencity.service.ubs;

import greencity.dto.PageableDto;
import greencity.dto.UserWithSomeOrderDetailDto;
import greencity.entity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.filters.UserFilterCriteria;
import greencity.repository.UserRepository;
import greencity.repository.UserTableRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ValuesForUserTableServiceImpl implements ValuesForUserTableService {
    UserRepository userRepository;
    UserTableRepo userTableRepo;

    @Override
    public PageableDto<UserWithSomeOrderDetailDto> getAllFields(Pageable page, String columnName,
        SortingOrder sortingOrder, UserFilterCriteria userFilterCriteria) {
        Page<User> users = userTableRepo.findAll(userFilterCriteria, columnName, sortingOrder, page);
        List<UserWithSomeOrderDetailDto> fields = new ArrayList<>();
        for (User u : users) {
            UserWithSomeOrderDetailDto allFieldsFromTableDto = mapToDto(u);
            fields.add(allFieldsFromTableDto);
        }

        return new PageableDto<>(fields, users.getTotalElements(),
            users.getPageable().getPageNumber(), users.getTotalPages());
    }

    private UserWithSomeOrderDetailDto mapToDto(User u) {
        final UserWithSomeOrderDetailDto allFieldsFromTableDto = new UserWithSomeOrderDetailDto();
        StringBuilder name = new StringBuilder();
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
