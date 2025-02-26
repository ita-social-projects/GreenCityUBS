package greencity.service.ubs;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.*;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import java.util.List;
import java.util.Optional;

public interface AddressService {
    /**
     * Gets address for given order id.
     *
     * @param orderId id of order
     * @return address with given order id
     * @author Dmytro Kizerov
     */
    UpdateAddressDto getAddressForOrder(Long orderId);

    /**
     * Method updates order address fields.
     *
     * @param orderAddressDtoUpdate the DTO that contains required data for address
     *                              update.
     * @return {@link OrderAddress} updated order's address.
     */
    OrderAddress updateOrderAddress(OrderAddressExportDetailsDtoUpdate orderAddressDtoUpdate);

    /**
     * Method updates order address. This method updates order address. It takes
     * {@link UpdateAddressDto} as an argument and updates the order address in the
     * database. It also checks if the address exists and if the user is authorized
     * to update the address.
     *
     * @param addressDto {@link UpdateAddressDto}
     * @param email      {@link String} the user's email
     * @author Kizerov Dmytro
     */
    void addressUpdate(UpdateAddressDto addressDto, String email);

    /**
     * Method that save address for current user.
     *
     * @param requestDto {@link CreateAddressRequestDto} information about address;
     * @param uuid       current {@link User}'s uuid;
     * @return {@link OrderAddressDtoRequest} contains all information needed for
     *         save address;
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto requestDto, String uuid);

    /**
     * Method that update address.
     *
     * @param dtoUpdate of {@link OrderAddressExportDetailsDtoUpdate} order id.
     * @param order     {@link Order}.
     * @param email     {@link String}.
     * @return {@link OrderAddressDtoResponse} that contains address.
     * @author Mahdziak Orest
     */
    Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
        String email);

    /**
     * Makes an address actual (default) for a given user, identified by his UUID.
     *
     * @param addressId - the ID of the address to make the default
     * @param uuid      - the UUID of the user whose address is being updated
     * @return an {@link AddressDto} object representing the updated address
     */
    AddressDto makeAddressActual(Long addressId, String uuid);

    /**
     * Method gets all districts in city.
     *
     * @param region - name of region
     * @param city   - name of city
     * @return {@link DistrictDto}
     */

    List<DistrictDto> getAllDistricts(String region, String city);

    /**
     * Retrieves all districts in Kyiv.
     *
     * @return List of all districts in Kyiv.
     */
    List<DistrictDto> getAllDistrictsForKyiv();

    /**
     * Method that update address for current user (if placeId is null updates only
     * addressComment).
     *
     * @param requestDto {@link OrderAddressDtoRequest} information about address;
     * @param uuid       current {@link User}'s uuid;
     * @return {@link OrderAddressDtoRequest} contains all information needed for
     *         update address;
     * @author Oleg Postolovskyi
     */
    OrderWithAddressesResponseDto updateCurrentAddressForOrder(OrderAddressDtoRequest requestDto, String uuid);

    /**
     * Method that delete user address.
     *
     * @param addressId of {@link Long} address id;
     * @param uuid      current {@link User}'s uuid;
     * @return {@link OrderWithAddressesResponseDto} that contains address list;
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid);

    /**
     * Methods return list of all user addresses.
     *
     * @param uuid current {@link User}'s uuid;
     * @return {@link OrderWithAddressesResponseDto} that contains address list
     * @author Veremchuk Zakhar
     */
    OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid);

    /**
     * Method that read user address by order id.
     *
     * @param orderId of {@link Long} order id;
     * @return {@link ReadAddressByOrderDto} that contains one address;
     * @author Mahdziak Orest
     */
    ReadAddressByOrderDto getAddressByOrderId(Long orderId);
}
