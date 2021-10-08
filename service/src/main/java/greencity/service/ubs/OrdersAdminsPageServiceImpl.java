package greencity.service.ubs;

import greencity.dto.*;
import greencity.entity.enums.EditType;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.user.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static greencity.constant.ErrorMessage.EMPLOYEE_DOESNT_EXIST;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND_BY_ID;

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
                false, true, true, 3, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("orderDate", "Дата замовлення", "Order date"), "orderDate", 20, false, true,
                false,
                4, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
            new ColumnDTO(new TitleDto("paymentDate", "Дата оплати", "Payment date"), "need to implement", 20,
                false, true, false, 5, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_INFO"),
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
                false,
                13, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("district", "Район", "District"), "ubsUser.address.district", 20, false,
                true, false, 14, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("address", "Адреса", "Address"), "need to implement", 20, false, true,
                false, 15,
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
                false, true, false, 24, EditType.DATE, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("timeOfExport", "Час вивезення", "Time of export"), "need to implement", 20,
                false, true, false, 25, EditType.TIME, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("idOrderFromShop", "Номер замовлення з магазину", "Id order from shop"), "",
                20, false, true, false, 26, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("receivingStation", "Станція приймання", "Receiving station"),
                "receivingStation", 20, false, true, false, 27, EditType.SELECT, receivingStationList(),
                "ORDERS_DETAILS"),
            new ColumnDTO(new TitleDto("responsibleManager", "Менеджер послуги", "Responsible manager"), "", 20,
                false, true, false, 28, EditType.SELECT, managerList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleCaller", "Менеджер обдзвону", "Responsible caller"), "", 20,
                false, true, false, 29, EditType.SELECT, callerList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleLogicMan", "Логіст", "Responsible logic man"), "", 20, false,
                true, false, 30, EditType.SELECT, logicManList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleDriver", "Водій", "Responsible driver"), "", 20, false, true,
                false, 31, EditType.SELECT, driverList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("responsibleNavigator", "Штурман", "Responsible navigator"), "", 20, false,
                true, true, 32, EditType.SELECT, navigatorList(), "RESPONSIBLE"),
            new ColumnDTO(new TitleDto("commentsForOrder", "Коментарі до замовлення", "Comments for order"), "",
                20, false, true, false, 33, EditType.READ_ONLY, new ArrayList<>(), "ORDERS_DETAILS"))));
        return new TableParamsDTO(orderPage, orderSearchCriteria, columnDTOS, columnBelongingListForDevelopStage());
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
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 1L));
            case "responsible_caller":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 2L));
            case "responsible_logic_man":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 3L));
            case "responsible_driver":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 5L));
            case "responsible_navigator":
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, 4L));
            default:
                return createReturnForSwitchChangeOrder(new ArrayList<>());
        }
    }

    private ChangeOrderResponseDTO createReturnForSwitchChangeOrder(List<Long> ordersId) {
        return ChangeOrderResponseDTO.builder().httpStatus(HttpStatus.OK).unresolvedGoalsOrderId(ordersId).build();
    }

    private List<OptionForColumnDTO> orderStatusListForDevelopStage() {
        return Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new OptionForColumnDTO("FORMED", "Сформовано", "Formed", false),
            new OptionForColumnDTO("ADJUSTMENT", "На узгодженні", "Adjustment",false),
            new OptionForColumnDTO("BROUGHT_IT_HIMSELF", "Заберуть самостійно", "Brought it himself", false),
            new OptionForColumnDTO("CONFIRMED", "Підтверджено", "Confirmed", true),
            new OptionForColumnDTO("ON_THE_ROUTE", "В дорозі", "On the route", false),
            new OptionForColumnDTO("DONE", "Виконано", "Done", true),
            new OptionForColumnDTO("NOT_TAKEN_OUT", "Не вивезено", "Not taken out", false),
            new OptionForColumnDTO("CANCELLED", "Скасовано", "Canceled", false))));
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
            if (!changeOrderStatus(orderId, orderStatus)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> dateOfExportForDevelopStage(List<Long> ordersId, String value) {
        LocalDate date = LocalDate.parse(value.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderDate(orderId, date)) {
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
        for (Long orderId : ordersId) {
            if (!changeOrderTime(orderId, timeFrom, timeTo)) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private List<Long> receivingStationForDevelopStage(List<Long> ordersId, String value) {
        ReceivingStation station = receivingStationRepository.getOne(Long.parseLong(value));
        List<Long> unresolvedGoals = new ArrayList<>();
        for (Long orderId : ordersId) {
            if (!changeOrderStation(orderId, station)) {
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
        for (Long orderId : ordersId) {
            if (!changeOrderEmployee(orderId, existedEmployee, existedPosition)) {
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

    private boolean changeOrderDate(Long oderId, LocalDate value) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setDateOfExport(value);
            orderRepository.save(existedOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderTime(Long oderId, LocalDateTime from, LocalDateTime to) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setDeliverFrom(from);
            existedOrder.setDeliverFrom(to);
            orderRepository.save(existedOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderStation(Long oderId, ReceivingStation station) {
        try {
            Order existedOrder = orderRepository.findById(oderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            existedOrder.setReceivingStation(station.getName());
            orderRepository.save(existedOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean changeOrderEmployee(Long orderId, Employee employee, Position position) {
        try {
            Order existedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            List<EmployeeOrderPosition> employeeOrderPositions =
                employeeOrderPositionRepository.findAllByOrderId(orderId);
            EmployeeOrderPosition newEmployeeOrderPosition = employeeOrderPositionRepository.save(
                EmployeeOrderPosition.builder().employee(employee).position(position).order(existedOrder).build());
            employeeOrderPositions.add(newEmployeeOrderPosition);
            Set<EmployeeOrderPosition> positionSet = new HashSet<>(employeeOrderPositions);
            existedOrder.setEmployeeOrderPositions(positionSet);
            orderRepository.save(existedOrder);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public List<BlockedOrderDTO> requestToBlockOrder(String userUuid, List<Long> orders) {
        User user = userRepository.findByUuid(userUuid);
        String userName = String.format("%s %s", user.getRecipientName(), user.getRecipientSurname());
        if (orders.isEmpty()) {
            /* update all */
        }
        List<BlockedOrderDTO> blockedOrderDTOS = new ArrayList<>();
        List<Long> afterFiltering = orders.stream().filter(x -> (x % 2 == 0)).collect(Collectors.toList());
        for (Long orderId : afterFiltering) {
            blockedOrderDTOS.add(new BlockedOrderDTO(orderId, userName));
        }
        return blockedOrderDTOS;
    }
}
