package greencity.service;

import greencity.ModelUtils;
import static greencity.ModelUtils.*;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.CertificateStatus;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSClientServiceImpl;

import java.util.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import javax.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class UBSClientServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private UBSuserRepository ubsUserRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    EntityManager entityManager;
    @Mock
    private RestClient restClient;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    UBSClientServiceImpl ubsService;

    @Test
    void getSecondPageData() {
        PersonalDataDto expected = ModelUtils.getOrderResponseDto().getPersonalData();
        User user = new User();
        user.setId(13L);

        when(userRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(user);
        when(ubsUserRepository.getAllByUserId(anyLong()))
            .thenReturn(Collections.singletonList(ModelUtils.getUBSuser()));
        when(modelMapper.map(ModelUtils.getUBSuser(), PersonalDataDto.class)).thenReturn(expected);

        assertEquals(expected, ubsService.getSecondPageData("35467585763t4sfgchjfuyetf").get(0));
    }

    @Test
    void checkCertificate() {
        when(certificateRepository.findById("certificate")).thenReturn(Optional.of(Certificate.builder()
            .code("certificate")
            .certificateStatus(CertificateStatus.ACTIVE)
            .build()));

        assertEquals("ACTIVE", ubsService.checkCertificate("certificate").getCertificateStatus());
    }

    @Test
    void checkCertificateWithNoAvailable() {
        Assertions.assertThrows(CertificateNotFoundException.class, () -> {
            ubsService.checkCertificate("randomstring").getCertificateStatus();
        });
    }

    @Test
    void getAllOrdersDoneByUser() {
        Order order = getOrderDoneByUser();
        OrderClientDto dto = getOrderClientDto();
        List<Order> orderList = Collections.singletonList(order);
        List<OrderClientDto> expected = Collections.singletonList(dto);

        when(orderRepository.getAllOrdersOfUser(anyString()))
            .thenReturn(orderList);
        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(dto);

        List<OrderClientDto> result = ubsService.getAllOrdersDoneByUser(anyString());

        assertEquals(expected, result);
    }

    @Test
    void cancelFormedOrder() {
        Order order = getOrderDoneByUser();
        order.setOrderStatus(OrderStatus.FORMED);
        OrderClientDto expected = getOrderClientDto();
        expected.setOrderStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderClientDto.class)).thenReturn(expected);

        OrderClientDto result = ubsService.cancelFormedOrder(1L);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);

        assertEquals(expected, result);
    }

    @Test
    void cancelFormedOrderShouldThrowOrderNotFoundException() {
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.cancelFormedOrder(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
    }

    @Test
    void cancelFormedOrderShouldThrowBadOrderStatusException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDoneByUser()));
        Exception thrown = assertThrows(BadOrderStatusRequestException.class,
            () -> ubsService.cancelFormedOrder(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + getOrderDoneByUser().getOrderStatus());
    }

    @Test
    void makeOrderAgain() {
        List<OrderBagDto> dto = Collections.singletonList(getOrderBagDto());
        when(entityManager.find(Order.class, 1L)).thenReturn(getOrderDoneByUser());
        when(modelMapper.map(any(Order.class), eq(new TypeToken<List<OrderBagDto>>() {
        }.getType()))).thenReturn(dto);

        List<OrderBagDto> result = ubsService.makeOrderAgain(1L);

        assertEquals(dto, result);
        verify(entityManager, times(1)).find(Order.class, 1L);
    }

    @Test
    void makeOrderAgainShouldThrowOrderNotFoundException() {
        Exception thrown = assertThrows(OrderNotFoundException.class,
            () -> ubsService.makeOrderAgain(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);
    }

    @Test
    void makeOrderAgainShouldThrowBadOrderStatusException() {
        Order order = getOrderDoneByUser();
        order.setOrderStatus(OrderStatus.CANCELLED);
        when(entityManager.find(Order.class, 1L)).thenReturn(order);
        Exception thrown = assertThrows(BadOrderStatusRequestException.class,
            () -> ubsService.makeOrderAgain(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.BAD_ORDER_STATUS_REQUEST
            + order.getOrderStatus());
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderIdThrowOrderNotFoundException() {
        when(orderRepository.findById(1L))
            .thenThrow(OrderNotFoundException.class);
        assertThrows(OrderNotFoundException.class,
            () -> ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L));
    }

    @Test
    void getsUserAndUserUbsAndViolationsInfoByOrderId() {
        UserInfoDto expected = ModelUtils.getUserInfoDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrderDetails()));
        UserInfoDto actual = ubsService.getUserAndUserUbsAndViolationsInfoByOrderId(1L);

        assertEquals(expected, actual);
    }

    @Test
    void updatesRecipientsInfoThrowUBSuserNotFoundException() {
        UbsCustomersDtoUpdate ubsCustomersDtoUpdate = ModelUtils.getUbsCustomersDtoUpdate();
        when(orderRepository.findById(ubsCustomersDtoUpdate.getId()))
            .thenThrow(UBSuserNotFoundException.class);
        assertThrows(UBSuserNotFoundException.class,
            () -> ubsService.updateUbsUserInfoInOrder(ubsCustomersDtoUpdate));
    }
}
