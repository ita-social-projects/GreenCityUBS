package greencity.service.ubs;

import greencity.client.UserRemoteClient;
import greencity.constant.OrderHistory;
import greencity.dto.OptionForColumnDTO;
import greencity.dto.TitleDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.order.BlockedOrderDto;
import greencity.dto.order.ChangeOrderResponseDTO;
import greencity.dto.order.RequestToChangeOrdersDataDto;
import greencity.dto.table.ColumnDTO;
import greencity.dto.table.TableParamsDto;
import greencity.enums.CancellationReason;
import greencity.enums.EditType;
import greencity.enums.OrderStatus;
import greencity.enums.PaymentStatus;
import greencity.entity.order.Certificate;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.user.User;
import greencity.entity.user.employee.*;
import greencity.exceptions.*;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import greencity.service.notification.NotificationServiceImpl;
import lombok.Data;
import org.apache.commons.lang3.EnumUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static greencity.constant.ErrorMessage.*;
import static java.util.Objects.isNull;

@Service
@Data
public class OrdersAdminsPageServiceImpl implements OrdersAdminsPageService {
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    private final CertificateRepository certificateRepository;
    private final UBSManagementEmployeeService employeeService;
    private final ModelMapper modelMapper;
    private final ReceivingStationRepository receivingStationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private final OrderStatusTranslationRepository orderStatusTranslationRepository;
    private final OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final NotificationServiceImpl notificationService;
    private final SuperAdminService superAdminService;
    private static final String ORDER_STATUS = "orderStatus";
    private static final String DATE_OF_EXPORT = "dateOfExport";
    private static final String RECEIVING = "receivingStation";
    private static final String LOGIC_MAN = "responsibleLogicMan";
    private static final String DRIVER = "responsibleDriver";
    private static final String CALLER = "responsibleCaller";
    private static final String NAVIGATOR = "responsibleNavigator";
    private static final String TIME_OF_EXPORT = "timeOfExport";
    private static final String CANCELLATION_REASON = "cancellationReason";
    private static final String CANCELLATION_COMMENT = "cancellationComment";

    @Override
    public TableParamsDto getParametersForOrdersTable(String uuid) {
        String ordersInfo = "ORDERS_INFO";
        String customersInfo = "CUSTOMERS_INFO";
        String exportAddress = "EXPORT_ADDRESS";
        String orderDetails = "ORDERS_DETAILS";
        String exportDetails = "EXPORT_DETAILS";
        String responsible = "RESPONSIBLE";

        OrderPage orderPage = new OrderPage();
        OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria();

        List<ColumnDTO> columnDTOS = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new ColumnDTO(new TitleDto("select", "Вибір", "Select"), "", 20, true, true, false, 0,
                EditType.CHECKBOX,
                new ArrayList<>(), ""),
            new ColumnDTO(new TitleDto("id", "Номер замовлення", "Order number"), "id", 20, true, false, false,
                1,
                EditType.READ_ONLY, new ArrayList<>(), ordersInfo),
            new ColumnDTO(new TitleDto(ORDER_STATUS, "Статус замовлення", "Order status"), ORDER_STATUS, 20,
                true, true, true, 2, EditType.SELECT, orderStatusListForDevelopStage(), ordersInfo),
            new ColumnDTO(new TitleDto("orderPaymentStatus", "Статус оплати", "Payment status"), "orderPaymentStatus",
                20,
                true, true, true, 3, EditType.READ_ONLY, orderPaymentStatusListForDevelopStage(), ordersInfo),
            new ColumnDTO(new TitleDto("orderDate", "Дата замовлення", "Order date"), "orderDate", 20, false, true,
                true,
                4, EditType.READ_ONLY, new ArrayList<>(), ordersInfo),
            new ColumnDTO(new TitleDto("paymentDate", "Дата оплати", "Payment date"), "paymentDate", 20,
                false, true, true, 5, EditType.READ_ONLY, new ArrayList<>(), ordersInfo),
            new ColumnDTO(new TitleDto("commentsForOrder", "Коментар адміністратора", "Admin comment"),
                "commentsForOrder",
                20, false, true, false, 33, EditType.READ_ONLY, new ArrayList<>(), ordersInfo),
            new ColumnDTO(new TitleDto("clientName", "Ім'я клієнта", "Client name"), "clientName", 20,
                false, true, false, 6, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("clientPhone", "Телефон клієнта", "Phone number"), "clientPhoneNumber",
                20, false, true, false, 7, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("clientEmail", "Email клієнта", "Client email"), "clientEmail", 20,
                false, true, false, 8, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("senderName", "Ім'я відправника", "Sender name"), "senderName", 20,
                false, true, false, 9, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("senderPhone", "Телефон відправника", "Sender phone"),
                "senderPhone", 20, false, true, false, 10, EditType.READ_ONLY, new ArrayList<>(),
                customersInfo),
            new ColumnDTO(new TitleDto("senderEmail", "Email відправника", "Sender email"), "senderEmail", 20,
                false, true, false, 11, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("violationsAmount", "Кількість порушень клієнта", "Violations"),
                "violationsAmount", 20, false, true, false, 12, EditType.READ_ONLY, new ArrayList<>(), customersInfo),
            new ColumnDTO(new TitleDto("region", "Область", "Region"), "region", 20, false,
                true, false, 35, EditType.READ_ONLY, new ArrayList<>(), exportAddress),
            new ColumnDTO(new TitleDto("settlement", "Населений пункт", "Settlement"), "settlement", 20,
                false,
                true, false, 36, EditType.READ_ONLY, new ArrayList<>(), exportAddress),
            new ColumnDTO(new TitleDto("district", "Район", "District"), "district", 20, false,
                true, false, 37, EditType.READ_ONLY, new ArrayList<>(), exportAddress),
            new ColumnDTO(new TitleDto("address", "Адреса", "Address"), "address", 20, false, true,
                false, 15,
                EditType.READ_ONLY, new ArrayList<>(), exportAddress),
            new ColumnDTO(
                new TitleDto("commentToAddressForClient", "Коментар до адреси від клієнта",
                    "Comment to address from the client"),
                "commentToAddressForClient", 20, false, true, false, 16, EditType.READ_ONLY, new ArrayList<>(),
                exportAddress),
            new ColumnDTO(new TitleDto("bagsAmount", "К-сть пакетів", "Bags amount"), "bagAmount", 20, false, true,
                false,
                17,
                EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto("totalOrderSum", "Сума замовлення", "Total order sum"), "totalOrderSum",
                20, false, true, false, 18, EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto("orderCertificateCode", "Номер сертифікату", "Order certificate code"),
                "orderCertificateCode",
                20, false, true, false, 19, EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto("generalDiscount", "Загальна знижка", "General discount"),
                "generalDiscount", 20, false, true, false, 20, EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto("amountDue", "Сума до оплати", "Amount due"), "amountDue", 20,
                false, true, false, 21, EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(
                new TitleDto("commentForOrderByClient", "Коментар до замовлення від клієнта",
                    "Comment to the order from the client"),
                "commentForOrderByClient", 20, false, true, false, 22, EditType.READ_ONLY, new ArrayList<>(),
                orderDetails),
            new ColumnDTO(new TitleDto("totalPayment", "Оплата", "Total payment"),
                "totalPayment", 20, false, true,
                false, 23,
                EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto(DATE_OF_EXPORT, "Дата вивезення", "Date of export"), DATE_OF_EXPORT, 20,
                false, true, true, 24, EditType.DATE, new ArrayList<>(), exportDetails),
            new ColumnDTO(new TitleDto(TIME_OF_EXPORT, "Час вивезення", "Time of export"), TIME_OF_EXPORT, 20,
                false, true, false, 25, EditType.TIME, new ArrayList<>(), exportDetails),
            new ColumnDTO(new TitleDto("idOrderFromShop", "Номер замовлення з магазину", "Id order from shop"),
                "idOrderFromShop",
                20, false, true, false, 26, EditType.READ_ONLY, new ArrayList<>(), orderDetails),
            new ColumnDTO(new TitleDto(RECEIVING, "Станція приймання", "Receiving station"),
                RECEIVING, 20, false, true, true, 27, EditType.SELECT, receivingStationList(),
                exportDetails),
            new ColumnDTO(new TitleDto(CALLER, "Менеджер обдзвону", "Responsible caller"), CALLER, 20,
                false, true, true, 29, EditType.SELECT, callerList(), responsible),
            new ColumnDTO(new TitleDto(LOGIC_MAN, "Логіст", "Logistician"), LOGIC_MAN, 20, false,
                true, true, 30, EditType.SELECT, logicManList(), responsible),
            new ColumnDTO(new TitleDto(DRIVER, "Водій", "Responsible driver"), DRIVER, 20, false, true,
                true, 31, EditType.SELECT, driverList(), responsible),
            new ColumnDTO(new TitleDto(NAVIGATOR, "Штурман", "Responsible navigator"), NAVIGATOR, 20, false,
                true, true, 32, EditType.SELECT, navigatorList(), responsible),
            new ColumnDTO(new TitleDto("blockedBy", "Ким заблоковано", "Blocked by"), "blockedBy",
                20, false, true, false, 34, EditType.READ_ONLY, blockingStatusListForDevelopStage(),
                orderDetails))));
        return new TableParamsDto(orderPage, orderSearchCriteria, columnDTOS, columnBelongingListForDevelopStage());
    }

    @Override
    public ChangeOrderResponseDTO chooseOrdersDataSwitcher(String email,
        RequestToChangeOrdersDataDto requestToChangeOrdersDataDTO) {
        String columnName = requestToChangeOrdersDataDTO.getColumnName();
        String value = requestToChangeOrdersDataDTO.getNewValue();
        List<Long> ordersId = requestToChangeOrdersDataDTO.getOrderId();
        Long employeeId = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND)).getId();
        switch (columnName) {
            case ORDER_STATUS:
                return createReturnForSwitchChangeOrder(orderStatusForDevelopStage(ordersId, value, employeeId));
            case DATE_OF_EXPORT:
                return createReturnForSwitchChangeOrder(dateOfExportForDevelopStage(ordersId, value, employeeId));
            case TIME_OF_EXPORT:
                return createReturnForSwitchChangeOrder(timeOfExportForDevelopStage(ordersId, value, employeeId));
            case RECEIVING:
                return createReturnForSwitchChangeOrder(receivingStationForDevelopStage(ordersId, value, employeeId));
            case CANCELLATION_REASON:
                return createReturnForSwitchChangeOrder(cancellationReasonForDevelopStage(ordersId, value));
            case CANCELLATION_COMMENT:
                return createReturnForSwitchChangeOrder(
                    cancellationCommentForDevelopStage(ordersId, value, employeeId));
            default:
                Long position = ColumnNameToPosition.columnNameToEmployeePosition(columnName);
                return createReturnForSwitchChangeOrder(responsibleEmployee(ordersId, value, position, email));
        }
    }

    private enum ColumnNameToPosition {
        RESPONSIBLE_MANAGER("responsibleManager", 1L),
        RESPONSIBLE_CALLER(CALLER, 2L),
        RESPONSIBLE_LOGICMAN(LOGIC_MAN, 3L),
        RESPONSIBLE_NAVIGATOR(NAVIGATOR, 4L),
        RESPONSIBLE_DRIVER(DRIVER, 5L);

        ColumnNameToPosition(String columnValue, Long positionId) {
            this.columnValue = columnValue;
            this.positionId = positionId;
        }

        public static Long columnNameToEmployeePosition(String columnName) {
            return Arrays.stream(ColumnNameToPosition.values())
                .filter(entity -> (entity.columnValue.equals(columnName)))
                .mapToLong(value -> value.positionId).findFirst().getAsLong();
        }

        private final String columnValue;
        private final Long positionId;
    }

    private ChangeOrderResponseDTO createReturnForSwitchChangeOrder(List<Long> ordersId) {
        return ChangeOrderResponseDTO.builder().httpStatus(HttpStatus.OK).unresolvedGoalsOrderId(ordersId).build();
    }

    private List<OptionForColumnDTO> orderStatusListForDevelopStage() {
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        OrderStatus[] orderStatuses = OrderStatus.values();
        for (OrderStatus o : orderStatuses) {
            String ua = orderStatusTranslationRepository.getOrderStatusTranslationById((long) o.getNumValue())
                .orElseThrow(() -> new EntityNotFoundException(ORDER_STATUS_NOT_FOUND)).getName();
            String en = orderStatusTranslationRepository.getOrderStatusTranslationById((long) o.getNumValue())
                .orElseThrow(() -> new EntityNotFoundException(ORDER_STATUS_NOT_FOUND)).getNameEng();
            optionForColumnDTOS
                .add(OptionForColumnDTO.builder().key(o.toString()).ua(ua).en(en).filtered(false).build());
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> orderPaymentStatusListForDevelopStage() {
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        PaymentStatus[] paymentStatuses = PaymentStatus.values();
        for (PaymentStatus p : paymentStatuses) {
            OrderPaymentStatusTranslation orderPaymentStatusTranslation =
                orderPaymentStatusTranslationRepository.getOrderPaymentStatusTranslationById((long) p.getNumValue())
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_PAYMENT_STATUS_NOT_FOUND));
            optionForColumnDTOS.add(OptionForColumnDTO.builder()
                .key(p.name())
                .ua(orderPaymentStatusTranslation.getTranslationValue())
                .en(orderPaymentStatusTranslation.getTranslationsValueEng())
                .build());
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
            new TitleDto("EXPORT_ADDRESS", "Адреса вивезення", "export address"),
            new TitleDto("ORDERS_DETAILS", "Деталі замовлення", "orders details"),
            new TitleDto("EXPORT_DETAILS", "Деталі вивезення", "export details"),
            new TitleDto("RESPONSIBLE", "Відповідальні особи", "responsible persons"))));
    }

    private List<OptionForColumnDTO> receivingStationList() {
        List<ReceivingStationDto> receivingStations = superAdminService.getAllReceivingStations();
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (ReceivingStationDto r : receivingStations) {
            optionForColumnDTOS.add(modelMapper.map(r, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> callerList() {
        List<Employee> employeeList = employeeRepository.findAllByEmployeePositionId(2L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> logicManList() {
        List<Employee> employeeList = employeeRepository.findAllByEmployeePositionId(3L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> navigatorList() {
        List<Employee> employeeList = employeeRepository.findAllByEmployeePositionId(4L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    private List<OptionForColumnDTO> driverList() {
        List<Employee> employeeList = employeeRepository.findAllByEmployeePositionId(5L);
        List<OptionForColumnDTO> optionForColumnDTOS = new ArrayList<>();
        for (Employee e : employeeList) {
            optionForColumnDTOS.add(modelMapper.map(e, OptionForColumnDTO.class));
        }
        return optionForColumnDTOS;
    }

    /* methods for changing order */
    @Override
    public synchronized List<Long> orderStatusForDevelopStage(List<Long> ordersId, String value, Long employeeId) {
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            orderRepository.changeStatusForAllOrders(value, employeeId);
            orderRepository.unblockAllOrders(employeeId);
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                if (existedOrder.getOrderStatus().checkPossibleStatus(value)) {
                    existedOrder.setOrderStatus(OrderStatus.valueOf(value));
                    removePickUpDetailsAndResponsibleEmployees(existedOrder);
                } else {
                    throw new BadRequestException(
                        "Such desired status isn't applicable with current status!");
                }
                if (existedOrder.getOrderStatus() == OrderStatus.CANCELED
                    && (existedOrder.getPointsToUse() != 0 || !existedOrder.getCertificates().isEmpty())) {
                    notificationService.notifyBonusesFromCanceledOrder(existedOrder);
                    returnAllPointsFromOrder(existedOrder);
                }
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    private void returnAllPointsFromOrder(Order order) {
        Integer pointsToReturn = order.getPointsToUse();
        if (isNull(pointsToReturn) || pointsToReturn == 0) {
            return;
        }
        order.setPointsToUse(0);
        User user = order.getUser();
        if (isNull(user.getCurrentPoints())) {
            user.setCurrentPoints(0);
        }
        user.setCurrentPoints(user.getCurrentPoints() + pointsToReturn);
        ChangeOfPoints changeOfPoints = ChangeOfPoints.builder()
            .amount(pointsToReturn)
            .date(LocalDateTime.now())
            .user(user)
            .order(order)
            .build();
        if (isNull(user.getChangeOfPointsList())) {
            user.setChangeOfPointsList(new ArrayList<>());
        }
        user.getChangeOfPointsList().add(changeOfPoints);
        userRepository.save(user);
        if (!order.getCertificates().isEmpty()) {
            Set<Certificate> certificates = order.getCertificates();
            for (Certificate certificate : certificates) {
                certificate.setPoints(0);
                certificateRepository.save(certificate);
            }
        }
    }

    private void removePickUpDetailsAndResponsibleEmployees(Order order) {
        if (OrderStatus.FORMED.equals(order.getOrderStatus())
            || OrderStatus.BROUGHT_IT_HIMSELF.equals(order.getOrderStatus())
            || OrderStatus.NOT_TAKEN_OUT.equals(order.getOrderStatus())
            || OrderStatus.CANCELED.equals(order.getOrderStatus())) {
            order.setDateOfExport(null);
            order.setDeliverFrom(null);
            order.setDeliverTo(null);
            order.setReceivingStation(null);
            employeeOrderPositionRepository.deleteAll(order.getEmployeeOrderPositions());
            order.setEmployeeOrderPositions(null);
        }
    }

    private List<Long> cancellationReasonForDevelopStage(List<Long> ordersId, String value) {
        List<Long> unresolvedGoals = new ArrayList<>();
        if (EnumUtils.isValidEnum(CancellationReason.class, value)) {
            for (Long orderId : ordersId) {
                try {
                    Order existedOrder = orderRepository.findById(orderId)
                        .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                    orderRepository.updateCancelingReason(existedOrder.getId(), value);
                } catch (Exception e) {
                    unresolvedGoals.add(orderId);
                }
            }
        }
        return unresolvedGoals;
    }

    private List<Long> cancellationCommentForDevelopStage(List<Long> ordersId, String value, Long employeeId) {
        List<Long> unresolvedGoals = new ArrayList<>();
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                orderRepository.updateCancelingComment(orderId, value);
                eventService.saveEvent(OrderHistory.ORDER_CANCELLED + "  " + value, employee.getEmail(),
                    existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    @Override
    public synchronized List<Long> dateOfExportForDevelopStage(List<Long> ordersId, String value, Long employeeId) {
        LocalDate date = LocalDate.parse(value.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            orderRepository.changeDateOfExportForAllOrders(date, employeeId);
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

    @Override
    public synchronized List<Long> timeOfExportForDevelopStage(List<Long> ordersId, String value, Long employeeId) {
        String from = value.substring(0, 5);
        String to = value.substring(6);
        LocalDateTime timeFrom = LocalDateTime.of(LocalDate.now(), LocalTime.parse(from, DateTimeFormatter.ISO_TIME));
        LocalDateTime timeTo = LocalDateTime.of(LocalDate.now(), LocalTime.parse(to, DateTimeFormatter.ISO_TIME));
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            orderRepository.changeDeliverFromForAllOrders(timeFrom, employeeId);
            orderRepository.changeDeliverToForAllOrders(timeTo, employeeId);
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setDeliverFrom(timeFrom);
                existedOrder.setDeliverTo(timeTo);
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
    public synchronized List<Long> receivingStationForDevelopStage(List<Long> ordersId, String value, Long employeeId) {
        ReceivingStation station = receivingStationRepository.getOne(Long.parseLong(value));
        List<Long> unresolvedGoals = new ArrayList<>();
        if (ordersId.isEmpty()) {
            orderRepository.changeReceivingStationForAllOrders(station.getId(), employeeId);
        }
        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                existedOrder.setReceivingStation(station);
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
    public synchronized List<Long> responsibleEmployee(List<Long> ordersId, String employee, Long position,
        String email) {
        Employee existedEmployee = employeeRepository.findById(Long.parseLong(employee))
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST));
        Position existedPosition = positionRepository.findById(position)
            .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND_BY_ID));
        List<Long> unresolvedGoals = new ArrayList<>();

        for (Long orderId : ordersId) {
            try {
                Order existedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
                Boolean existedBefore =
                    employeeOrderPositionRepository.existsByOrderAndPosition(existedOrder, existedPosition);
                final String historyChanges;

                if (existedBefore.equals(Boolean.TRUE)) {
                    employeeOrderPositionRepository.update(existedOrder, existedEmployee, existedPosition);
                    historyChanges = eventService.changesWithResponsibleEmployee(existedPosition.getId(), Boolean.TRUE);
                } else {
                    List<EmployeeOrderPosition> employeeOrderPositions =
                        employeeOrderPositionRepository.findAllByOrderId(orderId);
                    EmployeeOrderPosition newEmployeeOrderPosition = EmployeeOrderPosition.builder()
                        .employee(existedEmployee).position(existedPosition)
                        .order(existedOrder).build();
                    employeeOrderPositions.add(newEmployeeOrderPosition);
                    Set<EmployeeOrderPosition> positionSet = new HashSet<>(employeeOrderPositions);
                    existedOrder.setEmployeeOrderPositions(positionSet);
                    historyChanges =
                        eventService.changesWithResponsibleEmployee(existedPosition.getId(), Boolean.FALSE);
                }
                existedOrder.setBlocked(false);
                existedOrder.setBlockedByEmployee(null);
                orderRepository.save(existedOrder);
                eventService.saveEvent(historyChanges, email, existedOrder);
            } catch (Exception e) {
                unresolvedGoals.add(orderId);
            }
        }
        return unresolvedGoals;
    }

    @Override
    public synchronized List<BlockedOrderDto> requestToBlockOrder(String userUuid, List<Long> orders) {
        String email = userRemoteClient.findByUuid(userUuid)
            .orElseThrow(() -> new EntityNotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST)).getEmail();
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        List<BlockedOrderDto> blockedOrderDTOS = new ArrayList<>();
        if (orders.isEmpty()) {
            orderRepository.setBlockedEmployeeForAllOrders(employee.getId());
        }
        for (Long orderId : orders) {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST));
            if (order.isBlocked() && !order.getBlockedByEmployee().equals(employee)) {
                blockedOrderDTOS.add(BlockedOrderDto
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
    public synchronized List<Long> unblockOrder(String userUuid, List<Long> orders) {
        String email = userRemoteClient.findByUuid(userUuid)
            .orElseThrow(() -> new EntityNotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST)).getEmail();
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        if (orders.isEmpty()) {
            orderRepository.unblockAllOrders(employee.getId());
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
