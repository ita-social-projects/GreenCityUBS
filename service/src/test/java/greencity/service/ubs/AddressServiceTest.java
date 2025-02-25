package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.order.*;
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
import greencity.repository.*;

import java.lang.reflect.Method;
import java.util.*;

import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @Test
    void testGetAllDistricts() {
        List<LocationDto> locationDtos;
        List<DistrictDto> districtDtos;
        locationDtos = Arrays.asList(LocationDto.builder().id("1").build(), LocationDto.builder().id("2").build());
        districtDtos = Arrays.asList(DistrictDto.builder().nameEn("District 1").build(), DistrictDto.builder().nameEn("District 2").build());
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString())).thenReturn(locationDtos);
        when(mapper.map(any(LocationDto.class), eq(DistrictDto.class))).thenAnswer(i -> new DistrictDto());
        List<DistrictDto> results = addressService.getAllDistricts("region", "city");
        verify(locationApiService, times(1)).getAllDistrictsInCityByNames(anyString(), anyString());
        assertEquals(districtDtos.size(), results.size());
        verify(locationApiService, times(1)).getAllDistrictsInCityByNames(anyString(), anyString());
        verify(mapper, times(locationDtos.size())).map(any(LocationDto.class), eq(DistrictDto.class));
    }

    @Test
    void checkOrderNotFound() {
        assertThrows(NotFoundException.class,
                () -> addressService.getAddressByOrderId(10000000L));
    }

    @Test
    void getAddressByOrderId() {
        Order order = getOrder();
        ReadAddressByOrderDto readAddressByOrderDto = ModelUtils.getReadAddressByOrderDto();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderAddressRepository.findByOrderId(anyLong())).thenReturn(Optional.of(getOrderAddress()));
        when(addressService.getAddressByOrderId(anyLong())).thenReturn(readAddressByOrderDto);
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderAddressRepository, times(1)).findByOrderId(anyLong());
        addressService.getAddressByOrderId(order.getId());
        Assertions.assertNotNull(order);
    }

    @Test
    void updateAddressTestIfOrderNotFoundTest() {
        UpdateAddressDto updateAddressDto = getUpdateAddressDto();
        String email = "test@email.com";
        when(orderRepository.findById(updateAddressDto.getOrderId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> addressService.addressUpdate(updateAddressDto, email));
        verify(orderRepository).findById(updateAddressDto.getOrderId());
    }

    @Test
    void updateOrderAddressTest() {
        OrderAddress expected = ModelUtils.getOrderAddress();
        OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate
                = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        Address address = ModelUtils.getAddress();
        Region region = ModelUtils.getRegion();
        District district = ModelUtils.getDistrict();
        City city = ModelUtils.getCity();
        when(mapper.map(orderAddressExportDetailsDtoUpdate, CreateAddressRequestDto.class)).thenReturn(createAddressRequestDto);
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(mapper.map(eq(address), eq(OrderAddress.class))).thenReturn(expected);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString())).thenReturn(Optional.of(city));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString())).thenReturn(Optional.of(district));
        when(baseEntityMapper.convert(eq(createAddressRequestDto), eq(District.class))).thenReturn(district);
        when(baseEntityMapper.convert(eq(createAddressRequestDto), eq(City.class))).thenReturn(city);
        addressService.updateOrderAddress(orderAddressExportDetailsDtoUpdate);
        verify(regionRepository, times(1)).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(districtRepository, times(1)).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(cityRepository, times(1)).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
    }

    @Test
    void updateOrderAddressIfNoRegionFoundTest() {
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        Address address = ModelUtils.getAddress();
        OrderAddressExportDetailsDtoUpdate orderAddressExportDetailsDtoUpdate
                = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        when(mapper.map(orderAddressExportDetailsDtoUpdate, CreateAddressRequestDto.class)).thenReturn(createAddressRequestDto);
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> addressService.updateOrderAddress(orderAddressExportDetailsDtoUpdate));
    }

    @Test
    void saveCurrentAddressForOrderTest() {
        User user = ModelUtils.getUser();
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        CreateAddressRequestDto createAddressRequestDto1 = ModelUtils.getAddressRequestDto();
        createAddressRequestDto1.setPlaceId("2");
        createAddressRequestDto1.setCity("Dnipro");
        List<Address> addresses = ModelUtils.addressList();
        Region region = ModelUtils.getRegion();
        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(eq(user.getId()))).thenReturn(addresses);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(addressRepository.findAllByUserId(eq(user.getId()))).thenReturn(addresses);
        when(mapper.map(any(Address.class), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(mapper.map(eq(createAddressRequestDto), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto1);
        addressService.saveCurrentAddressForOrder(createAddressRequestDto, user.getUuid());
        verify(userRepository, times(2)).findByUuid(anyString());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(eq(user.getId()));
        verify(addressRepository, times(1)).findAllByUserId(eq(user.getId()));
    }

    @Test
    void saveCurrentAddressForOrderIfAddressExistsTest() {
        User user = ModelUtils.getUser();
        CreateAddressRequestDto createAddressRequestDto = ModelUtils.getAddressRequestDto();
        List<Address> addresses = ModelUtils.addressList();
        Region region = ModelUtils.getRegion();
        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(eq(user.getId()))).thenReturn(addresses);
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString())).thenReturn(Optional.of(region));
        when(addressRepository.findAllByUserId(eq(user.getId()))).thenReturn(addresses);
        when(mapper.map(any(Address.class), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        when(mapper.map(eq(createAddressRequestDto), eq(CreateAddressRequestDto.class))).thenReturn(createAddressRequestDto);
        assertThrows(BadRequestException.class, () -> addressService.saveCurrentAddressForOrder(createAddressRequestDto, user.getUuid()));
    }

    @Test
    void testUpdateAddressThrowsNotFoundOrderAddressException() {
        Order order = getOrder();
        assertThrows(NotFoundException.class,
                () -> addressService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, order, "abc"));
    }

    @Test
    void testMakeAddressActualWhenAddressIdDeleted() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setUser(user);
        firstAddress.setAddressStatus(AddressStatus.DELETED);
        user.setAddresses(List.of(firstAddress));

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(CANNOT_MAKE_ACTUAL_DELETED_ADDRESS, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void testMakeAddressActualWhenAddressNotBelongsToUser() {
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
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
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
        when(baseEntityMapper.convert(eq(dto), eq(City.class))).thenReturn(city);
        setLocations.invoke(addressService, dto, address);

        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());
        verify(cityRepository).save(any());
        verify(districtRepository).save(any());
    }

    @Test
    void testAreAddressesEqual() throws Exception {
        Method areAddressesEqualMethod = AddressServiceImpl.class.getDeclaredMethod("areAddressesEqual",
                CreateAddressRequestDto.class, CreateAddressRequestDto.class);
        areAddressesEqualMethod.setAccessible(true);

        CreateAddressRequestDto address1 = getAddressRequestDtoReflection();
        CreateAddressRequestDto address2 = getAddressRequestDtoReflection2();
        CreateAddressRequestDto address3 = getAddressRequestDtoReflection3();
        CreateAddressRequestDto address4 = getAddressRequestDtoReflection4();
        CreateAddressRequestDto address5 = getAddressRequestDtoReflection5();

        boolean result1 = (boolean) areAddressesEqualMethod.invoke(addressService, address1, address2);
        assertTrue(result1);

        boolean result2 = (boolean) areAddressesEqualMethod.invoke(addressService, address1, address3);
        assertFalse(result2);

        boolean result3 = (boolean) areAddressesEqualMethod.invoke(addressService, address1, null);
        assertFalse(result3);

        boolean result4 = (boolean) areAddressesEqualMethod.invoke(addressService, null, null);
        assertFalse(result4);

        boolean result5 = (boolean) areAddressesEqualMethod.invoke(addressService, address1, address4);
        assertFalse(result5);

        boolean result6 = (boolean) areAddressesEqualMethod.invoke(addressService, address1, address5);
        assertTrue(result6);
    }

    @Test
    void updateCurrentAddressForOrderWithInvalidUserTest() {
        when(userRepository.findByUuid(anyString())).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> addressService.updateCurrentAddressForOrder(null, USER_UUID));

        verify(userRepository).findByUuid(anyString());
    }

    @Test
    void updateCurrentAddressForOrderWithExistingDeletedAddressTest() {
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressDtoRequest2();
        User user = getUser();
        Address address = getAddress(1L)
                .setUser(user)
                .setAddressStatus(AddressStatus.DELETED);
        CreateAddressRequestDto dto = getAddressRequestDto2();

        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.findAllByUserId(anyLong())).thenReturn(List.of(address));
        when(mapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(dto);

        addressService.updateCurrentAddressForOrder(dtoRequest, USER_UUID);

        verify(userRepository, times(2)).findByUuid(anyString());
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
                .setUser(user)
                .setAddressStatus(AddressStatus.DELETED);
        CreateAddressRequestDto dto = getAddressRequestDto();

        when(userRepository.findByUuid(anyString())).thenReturn(user);
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(address));
        when(addressRepository.findAllByUserId(anyLong())).thenReturn(List.of(address));
        when(mapper.map(any(), eq(CreateAddressRequestDto.class))).thenReturn(dto);
        when(mapper.map(any(), eq(Address.class))).thenReturn(getAddress());
        when(regionRepository.findRegionByNameEnOrNameUk(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> addressService.updateCurrentAddressForOrder(dtoRequest, USER_UUID));

        verify(userRepository).findByUuid(anyString());
        verify(addressRepository).findById(anyLong());
        verify(addressRepository).findAllByUserId(anyLong());
        verify(mapper).map(any(), eq(CreateAddressRequestDto.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
    }

    @Test
    void testFindAllAddressesForCurrentOrder() {
        String uuid = "35467585763t4sfgchjfuyetf";
        User user = new User();
        user.setId(13L);
        when(userRepository.findByUuid(uuid)).thenReturn(user);

        List<AddressDto> testAddressesDto = getTestAddressesDto();

        OrderWithAddressesResponseDto expected = new OrderWithAddressesResponseDto(testAddressesDto);

        List<Address> addresses = getTestAddresses(user);

        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(addresses.get(0), AddressDto.class)).thenReturn(testAddressesDto.get(0));
        when(mapper.map(addresses.get(1), AddressDto.class)).thenReturn(testAddressesDto.get(1));

        OrderWithAddressesResponseDto actual = addressService.findAllAddressesForCurrentOrder(uuid);

        assertEquals(actual, expected);
        verify(userRepository, times(1)).findByUuid(uuid);
        verify(addressRepository, times(1)).findAllNonDeletedAddressesByUserId(user.getId());
    }

    private List<AddressDto> getTestAddressesDto() {
        AddressDto addressDto1 = AddressDto.builder().actual(true).id(13L).city("Kyiv").district("Svyatoshyn")
                .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
                .coordinates(new Coordinates(12.5, 34.5)).build();

        AddressDto addressDto2 = AddressDto.builder().actual(true).id(42L).city("Lviv").district("Syhiv")
                .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
                .coordinates(new Coordinates(13.5, 36.5)).build();
        return Arrays.asList(addressDto1, addressDto2);
    }

    private List<Address> getTestAddresses(User user) {
        Address address1 = Address.builder()
                .addressStatus(AddressStatus.NEW).id(13L).city("Kyiv").district("Svyatoshyn")
                .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Peremohy av.")
                .user(user).actual(true).coordinates(new Coordinates(12.5, 34.5))
                .build();

        Address address2 = Address.builder()
                .addressStatus(AddressStatus.NEW).id(42L).city("Lviv").district("Syhiv")
                .entranceNumber("1").houseCorpus("1").houseNumber("55").street("Lvivska st.")
                .user(user).actual(true).coordinates(new Coordinates(13.5, 36.5))
                .build();

        return Arrays.asList(address1, address2);
    }

    @Test
    void testSaveCurrentAddressForOrder() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        addresses.getFirst().setActual(false);
        addresses.getFirst().setAddressStatus(AddressStatus.NEW);

        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestToSaveDto = getAddressRequestToSaveDto();
        Address addressToSave = getAddress();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        when(mapper.map(any(), eq(Address.class))).thenReturn(addressToSave);
        when(regionRepository.findRegionByNameEnOrNameUk(any(), any())).thenReturn(Optional.of(getRegion()));
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.of(getCity()));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.of(getDistrict()));

        var addressDto = addressDto();
        addressDto.setDistrict("Район");
        addressDto.setDistrictEn("District");
        when(mapper.map(addresses.getFirst(), AddressDto.class)).thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
                addressService.saveCurrentAddressForOrder(createAddressRequestToSaveDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(createAddressRequestToSaveDto.getDistrict(),
                actualWithSearchAddress.getAddressList().getFirst().getDistrict());
        assertEquals(createAddressRequestToSaveDto.getDistrictEn(),
                actualWithSearchAddress.getAddressList().getFirst().getDistrictEn());

        verify(addressRepository).save(addressToSave);

        verify(userRepository, times(2)).findByUuid(user.getUuid());
        verify(addressRepository, times(2)).findAllNonDeletedAddressesByUserId(user.getId());
        verify(regionRepository).findRegionByNameEnOrNameUk(anyString(), anyString());
        verify(cityRepository).findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString());
        verify(districtRepository).findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString());

        verify(mapper, times(1)).map(any(), eq(Address.class));
        verify(mapper).map(any(), eq(Address.class));
        verify(mapper).map(addresses.getFirst(), AddressDto.class);
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenItIsLastAddress() {
        AddressService serviceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId())).thenReturn(Optional.empty());
        doReturn(new OrderWithAddressesResponseDto()).when(serviceSpy).findAllAddressesForCurrentOrder(uuid);
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        addressService.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
    }

    @Test
    void testSaveCurrentAddressForMaximumNumbersOfOrdersAddressesException() {
        User user = getUserForCreate();
        List<Address> addresses = getMaximumAmountOfAddresses();
        String uuid = user.getUuid();
        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ErrorMessage.NUMBER_OF_ADDRESSES_EXCEEDED, exception.getMessage());
    }

    @Test
    void testSaveCurrentAddressForOrderAlreadyExistException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();

        CreateAddressRequestDto createAddressRequestDto = getAddressRequestDto();

        addresses.getFirst().setAddressStatus(AddressStatus.NEW);
        addresses.getFirst().setActual(false);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findAllNonDeletedAddressesByUserId(user.getId())).thenReturn(addresses);
        when(mapper.map(any(),
                eq(CreateAddressRequestDto.class)))
                .thenReturn(dtoRequest);
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(addresses);

        dtoRequest.setPlaceId(null);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.saveCurrentAddressForOrder(createAddressRequestDto, uuid));

        assertEquals(ADDRESS_ALREADY_EXISTS, exception.getMessage());

        verify(userRepository).findByUuid(user.getUuid());
        verify(addressRepository).findAllNonDeletedAddressesByUserId(user.getId());
        verify(mapper, times(2)).map(any(), eq(CreateAddressRequestDto.class));
    }

    @Test
    void testUpdateCurrentAddressForOrder() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().setActual(false);
        addresses.getFirst().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
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
        addressDto.setDistrict("Район");
        addressDto.setDistrictEn("District");

        when(mapper.map(addresses.getFirst(),
                AddressDto.class))
                .thenReturn(addressDto);

        OrderWithAddressesResponseDto actualWithSearchAddress =
                addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid);

        assertEquals(getAddressDtoResponse(), actualWithSearchAddress);
        assertEquals(updateAddressRequestDto.getDistrict(),
                actualWithSearchAddress.getAddressList().getFirst().getDistrict());
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
    void testMakeAddressActualWhenAddressNotFound() {
        Long firstAddressId = 1L;
        User user = getUser();
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + firstAddressId, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findByUserIdAndActualTrue(anyLong());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void testMakeAddressActualWhereUserNotHaveActualAddress() {
        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndActualTrue(user.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> addressService.makeAddressActual(firstAddressId, uuid));

        assertEquals(ACTUAL_ADDRESS_NOT_FOUND, exception.getMessage());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndActualTrue(user.getId());
        verify(mapper, times(0)).map(any(), any());
    }

    @Test
    void testUpdateCurrentAddressForOrderWithNoAddress() {
        User user = getUserForCreate(AddressStatus.DELETED);
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(7L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().setActual(false);
        addresses.getFirst().setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
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
    void testUpdateCurrentAddressForOrderAlreadyExistException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        dtoRequest.setPlaceId(null);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().setActual(false);
        addresses.getFirst().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
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
    void testUpdateCurrentAddressForOrderThrowsAccessDeniedException() {
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
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> addressService.updateCurrentAddressForOrder(dtoRequest, uuid));

        assertEquals(CANNOT_ACCESS_PERSONAL_INFO, exception.getMessage());
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenAddressIsActual() {
        AddressServiceImpl addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setActual(true);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findAnyByUserIdAndAddressStatusNotDeleted(user.getId()))
                .thenReturn(Optional.of(secondAddress));
        doReturn(new OrderWithAddressesResponseDto()).when(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        addressServiceSpy.deleteCurrentAddressForOrder(firstAddressId, uuid);

        Assertions.assertFalse(firstAddress.getActual());
        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());
        Assertions.assertTrue(secondAddress.getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findAnyByUserIdAndAddressStatusNotDeleted(user.getId());
        verify(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void testDeleteCurrentAddressForOrderWhenAddressIsNotActual() {
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

        assertEquals(AddressStatus.DELETED, firstAddress.getAddressStatus());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository, times(0)).findAnyByUserIdAndAddressStatusNotDeleted(anyLong());
        verify(addressServiceSpy).findAllAddressesForCurrentOrder(uuid);
    }

    @Test
    void testUpdateCurrentAddressForOrderNotFoundOrderAddressException() {
        User user = getUserForCreate();
        List<Address> addresses = user.getAddresses();
        String uuid = user.getUuid();
        OrderAddressDtoRequest dtoRequest = getTestOrderAddressLocationDto();
        dtoRequest.setId(1L);
        OrderAddressDtoRequest updateAddressRequestDto = getTestOrderAddressDtoRequest();
        updateAddressRequestDto.setId(1L);
        addresses.getFirst().setActual(false);
        addresses.getFirst().setAddressStatus(AddressStatus.IN_ORDER);
        addresses.getFirst().setUser(user);

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(addressRepository.findById(updateAddressRequestDto.getId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> addressService.updateCurrentAddressForOrder(updateAddressRequestDto, uuid));

        assertEquals(NOT_FOUND_ADDRESS_ID_FOR_CURRENT_USER + updateAddressRequestDto.getId(), exception.getMessage());
    }

    @Test
    void testDeleteCurrentAddressForOrderWithUnexistingAddress() {
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
    void testDeleteCurrentAddressForOrderForWrongUser() {
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
    void testDeleteCurrentAddressForOrderWhenAddressAlreadyDeleted() {
        AddressService addressServiceSpy = spy(addressService);

        Long firstAddressId = 1L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        firstAddress.setAddressStatus(AddressStatus.DELETED);
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
    void testMakeAddressActual() {
        Long firstAddressId = 1L;
        Long secondAddressId = 2L;
        Address firstAddress = getAddress();
        firstAddress.setId(firstAddressId);
        Address secondAddress = getAddress();
        secondAddress.setId(secondAddressId);
        secondAddress.setActual(true);
        User user = getUser();
        firstAddress.setUser(user);
        String uuid = user.getUuid();

        when(addressRepository.findById(firstAddressId)).thenReturn(Optional.of(firstAddress));
        when(addressRepository.findByUserIdAndActualTrue(user.getId())).thenReturn(Optional.of(secondAddress));

        addressService.makeAddressActual(firstAddressId, uuid);

        Assertions.assertTrue(firstAddress.getActual());
        assertFalse(secondAddress.getActual());

        verify(addressRepository).findById(firstAddressId);
        verify(addressRepository).findByUserIdAndActualTrue(user.getId());
        verify(mapper).map(firstAddress, AddressDto.class);
    }

    @Test
    void getAllDistrictsForKyivTest() {
        List<District> district = List.of(ModelUtils.getDistrict());
        when(districtRepository.findAllByCityId(eq(1L))).thenReturn(district);
        addressService.getAllDistrictsForKyiv();
        verify(districtRepository, times(1)).findAllByCityId(anyLong());
    }

    @Test
    void mapUpdatedOrderAddressFieldsTest() throws Exception {
        Method mapUpdatedOrderAddressFieldsMethod = AddressServiceImpl.class.getDeclaredMethod("mapUpdatedOrderAddressFields",
                OrderAddress.class, OrderAddress.class, String.class);
        mapUpdatedOrderAddressFieldsMethod.setAccessible(true);
        Location location = ModelUtils.getLocation();
        String comment = "New comment test";
        OrderAddress actual = OrderAddress.builder().
                location(location)
                .id(1L)
                .actual(false)
                .addressComment("No comments")
                .coordinates(location.getCoordinates())
                .addressStatus(AddressStatus.DELETED)
                .build();
        OrderAddress expected = ModelUtils.getOrderAddress();
        mapUpdatedOrderAddressFieldsMethod.invoke(addressService, expected, actual, comment);
        assertEquals(expected.getLocation(), actual.getLocation());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getActual(), actual.getActual());
        assertEquals(comment, actual.getAddressComment());
        assertEquals(expected.getCoordinates(), actual.getCoordinates());
        assertEquals(expected.getAddressStatus(), actual.getAddressStatus());
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
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());
        when(mapper.map(any(OrderAddressExportDetailsDtoUpdate.class), eq(Address.class))).thenReturn(address);
        when(cityRepository.findCityByRegionIdAndNameUkAndNameEn(anyLong(), anyString(), anyString())).thenReturn(Optional.of(city));
        when(districtRepository.findDistrictByCityIdAndNameEnOrNameUk(anyLong(), anyString(), anyString())).thenReturn(Optional.of(district));
        when(mapper.map(any(Address.class), eq(OrderAddress.class))).thenReturn(orderAddress);
        when(mapper.map(any(OrderAddress.class), eq(OrderAddressDtoResponse.class))).thenReturn(orderAddressDtoResponse);
        addressService.addressUpdate(addressDto, "email@gmail.com");
        verify(orderRepository, times(1)).findById(order.getId());
        verify(orderAddressRepository, times(1)).save(any());

    }
}
