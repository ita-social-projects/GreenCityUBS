package greencity.service.ubs;

import greencity.dto.address.UpdateAddressDto;

public interface AddressService {
    /**
     * Gets address for given order id.
     *
     * @param orderId id of order
     * @return address with given order id
     * @author Dmytro Kizerov
     */
    UpdateAddressDto getAddressForOrder(Long orderId);
}
