package greencity.service.ubs;

import greencity.client.RestClient;
import greencity.dto.*;
import greencity.entity.enums.EditType;
import greencity.entity.enums.OrderStatus;
import greencity.entity.enums.PaymentStatus;
import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static greencity.constant.ErrorMessage.*;

@Service
@AllArgsConstructor
public class OrdersAdminsPageServiceImpl implements OrdersAdminsPageService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final UBSManagementEmployeeService employeeService;
    private final ModelMapper modelMapper;
    private final ReceivingStationRepository receivingStationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final RestClient restClient;

    @Override
    public TableParamsDTO getParametersForOrdersTable(Long userId) {
        OrderPage orderPage = new OrderPage();
        OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();

        List<ColumnDTO> columnDTOS = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new ColumnDTO(new TitleDto("select", "Вибір", "Select"), "", 20, true, true, false, 0,
                EditType.CHECKBOX,
                new ArrayList<>(), ""),
            new ColumnDTO(new TitleDto("id", "Номер замовлення", "Order's number"), "id", 20, true, false, false,
                1,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("orderStatus", "Статус замовлення", "Order's status"), "orderStatus", 20,
                true, true, true, 2, EditType.SELECT, orderStatusListForDevelopStage(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("paymentStatus", "Статус оплати", "Payment status"), "paymentStatus", 20,
                false, true, true, 3, EditType.READ_ONLY, orderPaymentStatusListForDevelopStage(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("orderDate", "Дата замовлення", "Order date"), "orderDate", 20, false, true,
                false,
                4, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("paymentDate", "Дата оплати", "Payment date"), "need to implement", 20,
                false, true, true, 5, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("clientName", "Ім'я замовника", "Client name"), "need to implement", 20,
                false, true, false, 6, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("phoneNumber", "Телефон замовника", "Phone number"), "user.recipientPhone",
                20, false, true, false, 7, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("email", "Email замовника", "Email"), "user.recipientEmail", 20, false,
                true, false, 8, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("senderName", "Ім'я відправника", "Sender name"), "need to implement", 20,
                false, true, false, 9, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("senderPhone", "Телефон відправника", "Sender phone"),
                "ubsUser.phoneNumber", 20, false, true, false, 10, EditType.READ_ONLY, new ArrayList<>(),
                "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("senderEmail", "Email відправника", "Sender email"), "ubsUser.email", 20,
                false, true, false, 11, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("violationsAmount", "Кількість порушень клієнта", "Violations"),
                "user.violations", 20, false, true, false, 12, EditType.READ_ONLY, new ArrayList<>(), "CUSTOMERS_INFO"),
            new ColumnDTO(new TitleDto("location", "Локація", "Location"), "user.lastLocation", 20, false, true,
                true,
                13, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("district", "Район", "District"), "ubsUser.address.district", 20, false,
                true, true, 14, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("address", "Адреса", "Address"), "need to implement", 20, false, true,
                true, 15,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(
                new TitleDto("commentToAddressForClient", "Коментар до адреси від клієнта",
                    "Comment to address for client"),
                "", 20, false, true, false, 16, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("bagsAmount", "К-сть пакетів", "Bags amount"), "", 20, false, true, false,
                17,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("totalOrderSum", "Сума замовлення", "Total order sum"), "need to implement",
                20, false, true, false, 18, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("orderCertificateCode", "Номер сертифікату", "Order certificate code"), "",
                20, false, true, false, 19, EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnDTO(new TitleDto("orderCertificatePoints", "Загальна знижка", "Order certificate points"),
                "pointsToUse", 20, false, true, false, 20, EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnDTO(new TitleDto("amountDue", "Сума до оплати", "Amount due"), "need to implement", 20,
                false, true, false, 21, EditType.READ_ONLY, new ArrayList<>(), "CERTIFICATE"),
            new ColumnDTO(
                new TitleDto("commentForOrderByClient", "Коментар до замовлення від клієнта",
                    "Comment for order by client"),
                "", 20, true, true, false, 22, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("payment", "Оплата", "Payment"), "need to implement", 20, false, true,
                false, 23,
                EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("dateOfExport", "Дата вивезення", "Date of export"), "dateOfExport", 20,
                false, true, true, 24, EditType.DATE, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("timeOfExport", "Час вивезення", "Time of export"), "need to implement", 20,
                false, true, false, 25, EditType.TIME, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("idOrderFromShop", "Номер замовлення з магазину", "Id order from shop"), "",
                20, false, true, false, 26, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("receivingStation", "Станція приймання", "Receiving station"),
                "receivingStation", 20, false, true, true, 27, EditType.SELECT, receivingStationList(),
                "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("responsibleManager", "Менеджер послуги", "Responsible manager"), "", 20,
                false, true, true, 28, EditType.SELECT, managerList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleCaller", "Менеджер обдзвону", "Responsible caller"), "", 20,
                false, true, true, 29, EditType.SELECT, callerList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleLogicMan", "Логіст", "Responsible logic man"), "", 20, false,
                true, true, 30, EditType.SELECT, logicManList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleDriver", "Водій", "Responsible driver"), "", 20, false, true,
                true, 31, EditType.SELECT, driverList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleNavigator", "Штурман", "Responsible navigator"), "", 20, false,
                true, true, 32, EditType.SELECT, navigatorList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("commentsForOrder", "Коментарі до замовлення", "Comments for order"), "",
                20, false, true, false, 33, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("blockedBy", "Ким заблоковано", "Blocked by"), "",
                20, false, true, true, 34, EditType.READ_ONLY, blockingStatusListForDevelopStage(),
                "ORDERS_DETAILS"))));
        return new TableParamsDTO(orderPage, orderSearchCriteria, columnDTOS, columnBelongingListForDevelopStage());
    }

    @Override
    public ChangeOrderResponseDTO chooseOrdersDataSwitcher(String userUuid,
        RequestToChangeOrdersDataDTO requestToChangeOrdersDataDTO) {
        String columnName = requestToChangeOrdersDataDTO.getColumnName();
        String value = requestToChangeOrdersDataDTO.getNewValue();
        List<Long> ordersId = requestToChangeOrdersDataDTO.getOrderId();
        switch (columnName) {
            case "orderStatus":
                return createReturnForSwitchChangeOrder(orderStatusForDevelopStage(ordersId, value));
            case "dateOfExport":
                return createReturnForSwitchChangeOrder(dateOfExportForDevelopStage(ordersId, value));
            case "timeOfExport":
                return createReturnForSwitchChangeOrder(timeOfExportForDevelopStage(ordersId, value));
            case "receivingStation":
                return createReturnForSwitchChangeOrder(receivingStationForDevelopStage(ordersId, value));
            case "responsibleManager":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 1L));
            case "responsibleCaller":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 2L));
            case "responsibleLogicMan":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 3L));
            case "responsibleDriver":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 5L));
            case "responsibleNavigator":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 4L));
            default:
                return createReturnForSwitchChangeOrder(new ArrayList<>());
        }
    }

    private ChangeOrderResponseDTO createReturnForSwitchChangeOrder(List<Long> ordersId) {
        return ChangeOrderResponseDTO.builder().httpStatus(HttpStatus.OK).unresolvedGoalsOrderId(ordersId).build();
    }

    private List<OptionForColumnDTO> orderStatusListForDevelopStage() {
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        OrderStatus[] orderStatuses = OrderStatus.values();
        for (OrderStatus o : orderStatuses) {
            String ua = orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(o.getNumValue(), 1L)
                .orElseThrow(() -> new EntityNotFoundException("Order status have not found")).getName();
            String en = orderStatusTranslationRepository.getOrderStatusTranslationByIdAndLanguageId(o.getNumValue(), 2L)
                .orElseThrow(() -> new EntityNotFoundException("Order status have not found")).getName();
            optionForColumnDTOS
                .add(OptionForColumnDTO.builder().key(o.toString()).ua(ua).en(en).filtered(false).build());
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> orderPaymentStatusListForDevelopStage() {
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        PaymentStatus[] paymentStatuses = PaymentStatus.values();
        for (PaymentStatus p : paymentStatuses) {
            optionForColumnDTOS.add(OptionForColumnDTO.builder().key(p.name()).ua(p.name()).en(p.name()).build());
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> blockingStatusListForDevelopStage() {
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        optionForColumnDTOS.add(OptionForColumnDTO.builder().key("blocked").ua("Заблоковано").en("Blocked").build());
        optionForColumnDTOS
            .add(OptionForColumnDTO.builder().key("notBlocked").ua("Не заблоковано").en("Not blocked").build());
        return optionForColumnDTOS;
    }

    private List<TitleDto> columnBelongingListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new TitleDto("ORDERS_INFO", "Інформація про замовлення", "order info"),
            new TitleDto("CUSTOMERS_INFO", "Інформація про клієнта", "customers info"),
            new TitleDto("ORDERS_DETAILS", "Деталі замовлення", "orders details"),
            new TitleDto("CERTIFICATE", "Сертифікат", "certificate"),
            new TitleDto("RESPONSIBLE", "Відповідальні", "responsible persons"))));
    }

    private List<OptionForColumnDTO> receivingStationList() {
        List<ReceivingStationDto> receivingStations = employeeService.getAllReceivingStation();
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (ReceivingStationDto r : receivingStations) {
            optionForColumnDTOS.add(modelMapper.map(r, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> managerList() {
        List<Employee> employeeList = employeeRepository.getAllEmployeeByPositionId(1L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> callerList() {
        List<Employee> employeeList = employeeRepository.getAllEmployeeByPositionId(2L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> logicManList() {
        List<Employee> employeeList = employeeRepository.getAllEmployeeByPositionId(3L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> navigatorList() {
        List<Employee> employeeList = employeeRepository.getAllEmployeeByPositionId(4L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> driverList() {
        List<Employee> employeeList = employeeRepository.getAllEmployeeByPositionId(5L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    /* methods for changing order */
    private List<Long> orderStatusForDevelopStage(List<Long> ordersId, String value) {
        OrderStatus orderStatus = OrderStatus.valueOf(value);
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            /* update all */
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setOrderStatus(orderStatus);
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> dateOfExportForDevelopStage(List<Long> ordersId, String value) {
        LocalDate date = LocalDate.parse(value.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            /* update all */
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setDateOfExport(date);
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> timeOfExportForDevelopStage(List<Long> ordersId, String value) {
        String from = value.substring(0, 5);
        String to = value.substring(6);
        LocalDateTime timeFrom = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_TIME);
        LocalDateTime timeTo = LocalDateTime.parse(to, DateTimeFormatter.ISO_LOCAL_TIME);
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            /* update all */
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setDeliverFrom(timeFrom);
                existedOrder.setDeliverFrom(timeTo);
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> receivingStationForDevelopStage(List<Long> ordersId, String value) {
        ReceivingStation station = receivingStationRepository.getOne(Long.parseLong(value));
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            /* update all */
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setReceivingStation(station.getName());
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> responsibleEmployee(List<Long> ordersId, String employee, Long position) {
        Employee existedEmployee = employeeRepository.findById(Long.parseLong(employee))
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_DOESNT_EXIST));
        Position existedPosition = positionRepository.findById(position)
            .orElseThrow(() -> new EntityNotFoundException(POSITION_NOT_FOUND_BY_ID));
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            /* update all */
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                List<EmployeeOrderPosition> employeeOrderPositions =
                    employeeOrderPositionRepository.findAllByOrderId(orderId);
                EmployeeOrderPosition newEmployeeOrderPosition = employeeOrderPositionRepository.save(
                    EmployeeOrderPosition.builder().employee(existedEmployee).position(existedPosition)
                        .order(existedOrder).build());
                employeeOrderPositions.add(newEmployeeOrderPosition);
                Set<EmployeeOrderPosition> positionSet = new HashSet<>(employeeOrderPositions);
                existedOrder.setEmployeeOrderPositions(positionSet);
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    @Override
    @Transactional
    public synchronized List<BlockedOrderDTO> requestToBlockOrder(String userUuid, List<Long> orders) {
        String email = restClient.findUserByUUid(userUuid)
            .orElseThrow(() -> new EntityNotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST)).getEmail();
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        if (orders.isEmpty()) {
            /* block all */
            /* probably using query in repo */
        }
        List<BlockedOrderDTO> blockedOrderDTOS = new ArrayList<>();
        for (Long orderId : orders) {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            if (order.isBlocked()) {
                blockedOrderDTOS.add(BlockedOrderDTO
                    .builder().orderId(orderId).userName(String.format("%s %s",
                        order.getBlockedByEmployee().getFirstName(), order.getBlockedByEmployee().getLastName()))
                    .build());
            } else {
                order.setBlocked(true);
                order.setBlockedByEmployee(employee);
                orderRepository.save(order);
            }
        }
        return blockedOrderDTOS;
    }

    @Override
    public List<Long> unblockOrder(String userUuid, List<Long> orders) {
        String email = restClient.findUserByUUid(userUuid)
            .orElseThrow(() -> new EntityNotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST)).getEmail();
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        if (orders.isEmpty()) {
            /* unblock all */
            /* probably using query in repo */
        }
        List<Long> unblockedOrdersId = new ArrayList<>();
        for (Long orderId : orders) {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            if (order.isBlocked() && order.getBlockedByEmployee().equals(employee)) {
                order.setBlocked(false);
                order.setBlockedByEmployee(null);
                orderRepository.save(order);
                unblockedOrdersId.add(order.getId());
            }
        }
        return unblockedOrdersId;
    }
}
