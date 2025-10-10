package greencity.service.ubs;

import static greencity.constant.ErrorMessage.ACTUAL_ADDRESS_NOT_FOUND;
import static greencity.constant.ErrorMessage.ADDRESS_ALREADY_EXISTS;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PERSONAL_INFO;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_DELETE_ALREADY_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.CANNOT_MAKE_ACTUAL_DELETED_ADDRESS;
import static greencity.constant.ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_BY_ID;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_BY_ORDER_ID;
import static greencity.constant.ErrorMessage.NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER;
import static greencity.constant.ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.google.maps.model.LatLng;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.constant.KyivTariffLocation;
import greencity.constant.OrderHistory;
import greencity.constant.TariffLocation;
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
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.AddressStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.mapping.location.AddressRequestDtoToBaseEntityMapper;
import greencity.mapping.location.LocationToLocationsDtoMapper;
import greencity.repository.AddressRepository;
import greencity.repository.CityRepository;
import greencity.repository.CourierRepository;
import greencity.repository.DistrictRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderRepository;
import greencity.repository.RegionRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.DistanceCalculationUtils;
import greencity.service.google.GoogleApiService;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private static final String KYIV_CITY = "Kyiv City";
    private static final String KYIV = "Kyiv";
    private final OrderAddressRepository orderAddressRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepo;
    private final OrderRepository orderRepository;
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final GoogleApiService googleApiService;
    private final LocationToLocationsDtoMapper locationToLocationsDtoMapper;
    private final EventService eventService;
    private final AddressRequestDtoToBaseEntityMapper baseEntityMapper;
    private final ModelMapper modelMapper;
    private static final Integer MAXIMUM_NUMBER_OF_ADDRESSES = 4;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UpdateAddressDto getAddressForOrder(Long orderId) {
        OrderAddress orderAddress = orderAddressRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId));
        UpdateAddressDto addressDto = modelMapper.map(orderAddress, UpdateAddressDto.class);
        addressDto.setOrderId(orderId);
        return addressDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderAddress updateOrderAddress(OrderAddressExportDetailsDtoUpdate orderAddressDtoUpdate) {
        CreateAddressRequestDto createAddressRequestDto =
            modelMapper.map(orderAddressDtoUpdate, CreateAddressRequestDto.class);
        Address address = modelMapper.map(orderAddressDtoUpdate, Address.class);
        setLocations(createAddressRequestDto, address);
        return modelMapper.map(address, OrderAddress.class);
    }

    @Override
    @Transactional
    public OrderAddressDtoResponse addressUpdate(UpdateAddressDto addressDto, String email) {
        Order order = orderRepository.findById(addressDto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + addressDto.getOrderId()));
        return updateAddress(addressDto.getOrderAddressExportDetails(), order, email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto addressRequestDto,
        String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST + uuid));
        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        if (addresses.size() == MAXIMUM_NUMBER_OF_ADDRESSES) {
            throw new BadRequestException(NUMBER_OF_ADDRESSES_EXCEEDED);
        }
        Optional<Address> addressIfExist = checkIfAddressExist(currentUser.getId(), addressRequestDto);
        if (addressIfExist.isPresent()) {
            Address existingAddress = addressIfExist.get();
            existingAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);
            addressRepo.save(existingAddress);
        } else {
            Address address = modelMapper.map(addressRequestDto, Address.class);
            setLocations(addressRequestDto, address);
            address.getBaseAddress().setAddressStatus(AddressStatus.NEW);
            address.setUser(currentUser);
            address.getBaseAddress().setActual(addresses.isEmpty());
            addressRepo.save(address);
        }
        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public ReadAddressByOrderDto getAddressByOrderId(Long orderId) {
        if (orderRepository.findById(orderId).isEmpty()) {
            throw new NotFoundException(NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId);
        }
        OrderAddress orderAddress = orderAddressRepository.findByOrderId(orderId)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_ADDRESS_BY_ORDER_ID + orderId));
        return modelMapper.map(orderAddress, ReadAddressByOrderDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderAddressDtoResponse updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
        String email) {
        OrderAddress orderAddress = orderAddressRepository.findById(dtoUpdate.getId())
            .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_ADDRESS_BY_ID, dtoUpdate.getId())));
        OrderAddress updatedOrderAddress = updateOrderAddress(dtoUpdate);
        mapUpdatedOrderAddressFields(orderAddress, updatedOrderAddress, dtoUpdate.getAddressComment());
        orderAddressRepository.save(updatedOrderAddress);
        eventService.saveEvent(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE_UK, email, order);
        return modelMapper.map(updatedOrderAddress, OrderAddressDtoResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AddressDto makeAddressActual(Long addressId, String uuid) {
        Address currentAddress = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));

        if (!currentAddress.getUser().getUuid().equals(uuid)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }

        if (currentAddress.getBaseAddress().getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS);
        }

        if (Boolean.FALSE.equals(currentAddress.getBaseAddress().getActual())) {
            Address address =
                addressRepo.findByUserIdAndBaseAddress_ActualTrue(currentAddress.getUser().getId()).orElseThrow(
                    () -> new NotFoundException(ACTUAL_ADDRESS_NOT_FOUND));
            address.getBaseAddress().setActual(false);
            currentAddress.getBaseAddress().setActual(true);
        }

        return modelMapper.map(currentAddress, AddressDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DistrictDto> getAllDistricts(String region, String city) {
        List<District> districts = districtRepository.findAllByCityId(cityRepository.findIdByNameUkOrNameEn(city));
        return districts.stream().map(p -> modelMapper.map(p, DistrictDto.class))
            .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DistrictDto> getAllDistrictsForKyiv() {
        Long cityId = cityRepository.findIdByCityNameEnIgnoreCase(AppConstant.KYIV)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CITY_NOT_FOUND));
        return districtRepository.findAllByCityId(cityId).stream()
            .filter(district -> !KYIV_CITY.equalsIgnoreCase(district.getNameEn())
                && !KYIV.equalsIgnoreCase(district.getNameEn()))
            .collect(toMap(
                District::getNameUk,
                district -> district,
                (existing, replacement) -> existing))
            .values()
            .stream()
            .map(district -> DistrictDto.builder()
                .nameUk(district.getNameUk())
                .nameEn(district.getNameEn())
                .build())
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto updateCurrentAddressForOrder(OrderAddressDtoRequest addressRequestDto,
        String uuid) {
        User currentUser = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));

        Address address = addressRepo.findById(addressRequestDto.getId())
            .orElseThrow(() -> new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressRequestDto.getId()));

        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }

        Optional<Address> addressIfExist = checkIfAddressExist(currentUser.getId(), addressRequestDto);

        if (addressIfExist.isEmpty()) {
            Address newAddress = modelMapper.map(addressRequestDto, Address.class);

            setLocations(addressRequestDto, newAddress);

            newAddress.setId(addressRequestDto.getId());
            newAddress.setUser(address.getUser());
            newAddress.getBaseAddress().setAddressStatus(address.getBaseAddress().getAddressStatus());
            newAddress.getBaseAddress().setActual(address.getBaseAddress().getActual());

            addressRepo.save(newAddress);
        } else {
            address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
            Address existingAddress = addressIfExist.get();
            existingAddress.getBaseAddress().setAddressStatus(AddressStatus.NEW);
            addressRepo.save(existingAddress);
            addressRepo.save(address);
        }
        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto deleteCurrentAddressForOrder(Long addressId, String uuid) {
        Address address = addressRepo.findById(addressId).orElseThrow(
            () -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        if (!Objects.equals(address.getUser().getUuid(), uuid)) {
            throw new AccessDeniedException(CANNOT_DELETE_ADDRESS);
        }
        if (address.getBaseAddress().getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_DELETE_ALREADY_DELETED_ADDRESS);
        }
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);

        if (Boolean.TRUE.equals(address.getBaseAddress().getActual())) {
            address.getBaseAddress().setActual(false);
            addressRepo.findAnyByUserIdAndAddressStatusNotDeleted(address.getUser().getId())
                .ifPresent(newActualAddress -> newActualAddress.getBaseAddress().setActual(true));
        }

        return findAllAddressesForCurrentOrder(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        Long id = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST)).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllNonDeletedAddressesByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .map(u -> modelMapper.map(u, AddressDto.class))
            .toList();
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    @Override
    @Transactional
    public boolean checkIfAddressMatchLocationArea(long locationId, long addressId) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(AppConstant.ADDRESS_NOT_FOUND_BY_ID_MESSAGE + addressId));

        boolean isKyivTariff = checkIfCityBelongsToKyivTariff(address.getBaseAddress().getCityEn());

        if (locationId == TariffLocation.KYIV_TARIFF.getLocationId()) {
            return isKyivTariff;
        } else if (locationId == TariffLocation.KYIV_REGION_20_KM_TARIFF.getLocationId()) {
            checkAndCalculateAddressCoordinatesIfEmpty(address);

            double addressLatitude = address.getCoordinates().getLatitude();
            double addressLongitude = address.getCoordinates().getLongitude();

            double distanceInKm =
                DistanceCalculationUtils.calculateDistanceInKmByHaversineFormula(AppConstant.KYIV_LATITUDE,
                    AppConstant.KYIV_LONGITUDE,
                    addressLatitude, addressLongitude);

            return distanceInKm <= AppConstant.LOCATION_40_KM_ZONE_VALUE && !isKyivTariff;
        } else {
            return locationRepository.findAddressAndLocationNamesMatch(locationId, addressId).isPresent();
        }
    }

    @Override
    @Transactional
    public OrderAddress formAndSaveOrderAddress(Long addressId, Long locationId, User currentUser) {
        return orderAddressRepository.save(formOrderAddress(addressId, locationId, currentUser));
    }

    @Override
    @Transactional
    public OrderAddress getOrUpdateOrderAddress(OrderAddress currentOrderAddress, Long newAddressId, Long newLocationId,
        User currentUser) {
        OrderAddress newOrderAddress = formOrderAddress(
            newAddressId, newLocationId, currentUser);
        newOrderAddress.setId(currentOrderAddress.getId());

        if (currentOrderAddress.equals(newOrderAddress)) {
            return currentOrderAddress;
        }
        return orderAddressRepository.save(newOrderAddress);
    }

    @Override
    public List<LocationsDto> getAllLocations() {
        List<Location> allActiveLocations = locationRepository.findAllActiveLocations();
        return allActiveLocations.stream().map(locationToLocationsDtoMapper::convert).toList();
    }

    @Override
    public List<LocationsDto> getAllLocationsByCourierId(Long courierId) {
        if (!courierRepository.existsCourierById(courierId)) {
            throw new NotFoundException(COURIER_IS_NOT_FOUND_BY_ID + courierId);
        }
        List<Location> locations = locationRepository.findAllActiveLocationsByCourierId(courierId);
        return locations.stream()
            .map(locationToLocationsDtoMapper::convert)
            .map(locationsDto -> locationsDto.setTariffsId(
                tariffsInfoRepository.findTariffIdByLocationIdAndCourierId(locationsDto.getId(), courierId)
                    .orElseThrow(() -> new NotFoundException(
                        String.format(TARIFF_FOR_COURIER_AND_LOCATION_NOT_EXIST, locationsDto.getId(), courierId)))))
            .toList();
    }

    private void setLocations(CreateAddressRequestDto addressRequestDto, Address address) {
        Optional<Region> optionalRegion =
            regionRepository.findRegionByNameEnOrNameUk(address.getBaseAddress().getRegionEn(),
                address.getBaseAddress().getRegionUk());
        if (optionalRegion.isPresent()) {
            address.setRegionId(optionalRegion.get());

            Optional<City> optionalCity = cityRepository
                .findCityByRegionIdAndNameUkAndNameEn(optionalRegion.get().getId(),
                    address.getBaseAddress().getCityUk(),
                    address.getBaseAddress().getCityEn());
            City city;
            if (optionalCity.isPresent()) {
                city = optionalCity.get();
            } else {
                city = baseEntityMapper.convert(addressRequestDto, City.class);
                city.setRegion(optionalRegion.get());
                city = cityRepository.save(city);
            }

            address.setCityId(city);

            Optional<District> optionalDistrict = districtRepository
                .findDistrictByCityIdAndNameEnOrNameUk(city.getId(), address.getBaseAddress().getDistrictEn(),
                    address.getBaseAddress().getDistrictUk());
            if (optionalDistrict.isPresent()) {
                address.setDistrictId(optionalDistrict.get());
            } else {
                District district =
                    District.builder()
                        .nameUk(addressRequestDto.getDistrictUk())
                        .nameEn(addressRequestDto.getDistrictEn())
                        .build();
                district.setCity(city);
                District savedDistrict = districtRepository.save(district);
                address.setDistrictId(savedDistrict);
            }
        } else {
            throw new BadRequestException(ErrorMessage.REGION_NOT_FOUND);
        }
    }

    private <T extends CreateAddressRequestDto> Optional<Address> checkIfAddressExist(Long userId,
        T addressRequestDto) {
        List<Address> addresses = addressRepo.findAllByUserId(userId);
        boolean exist = addresses.stream()
            .filter(address -> !AddressStatus.DELETED.equals(address.getBaseAddress().getAddressStatus()))
            .map(address -> modelMapper.map(address, CreateAddressRequestDto.class))
            .anyMatch(
                addressDto -> addressDto.equals(modelMapper.map(addressRequestDto, CreateAddressRequestDto.class)));

        if (exist) {
            throw new BadRequestException(ADDRESS_ALREADY_EXISTS);
        }

        return addresses.stream()
            .filter(address -> AddressStatus.DELETED.equals(address.getBaseAddress().getAddressStatus()))
            .filter(address -> addressRequestDto
                .areAddressesEqual((modelMapper.map(address, CreateAddressRequestDto.class))))
            .findFirst();
    }

    private void mapUpdatedOrderAddressFields(OrderAddress orderAddress, OrderAddress updatedOrderAddress,
        String comment) {
        updatedOrderAddress.setLocation(orderAddress.getLocation());
        updatedOrderAddress.setId(orderAddress.getId());
        updatedOrderAddress.getBaseAddress().setActual(orderAddress.getBaseAddress().getActual());
        updatedOrderAddress.getBaseAddress().setAddressComment(comment);
        updatedOrderAddress.setCoordinates(orderAddress.getCoordinates());
        updatedOrderAddress.getBaseAddress().setAddressStatus(orderAddress.getBaseAddress().getAddressStatus());
    }

    private boolean checkIfCityBelongsToKyivTariff(String cityName) {
        return Arrays.stream(KyivTariffLocation.values())
            .anyMatch(kyivTariffLocation -> kyivTariffLocation.getLocationName().equalsIgnoreCase(cityName));
    }

    private void checkAndCalculateAddressCoordinatesIfEmpty(Address address) {
        if (address.getCoordinates().getLatitude() == 0.0 && address.getCoordinates().getLongitude() == 0.0) {
            LatLng latLng = googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(AppConstant.UKRAINE_EN,
                    address.getBaseAddress().getCityEn(),
                    AppConstant.LANG_EN).geometry.location;
            Coordinates addressCoordinates = Coordinates.builder().latitude(latLng.lat).longitude(latLng.lng).build();
            address.setCoordinates(addressCoordinates);
            addressRepo.save(address);
        }
    }

    private OrderAddress formOrderAddress(Long addressId, Long locationId, User currentUser) {
        Address address = addressRepo.findById(addressId)
            .orElseThrow(() -> new NotFoundException(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId));
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfAddressHasBeenDeleted(address);
        checkAddressUser(address, currentUser);

        OrderAddress orderAddress = modelMapper.map(address, OrderAddress.class);
        orderAddress.setLocation(location);

        return orderAddress;
    }

    private void checkIfAddressHasBeenDeleted(Address address) {
        if (address.getBaseAddress().getAddressStatus().equals(AddressStatus.DELETED)) {
            throw new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }

    private void checkAddressUser(Address address, User user) {
        if (!address.getUser().equals(user)) {
            throw new NotFoundException(
                NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + address.getId());
        }
    }
}
