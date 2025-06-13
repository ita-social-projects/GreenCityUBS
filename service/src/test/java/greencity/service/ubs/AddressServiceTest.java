package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
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
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Order;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.BaseAddress;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.Collections;

import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import static greencity.ModelUtils.*;
import static greencity.constant.ErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddressServiceTest {
    private static final String USER_UUID = "uuid";
    @Mock
    private OrderAddressRepository orderAddressRepository;
    @Mock
    private ModelMapper mapper;
    @Mock
    private AddressRequestDtoToBaseEntityMapper addressMapper;
    @Mock
    private LocationApiService locationApiService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EventService eventService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private AddressRequestDtoToBaseEntityMapper baseEntityMapper;
    @Mock
    private AddressRepository addressRepository;
    @InjectMocks
    private AddressServiceImpl addressService;

    @BeforeEach
    void setUp() {
        Mockito.reset(cityRepository);
    }

    @Test
    void getAddressForExistingOrderTest() {
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
    void getNotExistingAddressNonExistingForOrderTest() {
        Long orderId = -1L;

        when(orderAddressRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> addressService.getAddressForOrder(orderId));

        verify(orderAddressRepository).findByOrderId(orderId);
    }

    @Test
    void getAllDistrictsTest() {
        List<LocationDto> locationDtos;
        List<DistrictDto> districtDtos;
        locationDtos = Arrays.asList(LocationDto.builder().id("1").build(), LocationDto.builder().id("2").build());
        districtDtos = Arrays.asList(DistrictDto.builder().nameEn("District 1").build(),
            DistrictDto.builder().nameEn("District 2").build());
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString())).thenReturn(locationDtos);
        when(mapper.map(any(LocationDto.class), eq(DistrictDto.class))).thenAnswer(i -> new DistrictDto());
        List<DistrictDto> results = addressService.getAllDistricts("region", "city");
        verify(locationApiService, times(1)).getAllDistrictsInCityByNames(anyString(), anyString());
        assertEquals(districtDtos.size(), results.size());
        verify(locationApiService, times(1)).getAllDistrictsInCityByNames(anyString(), anyString());
        verify(mapper, times(locationDtos.size())).map(any(LocationDto.class), eq(DistrictDto.class));
    }

    @Test
    void getAddressByIdThrowsNotFoundExceptionForNonExistingOrderTest() {
        assertThrows(NotFoundException.class,
            () -> addressService.getAddressByOrderId(10000000L));
    }

    @Test
    void getAddressByExistingOrderId() {
        Order order = getOrder();
        ReadAddressByOrderDto readAddressByOrderDto = ModelUtils.getReadAddressByOrderDto();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderAddressRepository.findByOrderId(anyLong())).thenReturn(Optional.of(getOrderAddress()));
        when(mapper.map(any(OrderAddress.class), eq(ReadAddressByOrderDto.class))).thenReturn(readAddressByOrderDto);
        ReadAddressByOrderDto result = addressService.getAddressByOrderId(order.getId());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderAddressRepository, times(1)).findByOrderId(anyLong());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(readAddressByOrderDto, result);
    }

    @Test
    void updateAddressThrowsNotFoundExceptionIfOrderNotFoundTest() {
        UpdateAddressDto updateAddressDto = getUpdateAddressDto();
        String email = "test@email.com";
        when(orderRepository.findById(updateAddressDto.getOrderId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> addressService.addressUpdate(updateAddressDto, email));
        verify(orderRepository).findById(updateAddressDto.getOrderId());
    }

    @Test
    void updateOrderAddressForValidOrderTest() {
        OrderAddress expected = ModelUtils.getOrderAddress();
        OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate =
            ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        Address address = ModelUtils.getAddress();
        Region region = ModelUtils.getRegion();
        District district = ModelUtils.getDistrict();
        City city = ModelUtils.getCity();
        when(mapper.map(orderAddressExportDetailsDtoUpdate, CreateAddressRequestDto.class))
            .thenReturn(createAddressRequestDto);
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(mapper.map(address, OrderAddress.class)).thenReturn(expected);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(city));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(district));
        when(baseEntityMapper.convert(createAddressRequestDto, District.class)).thenReturn(district);
        when(baseEntityMapper.convert(any(CreateAddressRequestDto.class), eq(City.class))).thenReturn(city);
        addressService.updateOrderAddress(orderAddressExportDetailsDtoUpdate);
        verify(regionRepository, times(1)).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(districtRepository, times(1)).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(cityRepository, times(1)).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
    }

    @Test
    void updateOrderAddressIfNoRegionFoundTest() {
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        Address address = ModelUtils.getAddress();
        OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate =
            ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        when(mapper.map(orderAddressExportDetailsDtoUpdate, CreateAddressRequestDto.class))
            .thenReturn(createAddressRequestDto);
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
            () -> addressService.updateOrderAddress(orderAddressExportDetailsDtoUpdate));
    }

    @Test
    void saveCurrentAddressForOrderTest() {
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        String uuid = "a-b-c";
        User user = ModelUtils.getUser();
        Address address = ModelUtils.getAddress();
        List<Address> addresses = List.of(address);
        Region region = ModelUtils.getRegion();
        City city = ModelUtils.getCity();
        District district = ModelUtils.getDistrict();
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(createAddressRequestDto, Address.class)).thenReturn(addresses.getFirst());
        when(regionRepository.findRegionByNameEnOrNameUk(address.getBaseAddress().getRegionEn(),
            address.getBaseAddress().getRegionUk())).thenReturn(Optional.of(region));
        when(cityRepository
            .findCityByRegionIdAndNameUkAndNameEn(region.getId(),
                address.getBaseAddress().getCityUk(),
                address.getBaseAddress().getCityEn()))
            .thenReturn(Optional.of(city));
        when(districtRepository
            .findDistrictByCityIdAndNameEnOrNameUk(city.getId(), address.getBaseAddress().getDistrictEn(),
                address.getBaseAddress().getDistrictUk()))
            .thenReturn(Optional.of(district));
        addressService.saveCurrentAddressForOrder(createAddressRequestDto, uuid);
        verify(userRepository, times(2)).findUserByUuid(anyString());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(addressRepository, times(1)).findAllByUserId(user.getId());
        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void saveCurrentAddressForOrderIfAddressExistsTest() throws Exception {
        User user = ModelUtils.getUser();
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        List<Address> addresses = ModelUtils.addressList();
        Region region = ModelUtils.getRegion();
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(any(Address.class), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(mapper.map(createAddressRequestDto, CreateAddressRequestDto.class))
            .thenReturn(createAddressRequestDto);
        Method method = AddressServiceImpl.class.getDeclaredMethod("checkIfAddressExist", Long.class,
            CreateAddressRequestDto.class);
        method.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
            () -> method.invoke(addressService, 1L, createAddressRequestDto));
        assertInstanceOf(BadRequestException.class, exception.getCause());
    }

    @Test
    void updateAddressThrowsNotFoundOrderAddressExceptionTest() {
        Order order = getOrder();
        assertThrows(NotFoundException.class,
            () -> addressService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, order, "abc"));
    }

    @Test
    void makeAddressActualWhenAddressIdDeletedTest() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(user);
        firstAddress.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        user.setAddresses(List.of(firstAddress));

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndBaseAddress_ActualTrue(anyLong());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void makeAddressActualWhenAddressNotBelongsToUserTest() {
        Long firstAddressId = 1L;
        Long userId = 2L;
        User user = getUser();
        user.setId(userId);
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        User addressOwner = getUser();
        addressOwner.setUuid("randomUuid");
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(addressOwner);

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(CANNOT_ACCESS_PERSONAL_INFO, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndBaseAddress_ActualTrue(anyLong());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void updateCurrentAddressForOrderWithNoExistingAddressTest() throws Exception {
        Method setLocations = AddressServiceImpl.class.getDeclaredMethod("setLocations",
            CreateAddressRequestDto.class, Address.class);
        setLocations.setAccessible(true);

        Address address = getAddress();
        CreateAddressRequestDto dto = getAddressRequestToSaveDto();
        City city = ModelUtils.getCity();
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString()))
            .thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(addressMapper.convert(any(), eq(City.class))).thenReturn(getCity());
        when(cityRepository.save(any())).thenReturn(getCity());
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(addressMapper.convert(any(), eq(District.class))).thenReturn(getDistrict());
        when(baseEntityMapper.convert(dto, City.class)).thenReturn(city);
        setLocations.invoke(addressService, dto, address);

        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(cityRepository).save(any());
        verify(districtRepository).save(any());
    }

    @Test
    void updateCurrentAddressForOrderWithInvalidUserTest() {
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressDtoRequest2();
        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> addressService.updateCurrentAddressForOrder(dtoRequest, USER_UUID));

        verify(userRepository).findUserByUuid(anyString());
    }

    @Test
    void updateCurrentAddressForOrderWithExistingDeletedAddressTest() {
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressDtoRequest2();
        User user = getUser();
        Address address = getAddress(1L);
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        address.setUser(user);

        CreateAddressRequestDto dto = getAddressRequestDto2();

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.findAllByUserId(anyLong())).thenReturn(List.of(address));
        when(mapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(dto);

        addressService.updateCurrentAddressForOrder(dtoRequest, USER_UUID);

        verify(userRepository, times(2)).findUserByUuid(anyString());
        verify(addressRepository).findById(anyLong());
        verify(addressRepository).findAllByUserId(anyLong());
        verify(mapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(addressRepository, times(2)).save(any());
    }

    @Test
    void updateCurrentAddressForOrderWithNoExistingRegionTest() {
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressDtoRequest2();
        User user = getUser();
        Address address = getAddress(1L)
            .setUser(user);
        address.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        CreateAddressRequestDto dto = getAddressRequestDto();

        when(userRepository.findUserByUuid(anyString())).thenReturn(Optional.of(user));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.findAllByUserId(anyLong())).thenReturn(List.of(address));
        when(mapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(dto);
        when(mapper.map(any(), eq(Address.class))).thenReturn(getAddress());
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString()))
            .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
            () -> addressService.updateCurrentAddressForOrder(dtoRequest, USER_UUID));

        verify(userRepository).findUserByUuid(anyString());
        verify(addressRepository).findById(anyLong());
        verify(addressRepository).findAllByUserId(anyLong());
        verify(mapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
    }

    @Test
    void findAllAddressesForCurrentOrderTest() {
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(user));

        List<AddressDto> testAddressesDto = getDataForAddressesDtoTest();

        OrderWithAddressesResponseDto expected = new OrderWithAddressesResponseDto(testAddressesDto);

        List<Address> addresses = getTestAddresses(user);

        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(addresses.get(0), AddressDto.class)).thenReturn(testAddressesDto.get(0));
        when(mapper.map(addresses.get(1), AddressDto.class)).thenReturn(testAddressesDto.get(1));

        OrderWithAddressesResponseDto actual = addressService.findAllAddressesForCurrentOrder(uuid);

        assertEquals(actual, expected);
        verify(userRepository, times(1)).findUserByUuid(uuid);
        verify(addressRepository, times(1)).findAllNonDeletedAddressesByUserId(user.getId());
    }

    private List<AddressDto> getDataForAddressesDtoTest() {
        AddressDto addressDto1 = AddressDto.builder().actual(true).id(13L).cityUk("Kyiv").districtUk("Svyatoshyn")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").streetUk("Peremohy av.")
            .coordinates(new Coordinates(12.5, 34.5)).build();

        AddressDto addressDto2 = AddressDto.builder().actual(true).id(42L).cityUk("Lviv").districtUk("Syhiv")
            .entranceNumber("1").houseCorpus("1").houseNumber("55").streetUk("Lvivska st.")
            .coordinates(new Coordinates(13.5, 36.5)).build();
        return Arrays.asList(addressDto1, addressDto2);
    }

    private List<Address> getTestAddresses(User user) {
        Address address1 = Address.builder()
            .id(13L)
            .baseAddress(BaseAddress.builder()
                .actual(true)
                .addressStatus(AddressStatus.NEW).districtUk("Svyatoshyn")
                .cityUk("Kyiv")
                .entranceNumber("1").houseCorpus("1").houseNumber("55").streetUk("Peremohy av.").build())
            .user(user).coordinates(new Coordinates(12.5, 34.5))
            .build();

        Address address2 = Address.builder()
            .id(42L)
            .baseAddress(BaseAddress.builder()
                .addressStatus(AddressStatus.NEW).cityUk("Lviv").districtUk("Syhiv")
                .actual(true)
                .entranceNumber("1").houseCorpus("1").houseNumber("55").streetUk("Lvivska st.").build())
            .user(user).coordinates(new Coordinates(13.5, 36.5))
            .build();

        return Arrays.asList(address1, address2);
    }

    @Test
    void saveCurrentAddressForValidOrderTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressRequestToSaveDto();
        Address addressToSave = getAddress();

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        when(mapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));

        var addressDto = addressDto();
        addressDto.setDistrictUk("Район");
        addressDto.setDistrictEn("District");
        when(mapper.map(addresses.getFirst(), AddressDto.class)).thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
            addressService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(createAddressRequestToSaveDto.getDistrictUk(),
            actualWithSearchAddress.getAddressList().getFirst().getDistrictUk());
        assertEquals(createAddressRequestToSaveDto.getDistrictEn(),
            actualWithSearchAddress.getAddressList().getFirst().getDistrictEn());

        verify(addressRepository).save(addressToSave);

        verify(userRepository, times(2)).findUserByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());

        verify(mapper, times(1)).map(any(), eq(Address.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(mapper).map(addresses.getFirst(), AddressDto.class);
    }

    @Test
    void deleteCurrentAddressForValidOrderWhenItIsLastAddressTest() {
        AddressService serviceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.getBaseAddress().setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId())).thenReturn(Optional.empty());
        doReturn(new OrderWithAddressesResponseDto()).when(serviceSpy).findAllAddressesForCurrentOrder(uuid);
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        addressService.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getBaseAddress().getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getBaseAddress().getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
    }

    @Test
    void saveCurrentAddressForMaximumNumbersOfOrdersAddressesExceptionTest() {
        User user = getUserForCreate();
        List<Address> addresses = getMaximumAmountOfAddresses();
        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addressService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED, exception.getMessage());
    }

    @Test
    void saveCurrentAddressForOrderAlreadyExistExceptionTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();

        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.NEW);
        addresses.getFirst().getBaseAddress().setActual(false);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(any(),
            eq(CreateAddressRequestDto.class)))
            .thenReturn(dtoRequest);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);

        dtoRequest.setPlaceId(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addressService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());

        verify(userRepository).findUserByUuid(user.getUuid());
        verify(addressRepository).findAllNonDeletedAddressesByUserId(user.getId());
        verify(mapper, times(2)).map(any(), eq(CreateAddressRequestDto.class));
    }

    @Test
    void updateCurrentAddressForOrderTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.getFirst()));
        when(mapper.map(any(),
            eq(Address.class))).thenReturn(addresses.getFirst());
        when(addressRepository.save(addresses.getFirst())).thenReturn(addresses.getFirst());
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));

        var addressDto = addressDto();
        addressDto.setDistrictUk("Район");
        addressDto.setDistrictEn("District");

        when(mapper.map(addresses.getFirst(),
            AddressDto.class))
            .thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
            addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(updateAddressRequestDto.getDistrictUk(),
            actualWithSearchAddress.getAddressList().getFirst().getDistrictUk());
        assertEquals(updateAddressRequestDto.getDistrictEn(),
            actualWithSearchAddress.getAddressList().getFirst().getDistrictEn());

        OrderWithAddressesResponseDto actualWithoutSearchAddress =
            addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithoutSearchAddress);

        verify(addressRepository, times(2)).save(addresses.getFirst());
        verify(regionRepository, times(2)).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository, times(2)).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository, times(2)).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
    }

    @Test
    void makeAddressActualWhenAddressNotFoundTest() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + firstAddressId, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndBaseAddress_ActualTrue(anyLong());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void makeAddressActualWhereUserNotHaveActualAddressTest() {
        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndBaseAddress_ActualTrue(user.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(ACTUAL_ADDRESS_NOT_FOUND, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndBaseAddress_ActualTrue(user.getId());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void updateCurrentAddressForOrderWithNoAddressTest() {
        User user = getUserForCreate(AddressStatus.DELETED);
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(7L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().setUser(user);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.getFirst()));
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.getFirst()));
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));
        when(mapper.map(any(),
            eq(Address.class))).thenReturn(addresses.getFirst());

        when(addressRepository.save(addresses.getFirst())).thenReturn(addresses.getFirst());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        assertEquals(OrderWithAddressesResponseDto.builder().addressList(Collections.emptyList()).build(),
            actualWithSearchAddress);

        verify(addressRepository, times(1)).save(addresses.getFirst());
        verify(addressRepository, times(1)).findById(anyLong());
        verify(mapper, times(1)).map(any(), eq(Address.class));
    }

    @Test
    void updateCurrentAddressForOrderAlreadyExistExceptionTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        dtoRequest.setPlaceId(null);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findById(updateAddressRequestDto.getId()))
            .thenReturn(Optional.ofNullable(addresses.getFirst()));
        when(mapper.map(any(),
            eq(CreateAddressRequestDto.class)))
            .thenReturn(dtoRequest);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());
    }

    @Test
    void updateCurrentAddressForOrderThrowsAccessDeniedExceptionTest() {
        long addressId = 1L;
        long userId = 2L;

        User user = getUserForCreate();
        user.setId(userId);

        Address address = new Address();
        address.setId(addressId);
        address.setUser(new User());
        address.getUser().setId(userId + 1);

        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(addressId);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> addressService.updateCurrentAddressForOrder(dtoRequest, uuid));

        assertEquals(CANNOT_ACCESS_PERSONAL_INFO, exception.getMessage());
    }

    @Test
    void deleteCurrentAddressForOrderWhenAddressIsActualTest() {
        AddressServiceImpl addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.getBaseAddress().setActual(true);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId()))
            .thenReturn(Optional.of(secondAddress));
        doReturn(new OrderWithAddressesResponseDto()).when(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        addressServiceSpy.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getBaseAddress().getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getBaseAddress().getAddressStatus());
        Assertions.assertTrue(secondAddress.getBaseAddress().getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
        verify(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void deleteCurrentAddressForOrderWhenAddressIsNotActualTest() {
        AddressServiceImpl addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        doReturn(new OrderWithAddressesResponseDto()).when(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);

        addressServiceSpy.deleteCurrentAddressForOrder(firstAddressId, uuid);

        assertEquals(AddressStatus.DELETED, firstAddress.getBaseAddress().getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void updateCurrentAddressForOrderNotFoundOrderAddressExceptionTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findById(updateAddressRequestDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + updateAddressRequestDto.getId(), exception.getMessage());
    }

    @Test
    void deleteCurrentAddressForOrderWithNonExistingAddressTest() {
        AddressService addressServiceSpy = spy(addressService);

        Long addressId = 1L;

        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> addressServiceSpy.deleteCurrentAddressForOrder(addressId, "qwe"));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + addressId, exception.getMessage());

        verify(addressRepository).findById(addressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(addressServiceSpy, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void deleteCurrentAddressForOrderForWrongUserTest() {
        AddressService addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = "qwe";

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> addressServiceSpy.deleteCurrentAddressForOrder(firstAddressId, uuid));

        assertEquals(CANNOT_DELETE_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(addressServiceSpy, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void deleteCurrentAddressForOrderWhenAddressAlreadyDeletedTest() {
        AddressService addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.getBaseAddress().setAddressStatus(AddressStatus.DELETED);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> addressServiceSpy.deleteCurrentAddressForOrder(firstAddressId, uuid));

        assertEquals(CANNOT_DELETE_ALREADY_DELETED_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(addressServiceSpy, times(0)).findAllAddressesForCurrentOrder(anyString());
    }

    @Test
    void makeAddressActualTest() {
        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        secondAddress.getBaseAddress().setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndBaseAddress_ActualTrue(user.getId()))
            .thenReturn(Optional.of(secondAddress));

        addressService.makeAddressActual(firstAddressId, uuid);

        Assertions.assertTrue(firstAddress.getBaseAddress().getActual());
        assertFalse(secondAddress.getBaseAddress().getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndBaseAddress_ActualTrue(user.getId());
        verify(mapper).map(firstAddress, AddressDto.class);
    }

    @Test
    void getAllDistrictsForKyivTest() {
        List<District> district = List.of(ModelUtils.getDistrict());
        when(districtRepository.findAllByCityId(1L)).thenReturn(district);
        when(cityRepository.findIdByCityNameEnIgnoreCase(AppConstant.KYIV)).thenReturn(Optional.of(1L));

        addressService.getAllDistrictsForKyiv();

        verify(cityRepository, times(1)).findIdByCityNameEnIgnoreCase(AppConstant.KYIV);
        verify(districtRepository, times(1)).findAllByCityId(anyLong());
    }

    @Test
    void getAllDistrictsForKyivAndCityKyivNotFoundThenExceptionThrownTest() {
        when(cityRepository.findIdByCityNameEnIgnoreCase(AppConstant.KYIV)).thenThrow(new NotFoundException(CITY_NOT_FOUND));

        assertThrows(NotFoundException.class, () -> addressService.getAllDistrictsForKyiv());

        verify(cityRepository, times(1)).findIdByCityNameEnIgnoreCase(AppConstant.KYIV);
        verify(districtRepository, times(0)).findAllByCityId(anyLong());
    }

    @Test
    void mapUpdatedOrderAddressFieldsTest() throws Exception {
        Method mapUpdatedOrderAddressFieldsMethod =
            AddressServiceImpl.class.getDeclaredMethod("mapUpdatedOrderAddressFields",
                OrderAddress.class, OrderAddress.class, String.class);
        mapUpdatedOrderAddressFieldsMethod.setAccessible(true);
        Location location = ModelUtils.getLocation();
        String comment = "New comment test";
        OrderAddress actual = OrderAddress.builder().location(location)
            .id(1L)
            .baseAddress(BaseAddress.builder()
                .actual(false)
                .addressComment("No comments")
                .addressStatus(AddressStatus.DELETED)
                .build())
            .coordinates(location.getCoordinates())
            .build();
        OrderAddress expected = ModelUtils.getOrderAddress();
        mapUpdatedOrderAddressFieldsMethod.invoke(addressService, expected, actual, comment);
        assertEquals(expected.getLocation(), actual.getLocation());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBaseAddress().getActual(), actual.getBaseAddress().getActual());
        assertEquals(comment, actual.getBaseAddress().getAddressComment());
        assertEquals(expected.getCoordinates(), actual.getCoordinates());
        assertEquals(expected.getBaseAddress().getAddressStatus(), actual.getBaseAddress().getAddressStatus());
    }

    @Test
    void addressUpdateIfPresentTest() {
        Order order = ModelUtils.getOrder();
        UpdateAddressDto addressDto = ModelUtils.getUpdateAddressDto();
        addressDto.setOrderId(order.getId());
        OrderAddress orderAddress = ModelUtils.getOrderAddress();
        Region region = ModelUtils.getRegion();
        Address address = ModelUtils.getAddress();
        City city = ModelUtils.getCity();
        District district = ModelUtils.getDistrict();
        OrderAddressDtoResponse orderAddressDtoResponse = new OrderAddressDtoResponse();
        when(orderAddressRepository.findById(anyLong())).thenReturn(Optional.of(orderAddress));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(city));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(district));
        when(mapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(orderAddress);
        when(mapper.map(any(OrderAddress.class), eq(OrderAddressDtoResponse.class)))
            .thenReturn(orderAddressDtoResponse);
        addressService.addressUpdate(addressDto, "email@gmail.com");
        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderAddressRepository, times(1)).save(any());
    }

    @Test
    void saveCurrentAddressForOrderForAddressesBelongToKyivEnTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressWithKyivRegionToSaveRequestDto();
        Address addressToSave = getAddress();

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));

        when(mapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(mapper.map(addresses.getFirst(), AddressDto.class)).thenReturn(addressWithKyivRegionDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            addressService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(KYIV_REGION_EN, actualWithSearchAddress.getAddressList().getFirst().getRegionEn());

        verify(userRepository, times(2)).findUserByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());

        verify(mapper, times(1)).map(any(), eq(Address.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(mapper).map(addresses.getFirst(), AddressDto.class);
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(addressRepository).save(addressToSave);
    }

    @Test
    void saveCurrentAddressForOrderForAddressesBelongToKyivUaTest() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.getFirst().getBaseAddress().setActual(false);
        addresses.getFirst().getBaseAddress().setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressWithKyivRegionToSaveRequestDto();
        Address addressToSave = getAddress();

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));
        when(mapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(mapper.map(addresses.getFirst(), AddressDto.class)).thenReturn(addressWithKyivRegionDto());

        OrderWithAddressesResponseDto actualWithSearchAddress =
            addressService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(KYIV_REGION_UK, actualWithSearchAddress.getAddressList().getFirst().getRegionUk());

        verify(userRepository, times(2)).findUserByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());

        verify(mapper, times(1)).map(any(), eq(Address.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(mapper).map(addresses.getFirst(), AddressDto.class);
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(addressRepository).save(addressToSave);
    }

    @Test
    void updateOrderAddressTest() {
        Address addressToSave = getAddress();

        when(mapper.map(TEST_ORDER_ADDRESS_DTO_UPDATE, CreateAddressRequestDto.class))
            .thenReturn(TEST_CREATE_ADDRESS_DTO);
        when(mapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
            .thenReturn(Optional.of(getDistrict()));

        addressService.updateOrderAddress(TEST_ORDER_ADDRESS_DTO_UPDATE);

        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
    }

    @Test
    void saveCurrentAddressForOrderWithNoUserFoundTest() {
        String uuid = "a-b-c";
        CreateAddressRequestDto mock = mock(CreateAddressRequestDto.class);
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> addressService.saveCurrentAddressForOrder(mock, uuid));
        verify(userRepository, times(1)).findUserByUuid(uuid);
    }
}