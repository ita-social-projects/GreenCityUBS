package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.bag.BagLimitDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.tariff.AddNewTariffResponseDto;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.dto.tariff.EditTariffDto;
import greencity.dto.tariff.GetTariffLimitsDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.Bag;
import greencity.entity.order.Courier;
import greencity.entity.order.Service;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierLimit;
import greencity.enums.CourierStatus;
import greencity.enums.LocationStatus;
import greencity.enums.StationStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import greencity.repository.BagRepository;
import greencity.repository.CourierRepository;
import greencity.repository.DeactivateChosenEntityRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.LocationRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.RegionRepository;
import greencity.repository.ServiceRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.SuperAdminService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@org.springframework.stereotype.Service
@Data
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final RegionRepository regionRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;
    private final TariffLocationRepository tariffsLocationRepository;
    private final DeactivateChosenEntityRepository deactivateTariffsForChosenParamRepository;
    private static final String BAD_SIZE_OF_REGIONS_MESSAGE =
        "Region ids size should be 1 if several params are selected";
    private static final String REGIONS_NOT_EXIST_MESSAGE = "Current region doesn't exist: %s";
    private static final String REGIONS_OR_CITIES_NOT_EXIST_MESSAGE = "Current regions %s or cities %s don't exist.";
    private static final String COURIER_NOT_EXISTS_MESSAGE = "Current courier doesn't exist: %s";
    private static final String RECEIVING_STATIONS_NOT_EXIST_MESSAGE = "Current receiving stations don't exist: %s";
    private static final String RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE =
        "Current receiving stations: %s or courier: %s don't exist.";
    private static final String REGION_OR_COURIER_NOT_EXIST_MESSAGE = "Current region: %s or courier: %s don't exist.";
    private static final String REGION_OR_RECEIVING_STATIONS_NOT_EXIST_MESSAGE =
        "Current region: %s or receiving stations: %s don't exist.";
    private static final String REGION_OR_CITIES_OR_COURIER_NOT_EXIST_MESSAGE =
        "Current region: %s or cities: %s or courier: %s don't exist.";
    private static final String REGION_OR_RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE =
        "Current region: %s or receiving stations: %s or courier: %s don't exist.";
    private static final String REGION_OR_CITIES_OR_RECEIVING_STATIONS_NOT_EXIST_MESSAGE =
        "Current region: %s or cities: %s or receiving stations: %s don't exist.";
    private static final String REGION_OR_CITIES_OR_RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE =
        "Current region: %s or cities: %s or receiving stations: %s or courier: %s don't exist.";

    @Override
    public GetTariffServiceDto addTariffService(long tariffId, TariffServiceDto dto, String employeeUuid) {
        Bag bag = bagRepository.save(createBag(tariffId, dto, employeeUuid));
        return modelMapper.map(bag, GetTariffServiceDto.class);
    }

    private Bag createBag(long tariffId, TariffServiceDto dto, String employeeUuid) {
        TariffsInfo tariffsInfo = getTariffById(tariffId);
        Employee employee = getEmployeeByUuid(employeeUuid);
        return Bag.builder()
            .price(dto.getPrice())
            .capacity(dto.getCapacity())
            .commission(dto.getCommission())
            .fullPrice(getFullPrice(dto.getPrice(), dto.getCommission()))
            .limitIncluded(false)
            .name(dto.getName())
            .nameEng(dto.getNameEng())
            .description(dto.getDescription())
            .descriptionEng(dto.getDescriptionEng())
            .tariffsInfo(tariffsInfo)
            .createdAt(LocalDate.now())
            .createdBy(employee)
            .build();
    }

    @Override
    public List<GetTariffServiceDto> getTariffService(long tariffId) {
        if (tariffsInfoRepository.existsById(tariffId)) {
            return bagRepository.getAllByTariffsInfoId(tariffId)
                .stream()
                .map(it -> modelMapper.map(it, GetTariffServiceDto.class))
                .collect(Collectors.toList());
        } else {
            throw new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId);
        }
    }

    @Override
    public void deleteTariffService(Integer bagId) {
        Bag bag = tryToFindBagById(bagId);
        TariffsInfo tariffsInfo = bag.getTariffsInfo();
        bagRepository.delete(bag);
        List<Bag> bags = bagRepository.findBagsByTariffsInfoId(tariffsInfo.getId());
        if (bags.isEmpty() || bags.stream().noneMatch(Bag::getLimitIncluded)) {
            tariffsInfo.setTariffStatus(TariffStatus.NEW);
            tariffsInfo.setBags(bags);
            tariffsInfo.setMax(null);
            tariffsInfo.setMin(null);
            tariffsInfo.setLimitDescription(null);
            tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
            tariffsInfoRepository.save(tariffsInfo);
        }
    }

    @Override
    public GetTariffServiceDto editTariffService(TariffServiceDto dto, Integer id, String employeeUuid) {
        Bag bag = tryToFindBagById(id);
        Employee employee = getEmployeeByUuid(employeeUuid);
        bag.setCapacity(dto.getCapacity());
        bag.setPrice(dto.getPrice());
        bag.setCommission(dto.getCommission());
        bag.setFullPrice(getFullPrice(dto.getPrice(), dto.getCommission()));
        bag.setName(dto.getName());
        bag.setNameEng(dto.getNameEng());
        bag.setDescription(dto.getDescription());
        bag.setDescriptionEng(dto.getDescriptionEng());
        bag.setEditedAt(LocalDate.now());
        bag.setEditedBy(employee);
        return modelMapper.map(bagRepository.save(bag), GetTariffServiceDto.class);
    }

    private Bag tryToFindBagById(Integer id) {
        return bagRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
    }

    @Override
    public GetServiceDto addService(Long tariffId, ServiceDto dto, String employeeUuid) {
        Service service = serviceRepository.save(createService(tariffId, dto, employeeUuid));
        return modelMapper.map(service, GetServiceDto.class);
    }

    private Service createService(Long tariffId, ServiceDto dto, String employeeUuid) {
        if (getServiceByTariffsInfoId(tariffId).isEmpty()) {
            Employee employee = getEmployeeByUuid(employeeUuid);
            TariffsInfo tariffsInfo = getTariffById(tariffId);
            return Service.builder()
                .price(dto.getPrice())
                .createdAt(LocalDate.now())
                .createdBy(employee)
                .name(dto.getName())
                .nameEng(dto.getNameEng())
                .description(dto.getDescription())
                .descriptionEng(dto.getDescriptionEng())
                .tariffsInfo(tariffsInfo)
                .build();
        } else {
            throw new ServiceAlreadyExistsException(ErrorMessage.SERVICE_ALREADY_EXISTS + tariffId);
        }
    }

    @Override
    public GetServiceDto getService(long tariffId) {
        return getServiceByTariffsInfoId(tariffId)
            .map(it -> modelMapper.map(it, GetServiceDto.class))
            .orElse(null);
    }

    @Override
    public void deleteService(long id) {
        serviceRepository.delete(getServiceById(id));
    }

    @Override
    public GetServiceDto editService(Long id, ServiceDto dto, String employeeUuid) {
        Service service = getServiceById(id);
        Employee employee = getEmployeeByUuid(employeeUuid);
        service.setPrice(dto.getPrice());
        service.setName(dto.getName());
        service.setNameEng(dto.getNameEng());
        service.setDescription(dto.getDescription());
        service.setDescriptionEng(dto.getDescriptionEng());
        service.setEditedAt(LocalDate.now());
        service.setEditedBy(employee);
        serviceRepository.save(service);
        return modelMapper.map(service, GetServiceDto.class);
    }

    private Employee getEmployeeByUuid(String employeeUuid) {
        return employeeRepository.findByUuid(employeeUuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + employeeUuid));
    }

    private Optional<Service> getServiceByTariffsInfoId(long tariffId) {
        if (tariffsInfoRepository.existsById(tariffId)) {
            return serviceRepository.findServiceByTariffsInfoId(tariffId);
        } else {
            throw new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId);
        }
    }

    private Service getServiceById(long id) {
        return serviceRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
    }

    private TariffsInfo getTariffById(long id) {
        return tariffsInfoRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + id));
    }

    @Override
    public List<LocationInfoDto> getAllLocation() {
        return regionRepository.findAll().stream()
            .map(i -> modelMapper.map(i, LocationInfoDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<LocationInfoDto> getActiveLocations() {
        return regionRepository.findRegionsWithActiveLocations().stream()
            .distinct()
            .map(region -> modelMapper.map(region, LocationInfoDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void addLocation(List<LocationCreateDto> dtoList) {
        dtoList.forEach(locationCreateDto -> {
            Region region = checkIfRegionAlreadyCreated(locationCreateDto);
            Location location = createNewLocation(locationCreateDto, region);
            checkIfLocationAlreadyCreated(locationCreateDto.getAddLocationDtoList(), region.getId());
            locationRepository.save(location);
        });
    }

    private Location createNewLocation(LocationCreateDto dto, Region region) {
        return Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .coordinates(Coordinates.builder().latitude(dto.getLatitude()).longitude(dto.getLongitude()).build())
            .nameEn(dto.getAddLocationDtoList().stream().filter(x -> x.getLanguageCode().equals("en")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName())
            .nameUk(dto.getAddLocationDtoList().stream().filter(x -> x.getLanguageCode().equals("ua")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName())
            .region(region)
            .build();
    }

    private void checkIfLocationAlreadyCreated(List<AddLocationTranslationDto> dto, Long regionId) {
        Optional<Location> location = locationRepository.findLocationByNameAndRegionId(
            dto.stream().filter(translation -> translation.getLanguageCode().equals("ua")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName(),
            dto.stream().filter(translation -> translation.getLanguageCode().equals("en")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName(),
            regionId);

        if (location.isPresent()) {
            throw new NotFoundException("The location with name: "
                + dto.get(0).getLocationName() + ErrorMessage.LOCATION_ALREADY_EXIST);
        }
    }

    private Region checkIfRegionAlreadyCreated(LocationCreateDto dto) {
        String enName = dto.getRegionTranslationDtos().stream()
            .filter(regionTranslationDto -> regionTranslationDto.getLanguageCode().equals("en")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();
        String ukName = dto.getRegionTranslationDtos().stream()
            .filter(regionTranslationDto -> regionTranslationDto.getLanguageCode().equals("ua")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();

        Region region = regionRepository.findRegionByEnNameAndUkrName(enName, ukName).orElse(null);

        if (null == region) {
            region = createRegionWithTranslation(dto);
            region = regionRepository.save(region);
        }
        return region;
    }

    @Override
    public void deactivateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.DEACTIVATED.equals(location.getLocationStatus())) {
            throw new BadRequestException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.DEACTIVATED);
        locationRepository.save(location);
    }

    @Override
    public void activateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.ACTIVE.equals(location.getLocationStatus())) {
            throw new BadRequestException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.ACTIVE);
        locationRepository.save(location);
    }

    @Override
    public CreateCourierDto createCourier(CreateCourierDto dto, String uuid) {
        Employee employee = employeeRepository.findByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + uuid));

        checkIfCourierAlreadyExists(courierRepository.findAll(), dto);

        Courier courier = new Courier();
        courier.setCreatedBy(employee);
        courier.setCourierStatus(CourierStatus.ACTIVE);
        courier.setCreateDate(LocalDate.now());
        courier.setNameEn(dto.getNameEn());
        courier.setNameUk(dto.getNameUk());
        courierRepository.save(courier);
        return modelMapper.map(courier, CreateCourierDto.class);
    }

    private void checkIfCourierAlreadyExists(List<Courier> couriers, CreateCourierDto createCourierDto) {
        couriers
            .forEach(courier -> {
                if (courier.getNameEn().equals(createCourierDto.getNameEn())
                    || courier.getNameUk().equals(createCourierDto.getNameUk())) {
                    throw new CourierAlreadyExists(ErrorMessage.COURIER_ALREADY_EXISTS);
                }
            });
    }

    @Override
    public CourierDto updateCourier(CourierUpdateDto dto) {
        Courier courier = courierRepository.findById(dto.getCourierId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID));
        courier.setNameUk(dto.getNameUk());
        courier.setNameEn(dto.getNameEn());
        courierRepository.save(courier);
        return modelMapper.map(courier, CourierDto.class);
    }

    @Override
    public List<CourierDto> getAllCouriers() {
        return courierRepository.findAll().stream().map(courier -> modelMapper.map(courier, CourierDto.class))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CourierDto deactivateCourier(Long id) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        if (CourierStatus.DEACTIVATED == courier.getCourierStatus()) {
            throw new BadRequestException(ErrorMessage.CANNOT_DEACTIVATE_COURIER + courier.getId());
        }
        deactivateTariffsForChosenParamRepository.deactivateTariffsByCourier(id);
        courier.setCourierStatus(CourierStatus.DEACTIVATED);
        return modelMapper.map(courier, CourierDto.class);
    }

    private Integer getFullPrice(Integer price, Integer commission) {
        return price + commission;
    }

    @Override
    public List<GetTariffsInfoDto> getAllTariffsInfo(TariffsInfoFilterCriteria filterCriteria) {
        List<TariffsInfo> tariffs = tariffsInfoRepository.findAll(new TariffsInfoSpecification(filterCriteria));
        return tariffs
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffsInfoDto.class))
            .sorted(Comparator.comparing(tariff -> tariff.getRegionDto().getNameUk()))
            .collect(Collectors.toList());
    }

    private Region createRegionWithTranslation(LocationCreateDto dto) {
        String enName = dto.getRegionTranslationDtos().stream().filter(x -> x.getLanguageCode().equals("en")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();
        String uaName = dto.getRegionTranslationDtos().stream().filter(x -> x.getLanguageCode().equals("ua")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();

        return Region.builder()
            .enName(enName)
            .ukrName(uaName)
            .build();
    }

    private Location tryToFindLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID + id));
    }

    @Override
    public ReceivingStationDto createReceivingStation(AddingReceivingStationDto dto, String uuid) {
        if (!receivingStationRepository.existsReceivingStationByName(dto.getName())) {
            Employee employee = employeeRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + uuid));
            ReceivingStation receivingStation = receivingStationRepository.save(buildReceivingStation(dto, employee));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new UnprocessableEntityException(
            ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS + dto.getName());
    }

    private ReceivingStation buildReceivingStation(AddingReceivingStationDto dto, Employee employee) {
        return ReceivingStation.builder()
            .name(dto.getName())
            .createdBy(employee)
            .createDate(LocalDate.now())
            .stationStatus(StationStatus.ACTIVE)
            .build();
    }

    @Override
    public List<ReceivingStationDto> getAllReceivingStations() {
        return receivingStationRepository.findAll().stream()
            .map(r -> modelMapper.map(r, ReceivingStationDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public ReceivingStationDto updateReceivingStation(ReceivingStationDto dto) {
        ReceivingStation receivingStation = receivingStationRepository.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + dto.getId()));
        receivingStation.setName(dto.getName());
        receivingStationRepository.save(receivingStation);
        return modelMapper.map(receivingStation, ReceivingStationDto.class);
    }

    @Override
    public void deleteReceivingStation(Long id) {
        ReceivingStation station = receivingStationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + id));
        receivingStationRepository.delete(station);
    }

    private Set<Location> findLocationsForTariff(List<Long> locationId, Long regionId) {
        Set<Location> locationSet = new HashSet<>(locationRepository
            .findAllByIdAndRegionId(locationId.stream().distinct().collect(Collectors.toList()), regionId));
        if (locationSet.isEmpty()) {
            throw new NotFoundException("List of locations can not be empty");
        }
        return locationSet;
    }

    private Set<ReceivingStation> findReceivingStationsForTariff(List<Long> receivingStationIdList) {
        if (receivingStationIdList == null) {
            return Collections.emptySet();
        }

        Set<ReceivingStation> receivingStations = new HashSet<>(receivingStationRepository
            .findAllById(receivingStationIdList.stream().distinct().collect(Collectors.toList())));
        if (receivingStations.isEmpty()) {
            throw new NotFoundException("List of receiving stations can not be empty");
        }
        return receivingStations;
    }

    private TariffsInfo tryToFindTariffById(Long tariffId) {
        return tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId));
    }

    @Override
    @Transactional
    public AddNewTariffResponseDto addNewTariff(AddNewTariffDto addNewTariffDto, String userUUID) {
        Courier courier = tryToFindCourier(addNewTariffDto.getCourierId());
        checkIfCourierHasStatusDeactivated(courier);
        List<Long> idListToCheck = new ArrayList<>(addNewTariffDto.getLocationIdList());
        final var tariffForLocationAndCourierAlreadyExistIdList =
            verifyIfTariffExists(idListToCheck, addNewTariffDto.getCourierId());
        TariffsInfo tariffsInfo = createTariff(addNewTariffDto, userUUID, courier);
        var tariffLocationSet =
            findLocationsForTariff(idListToCheck, addNewTariffDto.getRegionId())
                .stream().map(location -> TariffLocation.builder()
                    .tariffsInfo(tariffsInfo)
                    .location(location)
                    .locationStatus(LocationStatus.ACTIVE)
                    .build())
                .collect(Collectors.toSet());
        List<Long> existingLocationsIds =
            tariffLocationSet.stream().map(tariffLocation -> tariffLocation.getLocation().getId())
                .collect(Collectors.toList());
        idListToCheck.removeAll(existingLocationsIds);
        tariffsInfo.setTariffLocations(tariffLocationSet);
        tariffsLocationRepository.saveAll(tariffLocationSet);
        tariffsInfoRepository.save(tariffsInfo);
        updateEmployeeTariffsInfoMapping(tariffsInfo);
        return new AddNewTariffResponseDto(tariffForLocationAndCourierAlreadyExistIdList, idListToCheck);
    }

    private void checkIfCourierHasStatusDeactivated(Courier courier) {
        if (courier.getCourierStatus().equals(CourierStatus.DEACTIVATED)) {
            throw new BadRequestException(ErrorMessage.CANNOT_CREATE_TARIFF + courier.getId());
        }
    }

    private TariffsInfo createTariff(AddNewTariffDto addNewTariffDto, String uuid, Courier courier) {
        TariffsInfo tariffsInfo = TariffsInfo.builder()
            .createdAt(LocalDate.now())
            .courier(courier)
            .receivingStationList(findReceivingStationsForTariff(addNewTariffDto.getReceivingStationsIdList()))
            .tariffStatus(TariffStatus.NEW)
            .creator(employeeRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + uuid)))
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .build();
        return tariffsInfoRepository.save(tariffsInfo);
    }

    private void updateEmployeeTariffsInfoMapping(TariffsInfo tariffsInfo) {
        employeeRepository.findAllByEmployeePositionId(6L)
            .forEach(e -> setEmployeeTariffInfos(e, tariffsInfo));
    }

    private void setEmployeeTariffInfos(Employee employee, TariffsInfo tariffsInfo) {
        Set<TariffsInfo> tariffsInfos = employee.getTariffInfos();
        tariffsInfos.add(tariffsInfo);
        employee.setTariffInfos(tariffsInfos);
    }

    private List<Long> verifyIfTariffExists(List<Long> locationIds, Long courierId) {
        var tariffLocationListList = tariffsLocationRepository
            .findAllByCourierIdAndLocationIds(courierId, locationIds);
        List<Long> alreadyExistsTariff = tariffLocationListList.stream()
            .map(tariffLocation -> tariffLocation.getLocation().getId())
            .collect(Collectors.toList());
        if (alreadyExistsTariff.stream().anyMatch(locationIds::contains)) {
            throw new TariffAlreadyExistsException(ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
        }
        return alreadyExistsTariff;
    }

    private Courier tryToFindCourier(Long courierId) {
        return courierRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
    }

    private ReceivingStation tryToFindReceivingStationById(Long id) {
        return receivingStationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + id));
    }

    private List<Location> tryToFindLocationsInSameRegion(List<Long> locationIds) {
        List<Location> locations = locationIds.stream()
            .map(this::tryToFindLocationById)
            .collect(Collectors.toList());
        long regionsCount = locations.stream()
            .map(Location::getRegion)
            .distinct()
            .count();
        if (regionsCount != 1) {
            throw new BadRequestException(ErrorMessage.LOCATIONS_BELONG_TO_DIFFERENT_REGIONS);
        }
        return locations;
    }

    private void checkIfLocationBelongsToAnotherTariff(List<Long> locationIds, TariffsInfo tariffsInfo) {
        long tariffsWithLocationsCount = tariffsLocationRepository
            .findAllByCourierIdAndLocationIds(tariffsInfo.getCourier().getId(), locationIds)
            .stream()
            .filter(it -> !tariffsInfo.equals(it.getTariffsInfo()))
            .count();
        if (tariffsWithLocationsCount != 0) {
            throw new TariffAlreadyExistsException(ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
        }
    }

    private Set<ReceivingStation> tryToFindReceivingStations(List<Long> receivingStationIds) {
        return receivingStationIds.stream()
            .map(this::tryToFindReceivingStationById)
            .collect(Collectors.toSet());
    }

    private Set<TariffLocation> getUpdatedTariffLocations(List<Location> locations, TariffsInfo tariffsInfo) {
        return locations.stream()
            .map(location -> updateTariffLocation(location, tariffsInfo))
            .collect(Collectors.toSet());
    }

    private TariffLocation updateTariffLocation(Location location, TariffsInfo tariffsInfo) {
        return tariffsLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location)
            .orElseGet(() -> TariffLocation.builder()
                .tariffsInfo(tariffsInfo)
                .location(location)
                .locationStatus(LocationStatus.ACTIVE)
                .build());
    }

    private void deleteUnusedTariffLocations(Set<TariffLocation> tariffLocations, TariffsInfo tariffsInfo) {
        tariffsLocationRepository.findAllByTariffsInfo(tariffsInfo)
            .forEach(it -> {
                if (!tariffLocations.contains(it)) {
                    tariffsLocationRepository.delete(it);
                }
            });
    }

    @Override
    @Transactional
    public void editTariff(Long id, EditTariffDto dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(id);
        List<Location> locations = tryToFindLocationsInSameRegion(dto.getLocationIds());
        checkIfLocationBelongsToAnotherTariff(dto.getLocationIds(), tariffsInfo);
        Set<ReceivingStation> receivingStations = tryToFindReceivingStations(dto.getReceivingStationIds());
        Set<TariffLocation> tariffLocations = getUpdatedTariffLocations(locations, tariffsInfo);
        deleteUnusedTariffLocations(tariffLocations, tariffsInfo);

        tariffsInfo.setReceivingStationList(receivingStations);
        tariffsInfo.setTariffLocations(tariffLocations);

        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public boolean checkIfTariffExists(AddNewTariffDto addNewTariffDto) {
        List<TariffLocation> tariffLocations = tariffsLocationRepository.findAllByCourierIdAndLocationIds(
            addNewTariffDto.getCourierId(), addNewTariffDto.getLocationIdList());

        return (!CollectionUtils.isEmpty(tariffLocations));
    }

    @Override
    @Transactional
    public void setTariffLimits(Long tariffId, SetTariffLimitsDto dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        checkIfLimitParamsAreValid(dto);
        List<Bag> bags = dto.getBagLimitDtoList()
            .stream()
            .map(bagDto -> changeBagLimitIncluded(bagDto, tariffId))
            .collect(Collectors.toList());
        bagRepository.saveAll(bags);

        tariffsInfo.setMin(dto.getMin());
        tariffsInfo.setMax(dto.getMax());
        tariffsInfo.setCourierLimit(dto.getCourierLimit());
        tariffsInfo.setLimitDescription(dto.getLimitDescription());
        tariffsInfo.setTariffStatus(getChangedTariffStatus(dto.getMin(), dto.getMax()));
        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public GetTariffLimitsDto getTariffLimits(Long tariffId) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        return modelMapper.map(tariffsInfo, GetTariffLimitsDto.class);
    }

    private void checkIfLimitParamsAreValid(SetTariffLimitsDto dto) {
        boolean isBagLimitIncludedTrue = dto.getBagLimitDtoList()
            .stream()
            .anyMatch(BagLimitDto::getLimitIncluded);
        boolean areMinOrMaxNotNull = dto.getMin() != null || dto.getMax() != null;
        boolean areParamsValid = dto.getCourierLimit() != null
            && ((areMinOrMaxNotNull && isBagLimitIncludedTrue)
                || (!areMinOrMaxNotNull && !isBagLimitIncludedTrue));
        if (!areParamsValid) {
            throw new BadRequestException(ErrorMessage.TARIFF_LIMITS_ARE_INPUTTED_INCORRECTLY);
        }
        checkIfMinAndMaxLimitValuesAreCorrect(dto.getMax(), dto.getMin());
    }

    private void checkIfMinAndMaxLimitValuesAreCorrect(Long max, Long min) {
        if (min != null && max != null) {
            if (min.equals(max)) {
                throw new BadRequestException(ErrorMessage.MIN_MAX_VALUE_RESTRICTION);
            } else if (min > max) {
                throw new BadRequestException(ErrorMessage.MAX_VALUE_IS_INCORRECT);
            }
        }
    }

    private Bag changeBagLimitIncluded(BagLimitDto dto, Long tariffId) {
        Bag bag = tryToFindBagById(dto.getId());
        if (!bag.getTariffsInfo().getId().equals(tariffId)) {
            throw new BadRequestException(String.format(ErrorMessage.BAG_FOR_TARIFF_NOT_EXIST, bag.getId(), tariffId));
        }
        bag.setLimitIncluded(dto.getLimitIncluded());
        return bag;
    }

    private TariffStatus getChangedTariffStatus(Long min, Long max) {
        return min != null || max != null
            ? TariffStatus.ACTIVE
            : TariffStatus.DEACTIVATED;
    }

    @Override
    @Transactional
    public void switchTariffStatus(Long tariffId, String tariffStatus) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        TariffStatus status = tryToGetTariffStatus(tariffStatus);
        if (tariffsInfo.getTariffStatus().equals(status)) {
            throw new BadRequestException(
                String.format(ErrorMessage.TARIFF_ALREADY_HAS_THIS_STATUS, tariffId, tariffStatus.toUpperCase()));
        }
        if (status.equals(TariffStatus.ACTIVE)) {
            checkIfTariffParamsAreValidForActivation(tariffsInfo);
        }
        tariffsInfo.setTariffStatus(status);
        tariffsInfoRepository.save(tariffsInfo);
    }

    private TariffStatus tryToGetTariffStatus(String tariffStatus) {
        if (Objects.equals(tariffStatus.toUpperCase(), "ACTIVE")
            || Objects.equals(tariffStatus.toUpperCase(), "DEACTIVATED")) {
            return TariffStatus.valueOf(tariffStatus.toUpperCase());
        }
        throw new BadRequestException(ErrorMessage.UNRESOLVABLE_TARIFF_STATUS);
    }

    private void checkIfTariffParamsAreValidForActivation(TariffsInfo tariffsInfo) {
        if (tariffsInfo.getBags().isEmpty()) {
            throw new BadRequestException(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_BAGS);
        }
        if (tariffsInfo.getMin() == null && tariffsInfo.getMax() == null) {
            throw new BadRequestException(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_LIMITS);
        }
        if (tariffsInfo.getCourier().getCourierStatus().equals(CourierStatus.DEACTIVATED)) {
            throw new BadRequestException(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_DEACTIVATED_COURIER
                + tariffsInfo.getCourier().getId());
        }
    }

    @Override
    public void editLocations(List<EditLocationDto> editLocationDtoList) {
        editLocationDtoList.forEach(this::editLocation);
    }

    private void editLocation(EditLocationDto editLocationDto) {
        Location location = tryToFindLocationById(editLocationDto.getLocationId());
        if (!locationExists(editLocationDto.getNameUa(), editLocationDto.getNameEn(), location.getRegion())) {
            location.setNameUk(editLocationDto.getNameUa());
            location.setNameEn(editLocationDto.getNameEn());
            locationRepository.save(location);
        }
    }

    private boolean locationExists(String nameUk, String nameEn, Region region) {
        return locationRepository.existsByNameUkAndNameEnAndRegion(nameUk, nameEn, region);
    }

    @Override
    @Transactional
    public void changeTariffLocationsStatus(Long tariffId, ChangeTariffLocationStatusDto dto, String param) {
        tryToFindTariffById(tariffId);
        if ("activate".equalsIgnoreCase(param)) {
            tariffsLocationRepository.changeStatusAll(tariffId, dto.getLocationIds(), LocationStatus.ACTIVE.name());
        } else if ("deactivate".equalsIgnoreCase(param)) {
            tariffsLocationRepository.changeStatusAll(tariffId, dto.getLocationIds(),
                LocationStatus.DEACTIVATED.name());
        } else {
            throw new BadRequestException("Unresolvable param");
        }
    }

    @Override
    @Transactional
    public void deactivateTariffForChosenParam(DetailsOfDeactivateTariffsDto details) {
        if (shouldDeactivateTariffsByRegions(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegions(details.getRegionsIds().get());
        } else if (shouldDeactivateTariffsByRegionsAndCities(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegionsAndCities(
                details.getCitiesIds().get(),
                details.getRegionsIds().get().get(0));
        } else if (shouldDeactivateTariffsByCourier(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourier(details.getCourierId().get());
        } else if (shouldDeactivateTariffsByReceivingStations(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByReceivingStations(
                details.getStationsIds().get());
        } else if (shouldDeactivateTariffsByCourierAndReceivingStations(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourierAndReceivingStations(
                details.getCourierId().get(), details.getStationsIds().get());
        } else if (shouldDeactivateTariffsByCourierAndRegion(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourierAndRegion(
                details.getRegionsIds().get().get(0), details.getCourierId().get());
        } else if (shouldDeactivateTariffsByRegionAndCityAndStation(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegionAndCitiesAndStations(
                details.getRegionsIds().get().get(0), details.getCitiesIds().get(),
                details.getStationsIds().get());
        } else if (shouldDeactivateTariffsByAll(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByAllParam(
                details.getRegionsIds().get().get(0), details.getCitiesIds().get(),
                details.getStationsIds().get(), details.getCourierId().get());
        } else if (shouldDeactivateTariffsByRegionAndReceivingStations(details)) {
            tariffsInfoRepository.deactivateTariffsByRegionAndReceivingStations(
                details.getRegionsIds().get().get(0), details.getStationsIds().get());
        } else if (shouldDeactivateTariffsByCourierAndRegionAndCities(details)) {
            tariffsInfoRepository.deactivateTariffsByCourierAndRegionAndCities(
                details.getRegionsIds().get().get(0), details.getCitiesIds().get(),
                details.getCourierId().get());
        } else if (shouldDeactivateTariffsByCourierAndRegionAndReceivingStations(details)) {
            tariffsInfoRepository.deactivateTariffsByCourierAndRegionAndReceivingStations(
                details.getRegionsIds().get().get(0), details.getStationsIds().get(),
                details.getCourierId().get());
        } else {
            throw new BadRequestException("Bad request. Please choose another combination of parameters");
        }
    }

    /**
     * Method that checks if the tariff should be deactivated by details. In this
     * case size of RegionsList should be one because we choose more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by details and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByAll(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isPresent()
            && details.getStationsIds().isPresent() && details.getCourierId().isPresent()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(
                        details.getCitiesIds().get(),
                        details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsIds().get())
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(
                        REGION_OR_CITIES_OR_RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getCitiesIds().get(),
                        details.getStationsIds().get(), details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id, cities
     * ids and receiving stations ids. In this case size of RegionsList should be
     * one because we choose more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by region id, cities id and
     *         receiving stations and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegionAndCityAndStation(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isPresent()
            && details.getStationsIds().isPresent() && details.getCourierId().isEmpty()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isCitiesExistForRegion(details.getCitiesIds().get(),
                            details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsIds().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGION_OR_CITIES_OR_RECEIVING_STATIONS_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getCitiesIds().get(),
                        details.getStationsIds().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id and
     * courier id. In this case size of RegionsList should be one because we choose
     * more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and courier id and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourierAndRegion(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCourierId().isPresent()
            && details.getCitiesIds().isEmpty() && details.getStationsIds().isEmpty()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGION_OR_COURIER_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id and
     * receiving stations ids.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by receiving stations ids and
     *         courier id and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourierAndReceivingStations(DetailsOfDeactivateTariffsDto details) {
        if (details.getStationsIds().isPresent() && details.getCourierId().isPresent()
            && details.getRegionsIds().isEmpty() && details.getCitiesIds().isEmpty()) {
            if (courierRepository.existsCourierById(details.getCourierId().get())
                && deactivateTariffsForChosenParamRepository
                    .isReceivingStationsExists(details.getStationsIds().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE,
                    details.getStationsIds().get(), details.getCourierId().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id and
     * receiving stations ids. In this case size of RegionsList should be one
     * because we choose more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and receiving
     *         stations ids and false if not.
     * @author Lilia Mokhnatska.
     */
    private boolean shouldDeactivateTariffsByRegionAndReceivingStations(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCourierId().isEmpty()
            && details.getCitiesIds().isEmpty() && details.getStationsIds().isPresent()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsIds().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGION_OR_RECEIVING_STATIONS_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getStationsIds().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id, region
     * id and cities ids. In this case size of RegionsList should be one because we
     * choose more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier ids.
     * @return true if you have to deactivate tariff by courier id, region id and
     *         cities ids false if not.
     * @author Lilia Mokhnatska.
     */
    private boolean shouldDeactivateTariffsByCourierAndRegionAndCities(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isPresent()
            && details.getStationsIds().isEmpty() && details.getCourierId().isPresent()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(
                        details.getCitiesIds().get(),
                        details.getRegionsIds().get().get(0))
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(
                        REGION_OR_CITIES_OR_COURIER_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getCitiesIds().get(),
                        details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id, region
     * id and receiving stations ids. In this case size of RegionsList should be one
     * because we choose more than one param.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by courier id, region id and
     *         receiving stations ids and false if not.
     * @author Lilia Mokhnatska.
     */
    private boolean shouldDeactivateTariffsByCourierAndRegionAndReceivingStations(
        DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isEmpty()
            && details.getStationsIds().isPresent() && details.getCourierId().isPresent()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsIds().get())
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(
                        REGION_OR_RECEIVING_STATIONS_OR_COURIER_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getStationsIds().get(),
                        details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by receiving stations
     * ids.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by receiving stations ids and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByReceivingStations(DetailsOfDeactivateTariffsDto details) {
        if (details.getStationsIds().isPresent() && details.getRegionsIds().isEmpty()
            && details.getCitiesIds().isEmpty() && details.getCourierId().isEmpty()) {
            if (deactivateTariffsForChosenParamRepository
                .isReceivingStationsExists(details.getStationsIds().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(RECEIVING_STATIONS_NOT_EXIST_MESSAGE,
                    details.getStationsIds().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by courier id and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourier(DetailsOfDeactivateTariffsDto details) {
        if (details.getCourierId().isPresent() && details.getRegionsIds().isEmpty()
            && details.getCitiesIds().isEmpty() && details.getStationsIds().isEmpty()) {
            if (courierRepository.existsCourierById(details.getCourierId().get())) {
                return true;
            } else {
                throw new NotFoundException(
                    String.format(COURIER_NOT_EXISTS_MESSAGE, details.getCourierId().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id and
     * cities ids. In this case size of RegionsList should be one because we choose
     * more than one param.
     *
     * @param details - contains regions ids cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and cities ids and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegionsAndCities(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isPresent()
            && details.getStationsIds().isEmpty() && details.getCourierId().isEmpty()) {
            if (details.getRegionsIds().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsIds().get().get(0))
                    && deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(
                        details.getCitiesIds().get(),
                        details.getRegionsIds().get().get(0))) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGIONS_OR_CITIES_NOT_EXIST_MESSAGE,
                        details.getRegionsIds().get(), details.getCitiesIds().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id.
     *
     * @param details - contains regions ids, cities ids, receiving stations ids and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegions(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsIds().isPresent() && details.getCitiesIds().isEmpty()
            && details.getStationsIds().isEmpty() && details.getCourierId().isEmpty()) {
            if (deactivateTariffsForChosenParamRepository.isRegionsExists(details.getRegionsIds().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(
                    REGIONS_NOT_EXIST_MESSAGE, details.getRegionsIds().get()));
            }
        }
        return false;
    }
}
