package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.dto.address.UpdateAddressDto;
import greencity.entity.user.ubs.OrderAddress;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderAddressRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {
    @Mock
    private OrderAddressRepository orderAddressRepository;
    @Mock
    private ModelMapper mapper;
    @InjectMocks
    private AddressServiceImpl addressService;

    @Test
    void getAddressForOrderTest() {
        Long orderId = 1L;
        OrderAddress orderAddress = ModelUtils.getOrderAddress1();

        when(orderAddressRepository.findByOrderId(orderId)).thenReturn(Optional.of(orderAddress));
        when(mapper.map(orderAddress, UpdateAddressDto.class)).thenReturn(ModelUtils.getUpdateAddressDto());

        var result = addressService.getAddressForOrder(orderId);

        assertEquals(orderId, result.getOrderId());
        assertEquals(orderAddress.getId(), result.getOrderAddressExportDetails().getId());

        verify(orderAddressRepository).findByOrderId(orderId);
    }

    @Test
    void getNotExistingAddressForOrderTest() {
        Long orderId = -1L;

        when(orderAddressRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.getAddressForOrder(orderId));

        verify(orderAddressRepository).findByOrderId(orderId);
    }
}
