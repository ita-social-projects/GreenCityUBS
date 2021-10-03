package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.enums.EditType;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;

@Service
@AllArgsConstructor
public class OrdersAdminsPageServiceImpl implements OrdersAdminsPageService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public TableParamsDTO getParametersForOrdersTable(Long userId) {
        List<ColumnStateDTO> columnStateDTOS = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new ColumnStateDTO(new TitleDto("select", "Вибір", "Select"), 20, true, true, 0, EditType.CHECKBOX,
                new ArrayList<>(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("order_id", "Номер замовлення", "Order's number"), 20, true, false, 1,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("order_status", "Статус замовлення", "Order's status"), 20, true, true, 2,
                EditType.SELECT, orderStatusListForDevelopStage(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("payment_status", "Статус оплати", "Payment status"), 20, false, true, 3,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("order_date", "Дата замовлення", "Order date"), 20, false, true, 4,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("payment_date", "Дата оплати", "Payment date"), 20, false, true, 5,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnStateDTO(new TitleDto("client_name", "Ім'я замовника", "Client name"), 20, false, true, 6,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("phone_number", "Телефон замовника", "Phone number"), 20, false, true, 7,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("email", "Email замовника", "Email"), 20, false, true, 8,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("sender_name", "Ім'я відправника", "Sender name"), 20, false, true, 9,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("sender_phone", "Телефон відправника", "Sender phone"), 20, false, true, 10,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("sender_email", "Email відправника", "Sender email"), 20, false, true, 11,
                EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("violations", "Кількість порушень клієнта", "Violations"), 20, false, true,
                12, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnStateDTO(new TitleDto("location", "Локація", "Location"), 20, false, true, 13, EditType.READ_ONLY,
                new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("district", "Район", "District"), 20, false, true, 14, EditType.READ_ONLY,
                new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("address", "Адреса", "Address"), 20, false, true, 15, EditType.READ_ONLY,
                new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("comment_to_address_for_client", "Коментар до адреси від клієнта",
                "Comment to address for client"), 20, false, true, 16, EditType.READ_ONLY, new ArrayList<>(),
                "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("bags_amount", "К-сть пакетів", "Bags amount"), 20, false, true, 17,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("total_order_sum", "Сума замовлення", "Total order sum"), 20, false, true,
                18, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("order_certificate_code", "Номер сертифікату", "Order certificate code"),
                20, false, true, 19, EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnStateDTO(new TitleDto("order_certificate_points", "Загальна знижка", "Order certificate points"),
                20, false, true, 20, EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnStateDTO(new TitleDto("amount_due", "Сума до оплати", "Amount due"), 20, false, true, 21,
                EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnStateDTO(new TitleDto("comment_for_order_by_client", "Коментар до замовлення від клієнта",
                "Comment for order by client"), 20, true, true, 22, EditType.READ_ONLY, new ArrayList<>(),
                "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("payment", "Оплата", "Payment"), 20, false, true, 23, EditType.READ_ONLY,
                new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("date_of_export", "Дата вивезення", "Date of export"), 20, false, true, 24,
                EditType.DATE, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("time_of_export", "Час вивезення", "Time of export"), 20, false, true, 25,
                EditType.TIME, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("id_order_from_shop", "Номер замовлення з магазину", "Id order from shop"),
                20, false, true, 26, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("receiving_station", "Станція приймання", "Receiving station"), 20, false,
                true, 27, EditType.SELECT, orderOptionalListForDevelopStage(), "ORDERS_DETAILS"),
            new ColumnStateDTO(new TitleDto("responsible_manager", "Менеджер послуги", "Responsible manager"), 20,
                false, true, 28, EditType.SELECT, orderOptionalListForDevelopStage(), "RESPONSIBLE"),
            new ColumnStateDTO(new TitleDto("responsible_caller", "Менеджер обдзвону", "Responsible caller"), 20, false,
                true, 29, EditType.SELECT, orderOptionalListForDevelopStage(), "RESPONSIBLE"),
            new ColumnStateDTO(new TitleDto("responsible_logic_man", "Логіст", "Responsible logic man"), 20, false,
                true, 30, EditType.SELECT, orderOptionalListForDevelopStage(), "RESPONSIBLE"),
            new ColumnStateDTO(new TitleDto("responsible_driver", "Водій", "Responsible driver"), 20, false, true, 31,
                EditType.SELECT, orderOptionalListForDevelopStage(), "RESPONSIBLE"),
            new ColumnStateDTO(new TitleDto("responsible_navigator", "Штурман", "Responsible navigator"), 20, false,
                true, 32, EditType.SELECT, orderOptionalListForDevelopStage(), "RESPONSIBLE"),
            new ColumnStateDTO(new TitleDto("comments_for_order", "Коментарі до замовлення", "Comments for order"), 20,
                false, true, 33, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"))));
        return new TableParamsDTO(columnStateDTOS, "order_id", SortingOrder.DESC, columnBelongingListForDevelopStage());
    }

    @Override
    public ChangeOrderResponseDTO chooseOrdersDataSwitcher(String userUuid,
        RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO) {
        String columnName = requestToChangeOrdersDataDTO.getColumnName();
        String value = requestToChangeOrdersDataDTO.getNewValue();
        List<Long> ordersId = requestToChangeOrdersDataDTO.getOrderId();
        switch (columnName) {
            case "order_status":
                return createReturnForSwitchChangeOrder(orderStatusForDevelopStage(ordersId, value));
            case "date_of_export":
                return createReturnForSwitchChangeOrder(dateOfExportForDevelopStage(ordersId, value));
            case "time_of_export":
                return createReturnForSwitchChangeOrder(timeOfExportForDevelopStage(ordersId, value));
            case "receiving_station":
                return createReturnForSwitchChangeOrder(receivingStationForDevelopStage(ordersId, value));
            case "responsible_manager":
                return createReturnForSwitchChangeOrder(responsibleManagerForDevelopStage(ordersId, value));
            case "responsible_caller":
                return createReturnForSwitchChangeOrder(responsibleCallerForDevelopStage(ordersId, value));
            case "responsible_logic_man":
                return createReturnForSwitchChangeOrder(responsibleLogicManForDevelopStage(ordersId, value));
            case "responsible_driver":
                return createReturnForSwitchChangeOrder(responsibleDriverForDevelopStage(ordersId, value));
            case "responsible_navigator":
                return createReturnForSwitchChangeOrder(responsibleNavigatorForDevelopStage(ordersId, value));
            default:
                return createReturnForSwitchChangeOrder(new ArrayList<>());
        }
    }

    private ChangeOrderResponseDTO createReturnForSwitchChangeOrder(List<Long> ordersId) {
        return ChangeOrderResponseDTO.builder().httpStatus(HttpStatus.OK).unresolvedGoalsOrderId(ordersId).build();
    }

    private List<TitleDto> orderStatusListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("FORMED", "Сформовано", "Formed"),
            new TitleDto("ADJUSTMENT", "На узгодженні", "Adjustment"),
            new TitleDto("BROUGHT_IT_HIMSELF", "Заберуть самостійно", "Brought it himself"),
            new TitleDto("CONFIRMED", "Підтверджено", "Confirmed"),
            new TitleDto("ON_THE_ROUTE", "В дорозі", "On the route"),
            new TitleDto("DONE", "Виконано", "Done"),
            new TitleDto("NOT_TAKEN_OUT", "Не вивезено", "Not taken out"),
            new TitleDto("CANCELLED", "Скасовано", "Canceled"))));
    }

    private List<TitleDto> columnBelongingListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("ORDERS_INFO", "Інформація про замовлення", "order info"),
            new TitleDto("CUSTOMERS_INFO", "Інформація про клієнта", "customers info"),
            new TitleDto("ORDERS_DETAILS", "Деталі замовлення", "orders details"),
            new TitleDto("CERTIFICATE", "Сертифікат", "certificate"),
            new TitleDto("RESPONSIBLE", "Відповідальні", "responsible persons"))));
    }

    private List<TitleDto> orderOptionalListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("1", "Щось перше", "Something first"),
            new TitleDto("2", "Щось інше", "Something other"),
            new TitleDto("3", "Третій варіант", "Thirst variant"))));
    }

    private List<Long> orderStatusForDevelopStage(List<Long> ordersId, String value) {
        OrderStatus orderStatus = OrderStatus.valueOf(value);
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderStatus(orderId, orderStatus)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> dateOfExportForDevelopStage(List<Long> ordersId, String value) {
        LocalDateTime date = LocalDateTime.parse(value);
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderDate(orderId, date)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> timeOfExportForDevelopStage(List<Long> ordersId, String value) {
        LocalDateTime time = LocalDateTime.parse(value);
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderTime(orderId, time)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> receivingStationForDevelopStage(List<Long> ordersId, String value) {
        String station = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderStation(orderId, station)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleManagerForDevelopStage(List<Long> ordersId, String value) {
        String manager = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderManager(orderId, manager)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleCallerForDevelopStage(List<Long> ordersId, String value) {
        String caller = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderCaller(orderId, caller)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleLogicManForDevelopStage(List<Long> ordersId, String value) {
        String logicMan = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderLogicMan(orderId, logicMan)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleDriverForDevelopStage(List<Long> ordersId, String value) {
        String driver = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderDriver(orderId, driver)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleNavigatorForDevelopStage(List<Long> ordersId, String value) {
        String navigator = value;
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderNavigator(orderId, navigator)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private boolean changeOrderStatus(Long oderId, OrderStatus orderStatus) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setOrderStatus(orderStatus);
            orderRepository.save(existedOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderDate(Long oderId, LocalDateTime value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setOrderDate(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderTime(Long oderId, LocalDateTime value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setOrderDate(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderStation(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setReceivingStation(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderManager(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.getAttachedEmployees().add(Employee.builder().firstName(value).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderCaller(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.getAttachedEmployees().add(Employee.builder().firstName(value).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderLogicMan(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.getAttachedEmployees().add(Employee.builder().firstName(value).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderDriver(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.getAttachedEmployees().add(Employee.builder().firstName(value).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderNavigator(Long oderId, String value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.getAttachedEmployees().add(Employee.builder().firstName(value).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public List<BlockedOrderDTO> requestToBlockOrder(String userUuid, List<Long> orders) {
        User user = userRepository.findByUuid(userUuid);
        String userName = String.format("%s %s", user.getRecipientName(), user.getRecipientSurname());
        List<BlockedOrderDTO> blockedOrderDTOS = new ArrayList<>();
        List<Long> afterFiltering = orders.stream().filter(x -> (x % 2 == 0)).collect(Collectors.toList());
        for (Long orderId : afterFiltering) {
            blockedOrderDTOS.add(new BlockedOrderDTO(orderId, userName));
        }
        return blockedOrderDTOS;
    }
}
