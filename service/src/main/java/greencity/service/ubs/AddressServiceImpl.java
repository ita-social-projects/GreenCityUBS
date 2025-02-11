package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.address.UpdateAddressDto;
import greencity.entity.user.ubs.OrderAddress;
import greencity.exceptions.NotFoundException;
import greencity.repository.OrderAddressRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final OrderAddressRepository orderAddressRepository;
    private final ModelMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateAddressDto getAddressForOrder(Long orderId) {
        OrderAddress orderAddress = orderAddressRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId));
        UpdateAddressDto addressDto = mapper.map(orderAddress, UpdateAddressDto.class);
        addressDto.setOrderId(orderId);
        return addressDto;
    }
}
