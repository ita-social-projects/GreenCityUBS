package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.CourierStatus;
import greencity.entity.enums.LocationStatus;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.User;
import greencity.exceptions.*;
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
                    .language(languageRepository.findById(tariffTranslationDto.getLanguageId()).orElseThrow(
                        () -> new LanguageNotFoundException(
                            ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + tariffTranslationDto.getLanguageId())))
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
            .languageCode(bagTranslation.getLanguage().getCode())
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
            translationRepository.findBagTranslationByBagAndLanguageCode(bag, dto.getLangCode());
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
        Courier courier = courierRepository.findById(dto.getCourierId()).orElseThrow(() -> new CourierNotFoundException(
            ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + dto.getCourierId()));
        Service service = Service.builder()
            .basePrice(dto.getPrice())
            .commission(dto.getCommission())
            .fullPrice(dto.getPrice() + dto.getCommission())
            .capacity(dto.getCapacity())
            .createdAt(LocalDate.now())
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .courier(courier)
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
            .courierId(serviceTranslation.getService().getCourier().getId())
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
    public List<GetLocationTranslationDto> getAllLocation() {
        return locationTranslationRepository.findAll()
            .stream()
            .map(this::getAllLocation)
            .collect(Collectors.toList());
    }

    private GetLocationTranslationDto getAllLocation(LocationTranslation locationTranslation) {
        return GetLocationTranslationDto.builder()
            .locationStatus(locationTranslation.getLocation().getLocationStatus().toString())
            .languageCode(locationTranslation.getLanguage().getCode())
            .region(locationTranslation.getRegion())
            .name(locationTranslation.getLocationName())
            .id(locationTranslation.getLocation().getId())
            .build();
    }

    @Override
    public LocationCreateDto addLocation(LocationCreateDto dto) {
        Location location = createLocationWithTranslation(dto);
        locationRepository.save(location);
        locationTranslationRepository.saveAll(location.getLocationTranslations());
        return modelMapper.map(location, LocationCreateDto.class);
    }

    private Location createLocationWithTranslation(LocationCreateDto dto) {
        Location location = Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .locationTranslations(dto.getAddLocationDtoList()
                .stream()
                .map(locationTranslationDto -> LocationTranslation.builder()
                    .locationName(locationTranslationDto.getLocationName())
                    .region(locationTranslationDto.getRegion())
                    .language(languageRepository.findById(locationTranslationDto.getLanguageId()).orElseThrow(
                        () -> new LanguageNotFoundException(
                            ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + locationTranslationDto.getLanguageId())))
                    .build())
                .collect(Collectors.toList()))
            .build();
        location.getLocationTranslations().forEach(locationTranslation -> locationTranslation.setLocation(location));
        return location;
    }

    @Override
    public GetLocationTranslationDto deactivateLocation(Long id, String code) {
        Location location = locationRepository.findById(id).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        final LocationTranslation locationTranslation =
            locationTranslationRepository.findLocationTranslationByLocationAndLanguageCode(location, code)
                .orElseThrow(() -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        if (location.getLocationStatus().equals(LocationStatus.DEACTIVATED)) {
            throw new LocationStatusAlreadyExistException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.DEACTIVATED);
        locationRepository.save(location);
        return getAllLocation(locationTranslation);
    }

    @Override
    public GetLocationTranslationDto activateLocation(Long id, String code) {
        Location location = locationRepository.findById(id).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        final LocationTranslation locationTranslation =
            locationTranslationRepository.findLocationTranslationByLocationAndLanguageCode(location, code)
                .orElseThrow(() -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        if (location.getLocationStatus().equals(LocationStatus.ACTIVE)) {
            throw new LocationStatusAlreadyExistException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.ACTIVE);
        locationRepository.save(location);
        return getAllLocation(locationTranslation);
    }

    @Override
    public CreateCourierDto createCourier(CreateCourierDto dto) {
        Courier courier = createCourierWithTranslation(dto);
        courier.setCourierStatus(CourierStatus.ACTIVE);
        courierRepository.save(courier);
        courierTranslationRepository.saveAll(courier.getCourierTranslationList());
        courierLocationRepository.saveAll(courier.getCourierLocations());
        return modelMapper.map(courier, CreateCourierDto.class);
    }

    private Courier createCourierWithTranslation(CreateCourierDto dto) {
        Courier courier = Courier.builder()
            .courierTranslationList(dto.getCreateCourierTranslationDtos().stream()
                .map(courierTranslationDtos -> CourierTranslation.builder()
                    .language(languageRepository.findById(courierTranslationDtos.getLanguageId()).orElseThrow(
                        () -> new LanguageNotFoundException(
                            ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + courierTranslationDtos.getLanguageId())))
                    .name(courierTranslationDtos.getName())
                    .limitDescription(courierTranslationDtos.getLimitDescription())
                    .build())
                .collect(Collectors.toList()))
            .courierLocations(dto.getCreateCourierLimitsDto().stream().map(i -> CourierLocation.builder()
                .courierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG)
                .maxAmountOfBigBags(i.getMaxAmountOfBigBags())
                .minAmountOfBigBags(i.getMinAmountOfBigBags())
                .maxPriceOfOrder(i.getMaxPriceOfOrder())
                .minPriceOfOrder(i.getMinPriceOfOrder())
                .location(locationRepository.findById(i.getLocationId())
                    .orElseThrow(() -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND)))
                .build()).collect(Collectors.toList()))
            .build();
        courier.getCourierTranslationList().forEach(courierTranslation -> courierTranslation.setCourier(courier));
        courier.getCourierLocations().forEach(courierLocations -> courierLocations.setCourier(courier));
        return courier;
    }

    @Override
    public List<GetCourierLocationDto> getAllCouriers() {
        return courierLocationRepository.findAllInfoAboutCourier()
            .stream()
            .map(i -> modelMapper.map(i, GetCourierLocationDto.class))
            .collect(Collectors.toList());
    }

    private GetCourierTranslationsDto getAllCouriers(CourierTranslation courierTranslation) {
        return GetCourierTranslationsDto.builder()
            .id(courierTranslation.getCourier().getId())
            .languageCode(courierTranslation.getLanguage().getCode())
            .name(courierTranslation.getName())
            .limitDescription(courierTranslation.getLimitDescription())
            .build();
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
    public GetCourierTranslationsDto setLimitDescription(Long courierId, String limitDescription, Long languageId) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
        CourierTranslation courierTranslation =
            courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courier, languageId);
        courierTranslation.setLimitDescription(limitDescription);
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
            courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courierLocation.getCourier(),
                dto.getLanguageId());
        final Language language = languageRepository.findById(dto.getLanguageId()).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + dto.getLanguageId()));
        courierLocation.setCourierLimit(dto.getCourierLimitsBy());
        courierLocation.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBag());
        courierLocation.setMinAmountOfBigBags(dto.getMinAmountOfBigBag());
        courierLocation.setMinPriceOfOrder(dto.getMinAmountOfOrder());
        courierLocation.setMaxPriceOfOrder(dto.getMaxAmountOfOrder());
        courierTranslation.setLanguage(language);
        courierTranslation.setLimitDescription(dto.getLimitDescription());
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
}