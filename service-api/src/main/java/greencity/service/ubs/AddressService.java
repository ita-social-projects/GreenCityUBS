package greencity.service.ubs;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.LocationsDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.OrderAddress;
import greencity.exceptions.NotFoundException;
import java.util.List;

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
     * @return {@link OrderAddressDtoResponse} that contains address.
     * @author Kizerov Dmytro
     */
    OrderAddressDtoResponse addressUpdate(UpdateAddressDto addressDto, String email);

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
    OrderAddressDtoResponse updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
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

    /**
     * Checks if the given address belongs to the specified location area.
     * - For Kyiv tariff: verifies if the address city is part of Kyiv.
     * - For Kyiv region 20 km tariff: calculates the distance from Kyiv
     *   and ensures the address lies within the allowed radius.
     * - For other tariffs: verifies by database match of location and address IDs.
     *
     * @param locationId the location ID to validate against
     * @param addressId  the ID of the address to check
     * @return true if the address belongs to the location area, false otherwise
     */
    boolean checkIfAddressMatchLocationArea(long locationId, long addressId);

    /**
     * Forms a new {@link OrderAddress} from the provided address and location,
     * validates ownership and deletion status, and saves it in the database.
     *
     * @param addressId   the ID of the address
     * @param locationId  the ID of the location
     * @param currentUser the current authenticated user who owns the address
     * @return the saved {@link OrderAddress} entity
     * @throws NotFoundException if address or location does not exist,
     *                           or if the address does not belong to the user
     */
    OrderAddress formAndSaveOrderAddress(Long addressId, Long locationId, User currentUser);

    /**
     * Updates the order address if the provided new address and location differ
     * from the current one. If they match, keeps the existing address.
     *
     * @param currentOrderAddress the current {@link OrderAddress} used in the order
     * @param newAddressId        the ID of the new address
     * @param newLocationId       the ID of the new location
     * @param currentUser         the current authenticated user
     * @return the updated or existing {@link OrderAddress}
     * @throws NotFoundException if the new address or location is invalid or not owned by the user
     */
    OrderAddress getOrUpdateOrderAddress(OrderAddress currentOrderAddress,
                                         Long newAddressId,
                                         Long newLocationId,
                                         User currentUser);

    /**
     * Retrieves all locations.
     *
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocations();

    /**
     * Retrieves all active locations by courier id.
     *
     * @param courierId The ID of the courier for which to retrieve all active the
     *                  locations.
     * @return List of all locations.
     */
    List<LocationsDto> getAllLocationsByCourierId(Long courierId);
}
