package greencity.service.ubs;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.enums.OrderStatus;
import greencity.enums.ViolationStatus;
import greencity.exceptions.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import greencity.ModelUtils;
import greencity.dto.violation.AddingViolationsToUserDto;
import greencity.dto.violation.UpdateViolationToUserDto;
import greencity.dto.violation.ViolationDetailInfoDto;
import greencity.enums.SortingOrder;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.Violation;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import greencity.repository.UserRepository;
import greencity.repository.UserViolationsTableRepo;
import greencity.repository.ViolationRepository;
import greencity.service.notification.NotificationServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViolationServiceImplTest {
    @Mock
    UserViolationsTableRepo userViolationsTableRepo;
    @Mock
    ViolationRepository violationRepository;
    @InjectMocks
    ViolationServiceImpl violationService;
    @Mock(lenient = true)
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    private FileService fileService;
    @Mock
    private EventService eventService;
    @Mock
    private NotificationServiceImpl notificationService;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void getAllViolations() {
        when(violationRepository.getNumberOfViolationsByUser(anyLong())).thenReturn(5L);
        when(userRepository.getOne(any())).thenReturn(ModelUtils.getUser());
        when(userViolationsTableRepo.findAll(anyLong(), anyString(), any(), any())).thenReturn(
            new PageImpl<>(List.of(ModelUtils.getViolation()),
                PageRequest.of(0, 5, Sort.by("id").descending()), 5));

        violationService.getAllViolations(Pageable.unpaged(), 1L, "violationDate", SortingOrder.ASC);
        assertEquals(violationService.getAllViolations(Pageable.unpaged(), 1L, "violationDate", SortingOrder.ASC)
            .getUserViolationsDto().getPage().get(0).getViolationDate(), ModelUtils.getViolation().getViolationDate());
    }

    @Test
    void deleteViolationThrowsException() {
        assertThrows(UserNotFoundException.class, () -> violationService.deleteViolation(1L, "abc"));
    }

    @Test
    void deleteViolationFromOrderResponsesNotFoundWhenNoViolationInOrder() {
        Employee employee = ModelUtils.getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(violationRepository.findActiveViolationByOrderId(1l)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> violationService.deleteViolation(1L, "abc"));
        verify(violationRepository, times(1)).findActiveViolationByOrderId(1L);
        verify(employeeRepository, times(1)).findByUuid(anyString());
    }

    @Test
    void deleteViolationFromOrderByOrderId() {
        Employee user = ModelUtils.getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(user));
        Violation violation = ModelUtils.getViolation2();
        when(violationRepository.findActiveViolationByOrderId(1L)).thenReturn(Optional.of(violation));
        violationService.deleteViolation(1L, "abc");

        verify(employeeRepository, times(1)).findByUuid(anyString());
        verify(employeeRepository).findByUuid(anyString());
        verify(violationRepository).save(violation);
        verify(notificationService).notifyDeleteViolation(1L);
        verify(userRepository).save(any(User.class));

        assertEquals(ViolationStatus.DELETED, violation.getViolationStatus());
        assertNotNull(violation.getDeleteDate());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DONE", "NOT_TAKEN_OUT"})
    void checkAddUserViolation(OrderStatus orderStatus) {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setUser(user);
        order.setOrderStatus(orderStatus);
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        order.setTariffsInfo(tariffsInfo);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(employee.getId())).thenReturn(List.of(tariffsInfo.getId()));
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(1);
        violationService.addUserViolation(add, new MultipartFile[2], employee.getEmail());

        assertEquals(1, user.getViolations());
    }

    @ParameterizedTest
    @MethodSource("provideCheckAddUserViolationThrowsException")
    void checkAddUserViolationThrowsException(OrderStatus orderStatus, Class<? extends Exception> c, String message,
        Violation violation) {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setUser(user);
        order.setOrderStatus(orderStatus);
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        order.setTariffsInfo(tariffsInfo);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(employee.getId())).thenReturn(List.of(tariffsInfo.getId()));
        if (violation != null) {
            when(violationRepository.findActiveViolationByOrderId(order.getId())).thenReturn(Optional.of(violation));
        }

        Exception ex =
            assertThrows(c, () -> violationService.addUserViolation(add, new MultipartFile[2], employee.getEmail()));
        assertEquals(message, ex.getMessage());
    }

    private static Stream<Arguments> provideCheckAddUserViolationThrowsException() {
        return Stream.of(
            Arguments.of(
                OrderStatus.ADJUSTMENT, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.ADJUSTMENT.name(), null),
            Arguments.of(
                OrderStatus.FORMED, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.FORMED.name(), null),
            Arguments.of(
                OrderStatus.BROUGHT_IT_HIMSELF, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.BROUGHT_IT_HIMSELF.name(), null),
            Arguments.of(
                OrderStatus.CONFIRMED, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.CONFIRMED.name(), null),
            Arguments.of(
                OrderStatus.ON_THE_ROUTE, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.ON_THE_ROUTE.name(), null),
            Arguments.of(
                OrderStatus.CANCELED, BadRequestException.class,
                ErrorMessage.INCOMPATIBLE_ORDER_STATUS_FOR_VIOLATION + OrderStatus.CANCELED.name(), null),
            Arguments.of(
                OrderStatus.DONE, NotFoundException.class, ErrorMessage.ORDER_ALREADY_HAS_VIOLATION,
                ModelUtils.getViolation()));
    }

    @Test
    void updateUserViolation() {
        Employee employee = ModelUtils.getEmployee();
        UpdateViolationToUserDto updateViolationToUserDto = ModelUtils.getUpdateViolationToUserDto();
        Violation violation = ModelUtils.getViolation();
        List<String> violationImages = violation.getImages();

        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(violationRepository.findActiveViolationByOrderId(1L)).thenReturn(Optional.of(violation));
        if (updateViolationToUserDto.getImagesToDelete() != null) {
            List<String> images = updateViolationToUserDto.getImagesToDelete();
            for (String image : images) {
                doNothing().when(fileService).delete(image);
                violationImages.remove(image);
            }
        }

        violationService.updateUserViolation(updateViolationToUserDto, new MultipartFile[2], "abc");

        assertEquals(2, violation.getImages().size());

        verify(employeeRepository).findByUuid("abc");
        verify(violationRepository).findActiveViolationByOrderId(1L);
    }

    @Test
    void returnsViolationDetailsByOrderId() {
        Violation violation = ModelUtils.getViolation();
        Optional<ViolationDetailInfoDto> expected = Optional.of(ModelUtils.getViolationDetailInfoDto());
        when(userRepository.findById(1L)).thenReturn(Optional.of(violation.getAddedByUser()));
        when(violationRepository.findActiveViolationByOrderId(1L)).thenReturn(Optional.of(violation));
        Optional<ViolationDetailInfoDto> actual = violationService.getViolationDetailsByOrderId(1L);

        assertEquals(expected, actual);
    }

    @Test
    void getViolationDetailsByOrderIdWhenOptionalViolationIsEmptyTest() {
        when(violationRepository.findActiveViolationByOrderId(anyLong())).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), violationService.getViolationDetailsByOrderId(1L));
    }

    @Test
    void checkAvailableOrderForEmployeeWhenStatusIsFalseTest() {
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        Order order = ModelUtils.getOrder();
        Employee employee = Employee.builder()
            .id(1L)
            .firstName("Петро")
            .lastName("Петренко")
            .phoneNumber("+380935577455")
            .email("test@gmail.com")
            .uuid("Test")
            .tariffs(List.of(
                TariffsInfo.builder()
                    .id(2L)
                    .service(ModelUtils.getService())
                    .build()))
            .imagePath("path")
            .build();

        order.setTariffsInfo(ModelUtils.getTariffsInfo());
        when(orderRepository.findById(anyLong())).thenReturn(Optional.ofNullable(order));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong()))
            .thenReturn(employee.getTariffs().stream().map(TariffsInfo::getId).collect(Collectors.toList()));
        assertThrows(BadRequestException.class, () -> {
            violationService.addUserViolation(add, new MultipartFile[2], "test@gmail.com");
        });
        verify(orderRepository, times(2)).findById(anyLong());
        verify(employeeRepository).findByEmail(anyString());
    }

    @Test
    void testAddUserViolationWithMultipartFiles() {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.DONE);
        order.setUser(user);
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        order.setTariffsInfo(tariffsInfo);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[1];
        multipartFiles[0] = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong())).thenReturn(Arrays.asList(1L, 2L));
        violationService.addUserViolation(add, multipartFiles, employee.getEmail());

        verify(orderRepository, times(2)).findById(anyLong());
        verify(employeeRepository).findTariffsInfoForEmployee(anyLong());
        verify(violationRepository).save(any(Violation.class));
        verify(userRepository).save(user);
        verify(eventService).saveEvent(OrderHistory.ADD_VIOLATION, employee.getEmail(), order);
        verify(notificationService).notifyAddViolation(order.getId());
    }

    @Test
    void testAddUserViolationWithoutMultipartFiles() {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.DONE);
        order.setUser(user);
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        order.setTariffsInfo(tariffsInfo);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong())).thenReturn(Arrays.asList(1L, 2L));
        violationService.addUserViolation(add, multipartFiles, employee.getEmail());

        verify(orderRepository, times(2)).findById(anyLong());
        verify(employeeRepository).findTariffsInfoForEmployee(anyLong());
        verify(violationRepository).save(any(Violation.class));
        verify(userRepository).save(user);
        verify(eventService).saveEvent(OrderHistory.ADD_VIOLATION, employee.getEmail(), order);
        verify(notificationService).notifyAddViolation(order.getId());
    }

    @Test
    void testUpdateViolationWhenImagesIsNotEmpty() {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        Violation violation = ModelUtils.getViolation();
        violation.setImages(Arrays.asList("img", "test"));
        MockMultipartFile[] multipartFiles = new MockMultipartFile[2];
        UpdateViolationToUserDto add = ModelUtils.getUpdateViolationToUserDto();
        multipartFiles[0] = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(violationRepository.findActiveViolationByOrderId(order.getId())).thenReturn(Optional.of(violation));
        violationService.updateUserViolation(add, multipartFiles, "uuid");

        verify(employeeRepository).findByUuid(anyString());
        verify(violationRepository).findActiveViolationByOrderId(order.getId());
    }

    @Test
    void testUpdateViolationWhenMultipartFilesAreEmpty() {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        Violation violation = ModelUtils.getViolation();
        violation.setImages(Arrays.asList("img", "test"));
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];
        UpdateViolationToUserDto add = ModelUtils.getUpdateViolationToUserDto();
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(violationRepository.findActiveViolationByOrderId(order.getId())).thenReturn(Optional.of(violation));

        violationService.updateUserViolation(add, multipartFiles, "uuid");
        verify(employeeRepository).findByUuid(anyString());
        verify(violationRepository).findActiveViolationByOrderId(order.getId());
    }

    @Test
    void testUpdateViolationWhenImagesToDeleteAreNull() {
        Employee employee = ModelUtils.getEmployee();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        Violation violation = ModelUtils.getViolation();
        violation.setImages(Arrays.asList("img", "test"));
        MockMultipartFile[] multipartFiles = new MockMultipartFile[0];

        UpdateViolationToUserDto add = UpdateViolationToUserDto.builder()
            .orderID(1L)
            .violationDescription("String1 string1 string1")
            .violationLevel("low")
            .imagesToDelete(null)
            .build();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(employee));
        when(violationRepository.findActiveViolationByOrderId(order.getId())).thenReturn(Optional.of(violation));

        violationService.updateUserViolation(add, multipartFiles, "uuid");
        verify(employeeRepository).findByUuid(anyString());
        verify(violationRepository).findActiveViolationByOrderId(order.getId());
    }

    @Test
    void testDeleteViolationWhenImagesAreNull() {
        Employee user = ModelUtils.getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(user));
        Violation violation = ModelUtils.getViolation();
        when(violationRepository.findActiveViolationByOrderId(1L)).thenReturn(Optional.of(violation));
        violationService.deleteViolation(1L, "abc");

        verify(employeeRepository, times(1)).findByUuid(anyString());
        verify(employeeRepository).findByUuid(anyString());
        verify(violationRepository).save(violation);

        assertEquals(ViolationStatus.DELETED, violation.getViolationStatus());
        assertNotNull(violation.getDeleteDate());
    }
}
