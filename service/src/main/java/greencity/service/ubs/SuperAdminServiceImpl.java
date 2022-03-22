package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.CourierStatus;
import greencity.entity.enums.LocationStatus;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierLocation;
import greencity.entity.order.CourierTranslation;
import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.Region;
import greencity.entity.user.RegionTranslation;
import greencity.entity.user.User;
import greencity.exceptions.*;
import greencity.entity.user.employee.ReceivingStation;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final BagTranslationRepository translationRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceTranslationRepository serviceTranslationRepository;
    private final LocationRepository locationRepository;
    private final LanguageRepository languageRepository;
    private final LocationTranslationRepository locationTranslationRepository;
    private final CourierRepository courierRepository;
    private final CourierTranslationRepository courierTranslationRepository;
    private final CourierLocationRepository courierLocationRepository;
    private final RegionRepository regionRepository;
    private final RegionTranslationRepository regionTranslationRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;

    @Override
    public AddServiceDto addTariffService(AddServiceDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Bag bag = createBagWithFewTranslation(dto, user);
        bagRepository.save(bag);
        translationRepository.saveAll(bag.getBagTranslations());
        return modelMapper.map(bag, AddServiceDto.class);
    }

    private Bag createBagWithFewTranslation(AddServiceDto dto, User user) {
        final Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        Bag bag = Bag.builder().price(dto.getPrice())
            .capacity(dto.getCapacity())
            .location(location)
            .commission(dto.getCommission())
            .fullPrice(getFullPrice(dto.getPrice(), dto.getCommission()))
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .createdAt(LocalDate.now())
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .bagTranslations(dto.getTariffTranslationDtoList().stream()
                .map(tariffTranslationDto -> BagTranslation.builder()
                    .name(tariffTranslationDto.getName())
                    .nameEng(tariffTranslationDto.getNameEng())
                    .description(tariffTranslationDto.getDescription())
                    .build())
                .collect(Collectors.toList()))
            .build();
        bag.getBagTranslations().forEach(bagTranslation -> bagTranslation.setBag(bag));
        return bag;
    }

    @Override
    public List<GetTariffServiceDto> getTariffService() {
        return translationRepository.findAll()
            .stream()
            .map(this::getTariffService)
            .collect(Collectors.toList());
    }

    private GetTariffServiceDto getTariffService(BagTranslation bagTranslation) {
        return GetTariffServiceDto.builder()
            .description(bagTranslation.getDescription())
            .price(bagTranslation.getBag().getPrice())
            .capacity(bagTranslation.getBag().getCapacity())
            .name(bagTranslation.getName())
            .commission(bagTranslation.getBag().getCommission())
            .nameEng(bagTranslation.getNameEng())
            .fullPrice(bagTranslation.getBag().getFullPrice())
            .id(bagTranslation.getBag().getId())
            .createdAt(bagTranslation.getBag().getCreatedAt())
            .createdBy(bagTranslation.getBag().getCreatedBy())
            .editedAt(bagTranslation.getBag().getEditedAt())
            .editedBy(bagTranslation.getBag().getEditedBy())
            .locationId(bagTranslation.getBag().getLocation().getId())
            .minAmountOfBag(bagTranslation.getBag().getMinAmountOfBags().toString())
            .build();
    }

    @Override
    public void deleteTariffService(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new BagNotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        bagRepository.delete(bag);
    }

    @Override
    public GetTariffServiceDto editTariffService(EditTariffServiceDto dto, Integer id, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Bag bag = bagRepository.findById(id).orElseThrow(() -> new BagNotFoundException(ErrorMessage.BAG_NOT_FOUND));
        bag.setPrice(dto.getPrice());
        bag.setCapacity(dto.getCapacity());
        bag.setCommission(dto.getCommission());
        bag.setFullPrice(getFullPrice(dto.getPrice(), dto.getCommission()));
        bag.setEditedAt(LocalDate.now());
        bag.setEditedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        bagRepository.save(bag);
        BagTranslation bagTranslation =
            translationRepository.findBagTranslationByBag(bag);
        bagTranslation.setName(dto.getName());
        bagTranslation.setDescription(dto.getDescription());
        translationRepository.save(bagTranslation);
        return getTariffService(bagTranslation);
    }

    @Override
    public CreateServiceDto addService(CreateServiceDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Service service = createServiceWithTranslation(dto, user);
        service.setFullPrice(getFullPrice(dto.getPrice(), dto.getCommission()));
        serviceRepository.save(service);
        serviceTranslationRepository.saveAll(service.getServiceTranslations());
        return modelMapper.map(service, CreateServiceDto.class);
    }

    private Service createServiceWithTranslation(CreateServiceDto dto, User user) {
        Service service = Service.builder()
            .basePrice(dto.getPrice())
            .commission(dto.getCommission())
            .fullPrice(dto.getPrice() + dto.getCommission())
            .capacity(dto.getCapacity())
            .createdAt(LocalDate.now())
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .serviceTranslations(dto.getServiceTranslationDtoList()
                .stream().map(serviceTranslationDto -> ServiceTranslation.builder()
                    .description(serviceTranslationDto.getDescription())
                    .language(languageRepository.findById(serviceTranslationDto.getLanguageId()).orElseThrow(
                        () -> new LanguageNotFoundException(
                            ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + serviceTranslationDto.getLanguageId())))
                    .name(serviceTranslationDto.getName())
                    .build())
                .collect(Collectors.toList()))
            .build();
        service.getServiceTranslations().forEach(serviceTranslation -> serviceTranslation.setService(service));
        return service;
    }

    @Override
    public List<GetServiceDto> getService() {
        return serviceTranslationRepository.findAll()
            .stream()
            .map(this::getService)
            .collect(Collectors.toList());
    }

    private GetServiceDto getService(ServiceTranslation serviceTranslation) {
        return GetServiceDto.builder()
            .courierId(serviceTranslation.getService().getCourier().getId())
            .description(serviceTranslation.getDescription())
            .price(serviceTranslation.getService().getBasePrice())
            .capacity(serviceTranslation.getService().getCapacity())
            .name(serviceTranslation.getName())
            .commission(serviceTranslation.getService().getCommission())
            .fullPrice(serviceTranslation.getService().getFullPrice())
            .id(serviceTranslation.getService().getId())
            .createdAt(serviceTranslation.getService().getCreatedAt())
            .createdBy(serviceTranslation.getService().getCreatedBy())
            .editedAt(serviceTranslation.getService().getEditedAt())
            .editedBy(serviceTranslation.getService().getEditedBy())
            .languageCode(serviceTranslation.getLanguage().getCode())
            .build();
    }

    @Override
    public void deleteService(long id) {
        Service service = serviceRepository.findById(id).orElseThrow(
            () -> new ServiceNotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        serviceRepository.delete(service);
    }

    @Override
    public GetServiceDto editService(long id, EditServiceDto dto, String uuid) {
        Service service = serviceRepository.findServiceById(id).orElseThrow(
            () -> new ServiceNotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        User user = userRepository.findByUuid(uuid);
        final Language language = languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE + dto.getLanguageCode()));
        service.setCapacity(dto.getCapacity());
        service.setCommission(dto.getCommission());
        service.setBasePrice(dto.getPrice());
        service.setEditedAt(LocalDate.now());
        service.setEditedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        ServiceTranslation serviceTranslation = serviceTranslationRepository
            .findServiceTranslationsByServiceAndLanguageCode(service, dto.getLanguageCode());
        serviceTranslation.setService(service);
        serviceTranslation.setName(dto.getName());
        serviceTranslation.setDescription(dto.getDescription());
        serviceTranslation.setLanguage(language);
        service.setFullPrice(dto.getPrice() + dto.getCommission());
        service.setBasePrice(dto.getPrice());
        serviceRepository.save(service);
        return getService(serviceTranslation);
    }

    @Override
    public List<LocationInfoDto> getAllLocation() {
        return regionRepository.findAll().stream()
            .map(i -> modelMapper.map(i, LocationInfoDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void addLocation(List<LocationCreateDto> dtoList) {
        dtoList.forEach(locationCreateDto -> {
            checkIfLocationAlreadyCreated(locationCreateDto.getAddLocationDtoList());

            Location location = createNewLocation(locationCreateDto);
            location.getLocationTranslations()
                .forEach(locationTranslation -> locationTranslation.setLocation(location));

            locationRepository.save(location);
            locationTranslationRepository.saveAll(location.getLocationTranslations());
        });
    }

    private Location createNewLocation(LocationCreateDto dto) {
        return Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .coordinates(Coordinates.builder().latitude(dto.getLatitude()).longitude(dto.getLongitude()).build())
            .locationTranslations(dto.getAddLocationDtoList()
                .stream()
                .map(this::addTranslationToLocation)
                .collect(Collectors.toList()))
            .region(checkIfRegionAlreadyCreated(dto))
            .build();
    }

    private LocationTranslation addTranslationToLocation(AddLocationTranslationDto locationTranslationDto) {
        return LocationTranslation.builder()
            .locationName(locationTranslationDto.getLocationName())
            .language(getLanguageByCode(locationTranslationDto.getLanguageCode()))
            .build();
    }

    private void checkIfLocationAlreadyCreated(List<AddLocationTranslationDto> dto) {
        dto.forEach(locationTranslationDto -> {
            Location location =
                locationRepository.findLocationByName(locationTranslationDto.getLocationName()).orElse(null);
            if (location != null) {
                throw new LocationAlreadyCreatedException("The location with name: "
                    + locationTranslationDto.getLocationName() + ErrorMessage.LOCATION_ALREADY_EXIST);
            }
        });
    }

    private Region checkIfRegionAlreadyCreated(LocationCreateDto dto) {
        Region region = new Region();

        for (RegionTranslationDto regionTranslationDto : dto.getRegionTranslationDtos()) {
            region = regionRepository.findRegionByName(regionTranslationDto.getRegionName()).orElse(null);
        }

        if (null == region) {
            region = createRegionWithTranslation(dto);

            regionRepository.save(region);
            regionTranslationRepository.saveAll(region.getRegionTranslations());
        }
        return region;
    }

    @Override
    public void deactivateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.DEACTIVATED.equals(location.getLocationStatus())) {
            throw new LocationStatusAlreadyExistException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.DEACTIVATED);
        locationRepository.save(location);
    }

    @Override
    public void activateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.ACTIVE.equals(location.getLocationStatus())) {
            throw new LocationStatusAlreadyExistException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.ACTIVE);
        locationRepository.save(location);
    }

    @Override
    public CreateCourierDto createCourier(CreateCourierDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);

        checkIfCourierAlreadyExists(courierRepository.findAll(), dto);

        Courier courier = createCourierWithTranslation(dto);
        courier.setCreatedBy(user);
        courier.setCourierStatus(CourierStatus.ACTIVE);
        courier.setCreateDate(LocalDate.now());

        courierRepository.save(courier);
        courierTranslationRepository.saveAll(courier.getCourierTranslationList());
        return modelMapper.map(courier, CreateCourierDto.class);
    }

    private Courier createCourierWithTranslation(CreateCourierDto dto) {
        Courier courier = Courier.builder()
            .courierTranslationList(
                List.of(
                    CourierTranslation.builder().name(dto.getNameUa())
                        .language(languageRepository.findLanguageByCode("ua")).build(),
                    CourierTranslation.builder().name(dto.getNameEn())
                        .language(languageRepository.findLanguageByCode("en")).build()))
            .build();
        courier.getCourierTranslationList().forEach(courierTranslation -> courierTranslation.setCourier(courier));
        return courier;
    }

    private void checkIfCourierAlreadyExists(List<Courier> couriers, CreateCourierDto createCourierDto) {
        couriers.stream()
            .forEach(courier -> courier.getCourierTranslationList().stream().forEach(courierTranslation -> {
                if (courierTranslation.getName().equals(createCourierDto.getNameEn())
                    || courierTranslation.getName().equals(createCourierDto.getNameUa())) {
                    throw new CourierAlreadyExists(ErrorMessage.COURIER_ALREADY_EXISTS);
                }
            }));
    }

    @Override
    public CourierDto updateCourier(CourierDto dto) {
        Courier courier = courierRepository.findById(dto.getCourierId())
            .orElseThrow(() -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID));
        courier.setCourierTranslationList(dto.getCourierTranslationDtos().stream()
            .map(courierTranslationDto -> modelMapper.map(courierTranslationDto, CourierTranslation.class))
            .collect(Collectors.toList()));
        courier.setCourierStatus(CourierStatus.valueOf(dto.getCourierStatus()));
        courierRepository.save(courier);
        courierTranslationRepository.saveAll(courier.getCourierTranslationList());
        return modelMapper.map(courier, CourierDto.class);
    }

    @Override
    public List<CourierDto> getAllCouriers() {
        return courierRepository.findAll().stream().map(courier -> modelMapper.map(courier, CourierDto.class))
            .collect(Collectors.toList());
    }

    private GetCourierTranslationsDto getAllCouriers(CourierTranslation courierTranslation) {
        return GetCourierTranslationsDto.builder()
            .id(courierTranslation.getCourier().getId())
            .languageCode(courierTranslation.getLanguage().getCode())
            .name(courierTranslation.getName())
            .build();
    }

    @Override
    public List<GetCourierLocationDto> getAllCouriersAndLocations() {
        return courierLocationRepository.findAllInfoAboutCourier()
            .stream()
            .map(courierLocation -> modelMapper.map(courierLocation, GetCourierLocationDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void setCourierLimitBySumOfOrder(Long id, EditPriceOfOrder dto) {
        CourierLocation courierLocation =
            courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(id, dto.getLocationId());
        courierLocation.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        courierLocation.setMinPriceOfOrder(dto.getMinPriceOfOrder());
        courierLocation.setMaxPriceOfOrder(dto.getMaxPriceOfOrder());
        courierLocationRepository.save(courierLocation);
    }

    @Override
    public void setCourierLimitByAmountOfBag(Long id, EditAmountOfBagDto dto) {
        CourierLocation courierLocation =
            courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(id, dto.getLocationId());
        courierLocation.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        courierLocation.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBags());
        courierLocation.setMinAmountOfBigBags(dto.getMinAmountOfBigBags());
        courierLocationRepository.save(courierLocation);
    }

    @Override
    public GetCourierTranslationsDto setLimitDescription(Long courierId, String limitDescription) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
        CourierTranslation courierTranslation =
            courierTranslationRepository.findCourierTranslationByCourier(courier);
        courierTranslationRepository.save(courierTranslation);
        return getAllCouriers(courierTranslation);
    }

    @Override
    public GetTariffServiceDto includeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new BagNotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (bag.getMinAmountOfBags().equals(MinAmountOfBag.INCLUDE)) {
            throw new BagWithThisStatusAlreadySetException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.INCLUDE);
        bagRepository.save(bag);
        BagTranslation bagTranslation = translationRepository.findBagTranslationByBag(bag);
        return getTariffService(bagTranslation);
    }

    @Override
    public GetTariffServiceDto excludeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (MinAmountOfBag.EXCLUDE.equals(bag.getMinAmountOfBags())) {
            throw new BagWithThisStatusAlreadySetException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagRepository.save(bag);
        BagTranslation bagTranslation = translationRepository.findBagTranslationByBag(bag);
        return getTariffService(bagTranslation);
    }

    @Override
    public EditTariffInfoDto editInfoInTariff(EditTariffInfoDto dto) {
        Bag bag = bagRepository.findById(dto.getBagId()).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.BAG_NOT_FOUND));
        bag.setMinAmountOfBags(dto.getMinimalAmountOfBagStatus());
        CourierLocation courierLocation = courierLocationRepository
            .findCourierLocationsLimitsByCourierIdAndLocationId(dto.getCourierId(), dto.getLocationId());
        final CourierTranslation courierTranslation =
            courierTranslationRepository.findCourierTranslationByCourier(courierLocation.getCourier());
        courierLocation.setCourierLimit(dto.getCourierLimitsBy());
        courierLocation.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBag());
        courierLocation.setMinAmountOfBigBags(dto.getMinAmountOfBigBag());
        courierLocation.setMinPriceOfOrder(dto.getMinAmountOfOrder());
        courierLocation.setMaxPriceOfOrder(dto.getMaxAmountOfOrder());
        bagRepository.save(bag);
        courierTranslationRepository.save(courierTranslation);
        courierLocationRepository.save(courierLocation);
        return dto;
    }

    @Override
    public void deleteCourier(Long id) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        courier.setCourierStatus(CourierStatus.DELETED);
        courierRepository.save(courier);
    }

    private Integer getFullPrice(Integer price, Integer commission) {
        return price + commission;
    }

    @Override
    public void addLocationToCourier(NewLocationForCourierDto dto) {
        CourierLocation courierLocation = modelMapper.map(dto, CourierLocation.class);
        courierLocation.setCourier(tryToFindCourierById(dto.getCourierId()));
        courierLocation.setLocation(tryToFindLocationById(dto.getLocationId()));
        courierLocationRepository.save(courierLocation);
    }

    @Override
    public List<GetTariffsInfoDto> getAllTariffsInfo() {
        return tariffsInfoRepository.findAll()
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffsInfoDto.class))
            .collect(Collectors.toList());
    }

    private Region createRegionWithTranslation(LocationCreateDto dto) {
        Region region = createNewRegion(dto);
        region.getRegionTranslations().forEach(regionTranslation -> regionTranslation.setRegion(region));
        return region;
    }

    private Region createNewRegion(LocationCreateDto dto) {
        return Region.builder().regionTranslations(
            dto.getRegionTranslationDtos().stream()
                .map(regionTranslationDto -> RegionTranslation.builder()
                    .language(getLanguageByCode(regionTranslationDto.getLanguageCode()))
                    .name(regionTranslationDto.getRegionName())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    private Language getLanguageByCode(String languageCode) {
        return languageRepository.findLanguageByLanguageCode(languageCode).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE + languageCode));
    }

    private Courier tryToFindCourierById(Long id) {
        return courierRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
    }

    private Location tryToFindLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
    }

    @Override
    public ReceivingStationDto createReceivingStation(AddingReceivingStationDto dto, String uuid) {
        if (!receivingStationRepository.existsReceivingStationByName(dto.getName())) {
            User user = userRepository.findByUuid(uuid);
            ReceivingStation receivingStation = receivingStationRepository.save(buildReceivingStation(dto, user));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new ReceivingStationValidationException(
            ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS + dto.getName());
    }

    private ReceivingStation buildReceivingStation(AddingReceivingStationDto dto, User user) {
        return ReceivingStation.builder()
            .name(dto.getName())
            .createdBy(user)
            .createDate(LocalDate.now())
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
            .orElseThrow(() -> new ReceivingStationNotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + dto.getId()));
        receivingStation.setName(dto.getName());
        receivingStationRepository.save(receivingStation);
        return modelMapper.map(receivingStation, ReceivingStationDto.class);
    }

    @Override
    public void deleteReceivingStation(Long id) {
        ReceivingStation station = receivingStationRepository.findById(id)
            .orElseThrow(() -> new ReceivingStationNotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + id));
        if (station.getEmployees() == null || station.getEmployees().isEmpty()) {
            receivingStationRepository.delete(station);
        } else {
            throw new EmployeeIllegalOperationException(ErrorMessage.EMPLOYEES_ASSIGNED_STATION);
        }
    }
}
