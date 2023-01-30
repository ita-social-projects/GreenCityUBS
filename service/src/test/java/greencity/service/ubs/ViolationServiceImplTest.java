package greencity.service.ubs;

import java.util.List;
import java.util.Optional;

import greencity.constant.ErrorMessage;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
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
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(violationRepository.findByOrderId(1l)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> violationService.deleteViolation(1L, "abc"));
        verify(violationRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void deleteViolationFromOrderByOrderId() {
        User user = ModelUtils.getTestUser();
        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        Violation violation = ModelUtils.getViolation2();
        Long id = ModelUtils.getViolation().getOrder().getId();
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.of(violation));
        doNothing().when(violationRepository).deleteById(1L);
        violationService.deleteViolation(1L, "abc");

        verify(violationRepository, times(1)).deleteById(id);
    }

    @Test
    void checkAddUserViolation() {
        Employee employee = ModelUtils.getEmployee();
        List<Long> tariffsInfos = List.of(1L, 2L, 3L);
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setTariffsInfo(tariffsInfo);
        order.setUser(user);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.ofNullable(order));
        when(userRepository.countTotalUsersViolations(1L)).thenReturn(1);
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(employee));
        when(employeeRepository.findTariffsInfoForEmployee(anyLong())).thenReturn(tariffsInfos);
        violationService.addUserViolation(add, new MultipartFile[2], "abc");

        verify(orderRepository, times(2)).findById(anyLong());
        verify(userRepository).countTotalUsersViolations(anyLong());
        verify(employeeRepository).findByEmail(anyString());
        verify(employeeRepository).findTariffsInfoForEmployee(anyLong());
        assertEquals(1, user.getViolations());
    }

    @Test
    void checkAddUserViolationThrowsException() {
        User user = ModelUtils.getTestUser();
        Order order = user.getOrders().get(0);
        order.setUser(user);
        AddingViolationsToUserDto add = ModelUtils.getAddingViolationsToUserDto();
        add.setOrderID(order.getId());
        when(orderRepository.findById(order.getId())).thenReturn(Optional.ofNullable(order));

        verify(orderRepository, never()).findById(anyLong());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
            () -> violationService.addUserViolation(add, new MultipartFile[2], "abc"));
        assertEquals(ErrorMessage.EMPLOYEE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void updateUserViolation() {
        User user = ModelUtils.getUser();
        UpdateViolationToUserDto updateViolationToUserDto = ModelUtils.getUpdateViolationToUserDto();
        Violation violation = ModelUtils.getViolation();
        List<String> violationImages = violation.getImages();

        when(userRepository.findUserByUuid("abc")).thenReturn(Optional.of(user));
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.of(violation));
        if (updateViolationToUserDto.getImagesToDelete() != null) {
            List<String> images = updateViolationToUserDto.getImagesToDelete();
            for (String image : images) {
                doNothing().when(fileService).delete(image);
                violationImages.remove(image);
            }
        }

        violationService.updateUserViolation(updateViolationToUserDto, new MultipartFile[2], "abc");

        assertEquals(2, violation.getImages().size());

        verify(userRepository).findUserByUuid("abc");
        verify(violationRepository).findByOrderId(1L);
    }

    @Test
    void returnsViolationDetailsByOrderId() {
        Violation violation = ModelUtils.getViolation();
        Optional<ViolationDetailInfoDto> expected = Optional.of(ModelUtils.getViolationDetailInfoDto());
        when(userRepository.findById(1L)).thenReturn(Optional.of(violation.getAddedByUser()));
        when(violationRepository.findByOrderId(1L)).thenReturn(Optional.of(violation));
        Optional<ViolationDetailInfoDto> actual = violationService.getViolationDetailsByOrderId(1L);

        assertEquals(expected, actual);
    }

}
