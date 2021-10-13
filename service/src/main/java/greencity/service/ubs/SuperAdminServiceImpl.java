package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
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
    private final ModelMapper modelMapper;

    @Override
    public GetTariffServiceDto addTariffService(AddServiceDto dto, String uuid) {
        final Language language = languageRepository.findById(dto.getLanguageId()).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_ID + dto.getLanguageId()));
        final Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        Bag bag = modelMapper.map(dto, Bag.class);
        final BagTranslation translation = modelMapper.map(dto, BagTranslation.class);
        bag.setFullPrice(dto.getPrice() + dto.getCommission());
        bag.setCreatedAt(LocalDate.now());
        bag.setLocation(location);
        bag.setMinAmountOfBags(MinAmountOfBag.INCLUDE);
        translation.setBag(bag);
        translation.setLanguage(language);
        User user = userRepository.findByUuid(uuid);
        bag.setCreatedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        bagRepository.save(bag);
        translationRepository.save(translation);
        return getTariffService(translation);
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
        bag.setFullPrice(dto.getPrice() + dto.getCommission());
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
    public GetServiceDto addService(CreateServiceDto dto, String uuid) {
        Service service = modelMapper.map(dto, Service.class);
        Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        User user = userRepository.findByUuid(uuid);
        final Language language = languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE + dto.getLanguageCode()));
        service.setCreatedAt(LocalDate.now());
        service.setLocation(location);
        service.setCreatedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        service.setBasePrice(dto.getPrice());
        service.setFullPrice(dto.getPrice() + dto.getCommission());
        ServiceTranslation serviceTranslation = modelMapper.map(dto, ServiceTranslation.class);
        serviceTranslation.setLanguage(language);
        serviceRepository.save(service);
        serviceTranslation.setService(service);
        serviceTranslationRepository.save(serviceTranslation);
        return getService(serviceTranslation);
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
            .locationId(serviceTranslation.getService().getLocation().getId())
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
    public GetServiceDto editService(long id, CreateServiceDto dto, String uuid) {
        Service service = serviceRepository.findById(id).orElseThrow(
            () -> new ServiceNotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        User user = userRepository.findByUuid(uuid);
        final Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
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
        service.setLocation(location);
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
            .name(locationTranslation.getLocationName())
            .id(locationTranslation.getLocation().getId())
            .build();
    }

    @Override
    public GetLocationTranslationDto addLocation(AddLocationDto dto) {
        Location location = modelMapper.map(dto, Location.class);
        Language language = languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()).orElseThrow(
            () -> new LanguageNotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE + dto.getLanguageCode()));
        LocationTranslation locationTranslation = modelMapper.map(dto, LocationTranslation.class);
        locationTranslation.setLocation(location);
        locationTranslation.setLanguage(language);
        location.setLocationStatus(LocationStatus.ACTIVE);
        locationRepository.save(location);
        return getAllLocation(locationTranslation);
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
    public GetCourierTranslationsDto createCourier(CreateCourierDto dto) {
        Courier courier = modelMapper.map(dto, Courier.class);
        Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new LocationNotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        final Language language = languageRepository.findLanguageByLanguageCode(
            dto.getLanguageCode())
            .orElseThrow(() -> new LanguageNotFoundException(
                ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE + dto.getLanguageCode()));
        courier.setLocation(location);
        courier.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = modelMapper.map(dto, CourierTranslation.class);
        courierTranslation.setCourier(courier);
        courierTranslation.setLanguage(language);
        courierTranslation.setLimitDescription(dto.getLimitDescription());
        courierRepository.save(courier);
        courierTranslationRepository.save(courierTranslation);
        return getAllCouriers(courierTranslation);
    }

    @Override
    public List<GetCourierTranslationsDto> getAllCouriers() {
        return courierTranslationRepository.findAll()
            .stream()
            .map(this::getAllCouriers)
            .collect(Collectors.toList());
    }

    private GetCourierTranslationsDto getAllCouriers(CourierTranslation courierTranslation) {
        return GetCourierTranslationsDto.builder().id(courierTranslation.getCourier().getId())
            .languageCode(courierTranslation.getLanguage().getCode())
            .courierLimit(courierTranslation.getCourier().getCourierLimit().toString())
            .locationId(courierTranslation.getCourier().getLocation().getId())
            .maxAmountOfBigBags(courierTranslation.getCourier().getMaxAmountOfBigBags())
            .minAmountOfBigBags(courierTranslation.getCourier().getMinAmountOfBigBags())
            .maxPriceOfOrder(courierTranslation.getCourier().getMaxPriceOfOrder())
            .minPriceOfOrder(courierTranslation.getCourier().getMinPriceOfOrder())
            .name(courierTranslation.getName())
            .limitDescription(courierTranslation.getLimitDescription())
            .build();
    }

    @Override
    public GetCourierTranslationsDto setCourierLimitBySumOfOrder(Long id, EditPriceOfOrder dto) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        courier.setMinPriceOfOrder(dto.getMinPriceOfOrder());
        courier.setMaxPriceOfOrder(dto.getMaxPriceOfOrder());
        courier.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = courierTranslationRepository.findCourierTranslationByCourier(courier);
        courierRepository.save(courier);
        return getAllCouriers(courierTranslation);
    }

    @Override
    public GetCourierTranslationsDto setCourierLimitByAmountOfBag(Long id, EditAmountOfBagDto dto) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        courier.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBags());
        courier.setMinAmountOfBigBags(dto.getMinAmountOfBigBags());
        courier.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        courierRepository.save(courier);
        CourierTranslation courierTranslation = courierTranslationRepository.findCourierTranslationByCourier(courier);
        return getAllCouriers(courierTranslation);
    }

    @Override
    public GetCourierTranslationsDto setLimitDescription(Long id, String limitDescription) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new CourierNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        CourierTranslation courierTranslation = courierTranslationRepository.findCourierTranslationByCourier(courier);
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
        if (bag.getMinAmountOfBags().equals(MinAmountOfBag.EXCLUDE)) {
            throw new BagWithThisStatusAlreadySetException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagRepository.save(bag);
        BagTranslation bagTranslation = translationRepository.findBagTranslationByBag(bag);
        return getTariffService(bagTranslation);
    }
}