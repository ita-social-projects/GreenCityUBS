package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.entity.order.Order;
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
import greencity.repository.AddressRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.DistrictRepository;
import greencity.repository.RegionRepository;
import greencity.repository.CityRepository;
import greencity.repository.UserRepository;
import greencity.repository.OrderRepository;
import greencity.service.locations.LocationApiService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static greencity.constant.ErrorMessage.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private static final Long CITY_ID_KIEV = 3L;
    private static final String KYIV_CITY = "Kyiv City";
    private final OrderAddressRepository orderAddressRepository;
    private final DistrictRepository districtRepository;
    private final CityRepository cityRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepo;
    private final OrderRepository orderRepository;
    private final EventService eventService;
    private final LocationApiService locationApiService;
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
    public void addressUpdate(UpdateAddressDto addressDto, String email) {
        Order order = orderRepository.findById(addressDto.getOrderId())
            .orElseThrow(() -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + addressDto.getOrderId()));
        updateAddress(addressDto.getOrderAddressExportDetails(), order, email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderWithAddressesResponseDto saveCurrentAddressForOrder(CreateAddressRequestDto addressRequestDto,
        String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        List<Address> addresses = addressRepo.findAllNonDeletedAddressesByUserId(currentUser.getId());

        if (addresses.size() == MAXIMUM_NUMBER_OF_ADDRESSES) {
            throw new BadRequestException(NUMBER_OF_ADDRESSES_EXCEEDED);
        }
        Optional<Address> addressIfExist = checkIfAddressExist(currentUser.getId(), addressRequestDto);
        if (addressIfExist.isPresent()) {
            Address existingAddress = addressIfExist.get();
            existingAddress.getAddress().setAddressStatus(AddressStatus.NEW);
            addressRepo.save(existingAddress);
        } else {
            Address address = modelMapper.map(addressRequestDto, Address.class);
            setLocations(addressRequestDto, address);
            address.getAddress().setAddressStatus(AddressStatus.NEW);
            address.setUser(currentUser);
            address.getAddress().setActual(addresses.isEmpty());
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
    public Optional<OrderAddressDtoResponse> updateAddress(OrderAddressExportDetailsDtoUpdate dtoUpdate, Order order,
        String email) {
        OrderAddress orderAddress = orderAddressRepository.findById(dtoUpdate.getId())
            .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_ADDRESS_BY_ID, dtoUpdate.getId())));
        OrderAddress updatedOrderAddress = updateOrderAddress(dtoUpdate);
        mapUpdatedOrderAddressFields(orderAddress, updatedOrderAddress, dtoUpdate.getAddressComment());
        orderAddressRepository.save(updatedOrderAddress);
        eventService.saveEvent(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE_UK, email, order);
        return Optional.of(modelMapper.map(updatedOrderAddress, OrderAddressDtoResponse.class));
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

        if (currentAddress.getAddress().getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS);
        }

        if (Boolean.FALSE.equals(currentAddress.getAddress().getActual())) {
            Address address = addressRepo.findByUserIdAndAddress_ActualTrue(currentAddress.getUser().getId()).orElseThrow(
                () -> new NotFoundException(ACTUAL_ADDRESS_NOT_FOUND));
            address.getAddress().setActual(false);
            currentAddress.getAddress().setActual(true);
        }

        return modelMapper.map(currentAddress, AddressDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DistrictDto> getAllDistricts(String region, String city) {
        List<LocationDto> locationDtos = locationApiService.getAllDistrictsInCityByNames(region, city);
        return locationDtos.stream().map(p -> modelMapper.map(p, DistrictDto.class))
            .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DistrictDto> getAllDistrictsForKyiv() {
        return districtRepository.findAllByCityId(CITY_ID_KIEV).stream()
            .filter(district -> !KYIV_CITY.equalsIgnoreCase(district.getNameEn()))
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
        User currentUser = userRepository.findByUuid(uuid);

        if (Objects.isNull(currentUser)) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }

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
            newAddress.getAddress().setAddressStatus(address.getAddress().getAddressStatus());
            newAddress.getAddress().setActual(address.getAddress().getActual());

            addressRepo.save(newAddress);
        } else {
            address.getAddress().setAddressStatus(AddressStatus.DELETED);
            Address existingAddress = addressIfExist.get();
            existingAddress.getAddress().setAddressStatus(AddressStatus.NEW);
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
        if (address.getAddress().getAddressStatus() == AddressStatus.DELETED) {
            throw new BadRequestException(CANNOT_DELETE_ALREADY_DELETED_ADDRESS);
        }
        address.getAddress().setAddressStatus(AddressStatus.DELETED);

        if (Boolean.TRUE.equals(address.getAddress().getActual())) {
            address.getAddress().setActual(false);
            addressRepo.findAnyByUserIdAndAddressStatusNotDeleted(address.getUser().getId())
                .ifPresent(newActualAddress -> newActualAddress.getAddress().setActual(true));
        }

        return findAllAddressesForCurrentOrder(uuid);
    }

    private void setLocations(CreateAddressRequestDto addressRequestDto, Address address) {
        Optional<Region> optionalRegion =
            regionRepository.findRegionByNameEnOrNameUk(address.getAddress().getRegionEn(), address.getAddress().getRegionUk());
        if (optionalRegion.isPresent()) {
            address.setRegionId(optionalRegion.get());

            Optional<City> optionalCity = cityRepository
                .findCityByRegionIdAndNameUkAndNameEn(optionalRegion.get().getId(), address.getAddress().getCityUk(),
                    address.getAddress().getCityEn());

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
                .findDistrictByCityIdAndNameEnOrNameUk(city.getId(), address.getAddress().getDistrictEn(), address.getAddress().getDistrictUk());
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
            .filter(address -> !address.getAddress().getAddressStatus().equals(AddressStatus.DELETED))
            .map(address -> modelMapper.map(address, CreateAddressRequestDto.class))
            .anyMatch(
                addressDto -> addressDto.equals(modelMapper.map(addressRequestDto, CreateAddressRequestDto.class)));

        if (exist) {
            throw new BadRequestException(ADDRESS_ALREADY_EXISTS);
        }

        return addresses.stream()
            .filter(address -> AddressStatus.DELETED.equals(address.getAddress().getAddressStatus()))
            .filter(address -> addressRequestDto
                .areAddressesEqual((modelMapper.map(address, CreateAddressRequestDto.class))))
            .findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderWithAddressesResponseDto findAllAddressesForCurrentOrder(String uuid) {
        Long id = userRepository.findByUuid(uuid).getId();
        List<AddressDto> addressDtoList = addressRepo.findAllNonDeletedAddressesByUserId(id)
            .stream()
            .sorted(Comparator.comparing(Address::getId))
            .map(u -> modelMapper.map(u, AddressDto.class))
            .toList();
        return new OrderWithAddressesResponseDto(addressDtoList);
    }

    private void mapUpdatedOrderAddressFields(OrderAddress orderAddress, OrderAddress updatedOrderAddress,
        String comment) {
        updatedOrderAddress.setLocation(orderAddress.getLocation());
        updatedOrderAddress.setId(orderAddress.getId());
        updatedOrderAddress.getAddress().setActual(orderAddress.getAddress().getActual());
        updatedOrderAddress.getAddress().setAddressComment(comment);
        updatedOrderAddress.setCoordinates(orderAddress.getCoordinates());
        updatedOrderAddress.getAddress().setAddressStatus(orderAddress.getAddress().getAddressStatus());
    }
}
