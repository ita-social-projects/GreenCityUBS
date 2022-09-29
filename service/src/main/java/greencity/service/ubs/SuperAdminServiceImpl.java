package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.LocationsDtos;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.*;
import greencity.entity.coords.Coordinates;
import greencity.enums.CourierLimit;
import greencity.enums.CourierStatus;
import greencity.enums.LocationStatus;
import greencity.enums.MinAmountOfBag;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.employee.ReceivingStation;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Data
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final BagTranslationRepository translationRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceTranslationRepository serviceTranslationRepository;
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final CourierTranslationRepository courierTranslationRepository;
    private final RegionRepository regionRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;
    private final TariffLocationRepository tariffsLocationRepository;

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
            () -> new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
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
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        bagRepository.delete(bag);
    }

    @Override
    public GetTariffServiceDto editTariffService(EditTariffServiceDto dto, Integer id, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Bag bag = bagRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND));
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
        Long id = dto.getCourierId();
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));

        Service service = Service.builder()
            .basePrice(dto.getPrice())
            .commission(dto.getCommission())
            .fullPrice(dto.getPrice() + dto.getCommission())
            .capacity(dto.getCapacity())
            .createdAt(LocalDate.now())
            .courier(courier)
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .serviceTranslations(dto.getServiceTranslationDtoList()
                .stream().map(serviceTranslationDto -> ServiceTranslation.builder()
                    .description(serviceTranslationDto.getDescription())
                    .name(serviceTranslationDto.getName())
                    .nameEng(serviceTranslationDto.getNameEng())
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
            .nameEng(serviceTranslation.getNameEng())
            .commission(serviceTranslation.getService().getCommission())
            .fullPrice(serviceTranslation.getService().getFullPrice())
            .id(serviceTranslation.getService().getId())
            .createdAt(serviceTranslation.getService().getCreatedAt())
            .createdBy(serviceTranslation.getService().getCreatedBy())
            .editedAt(serviceTranslation.getService().getEditedAt())
            .editedBy(serviceTranslation.getService().getEditedBy())
            .build();
    }

    @Override
    public void deleteService(long id) {
        Service service = serviceRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        serviceRepository.delete(service);
    }

    @Override
    public GetServiceDto editService(long id, EditServiceDto dto, String uuid) {
        Service service = serviceRepository.findServiceById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        User user = userRepository.findByUuid(uuid);
        service.setCapacity(dto.getCapacity());
        service.setCommission(dto.getCommission());
        service.setBasePrice(dto.getPrice());
        service.setEditedAt(LocalDate.now());
        service.setEditedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        ServiceTranslation serviceTranslation = serviceTranslationRepository
            .findServiceTranslationsByService(service);
        serviceTranslation.setService(service);
        serviceTranslation.setName(dto.getName());
        serviceTranslation.setNameEng(dto.getNameEng());
        serviceTranslation.setDescription(dto.getDescription());
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

        Region region = regionRepository.findRegionByName(enName, ukName).orElse(null);

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
                    CourierTranslation.builder().name(dto.getNameUa()).nameEng(dto.getNameEn()).build()))
            .build();
        courier.getCourierTranslationList().forEach(courierTranslation -> courierTranslation.setCourier(courier));
        return courier;
    }

    private void checkIfCourierAlreadyExists(List<Courier> couriers, CreateCourierDto createCourierDto) {
        couriers
            .forEach(courier -> courier.getCourierTranslationList().forEach(courierTranslation -> {
                if (courierTranslation.getNameEng().equals(createCourierDto.getNameEn())
                    || courierTranslation.getName().equals(createCourierDto.getNameUa())) {
                    throw new CourierAlreadyExists(ErrorMessage.COURIER_ALREADY_EXISTS);
                }
            }));
    }

    @Override
    public CourierDto updateCourier(CourierUpdateDto dto) {
        Courier courier = courierRepository.findById(dto.getCourierId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID));
        List<CourierTranslation> listToUpdate = courier.getCourierTranslationList();
        List<CourierTranslationDto> updatedList = dto.getCourierTranslationDtos();
        for (CourierTranslation originalCourierTranslation : listToUpdate) {
            originalCourierTranslation.setName(updatedList.get(0).getName());
            originalCourierTranslation.setNameEng(updatedList.get(0).getNameEng());
        }
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
            .name(courierTranslation.getName())
            .nameEng(courierTranslation.getNameEng())
            .build();
    }

    @Override
    public GetCourierTranslationsDto setLimitDescription(Long courierId, String limitDescription) {
        Courier courier = courierRepository.findById(courierId).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
        CourierTranslation courierTranslation =
            courierTranslationRepository.findCourierTranslationByCourier(courier);
        courierTranslationRepository.save(courierTranslation);
        return getAllCouriers(courierTranslation);
    }

    @Override
    public GetTariffServiceDto includeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (bag.getMinAmountOfBags().equals(MinAmountOfBag.INCLUDE)) {
            throw new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.INCLUDE);
        bagRepository.save(bag);
        BagTranslation bagTranslation = translationRepository.findBagTranslationByBag(bag);
        return getTariffService(bagTranslation);
    }

    @Override
    public GetTariffServiceDto excludeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (MinAmountOfBag.EXCLUDE.equals(bag.getMinAmountOfBags())) {
            throw new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagRepository.save(bag);
        BagTranslation bagTranslation = translationRepository.findBagTranslationByBag(bag);
        return getTariffService(bagTranslation);
    }

    @Override
    public void deleteCourier(Long id) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        courier.setCourierStatus(CourierStatus.DELETED);
        courierRepository.save(courier);
    }

    private Integer getFullPrice(Integer price, Integer commission) {
        return price + commission;
    }

    @Override
    public List<GetTariffsInfoDto> getAllTariffsInfo(TariffsInfoFilterCriteria filterCriteria) {
        List<TariffsInfo> tariffs = tariffsInfoRepository.findAll(new TariffsInfoSpecification(filterCriteria));
        List<GetTariffsInfoDto> dtos = tariffs
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffsInfoDto.class))
            .sorted(Comparator.comparing(tariff -> tariff.getRegionDto().getNameUk()))
            .sorted(Comparator.comparing(tariff -> tariff.getTariffStatus().getPriority()))
            .collect(Collectors.toList());
        dtos.forEach(tariff -> tariff.setLocationInfoDtos(tariff.getLocationInfoDtos().stream()
            .sorted(Comparator.comparing(LocationsDtos::getNameUk))
            .collect(Collectors.toList())));
        return dtos;
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
            () -> new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
    }

    @Override
    public ReceivingStationDto createReceivingStation(AddingReceivingStationDto dto, String uuid) {
        if (!receivingStationRepository.existsReceivingStationByName(dto.getName())) {
            User user = userRepository.findByUuid(uuid);
            ReceivingStation receivingStation = receivingStationRepository.save(buildReceivingStation(dto, user));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new UnprocessableEntityException(
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
            throw new EntityNotFoundException("List of locations can not be empty");
        }
        return locationSet;
    }

    private Set<ReceivingStation> findReceivingStationsForTariff(List<Long> receivingStationIdList) {
        Set<ReceivingStation> receivingStations = new HashSet<>(receivingStationRepository
            .findAllById(receivingStationIdList.stream().distinct().collect(Collectors.toList())));
        if (receivingStations.isEmpty()) {
            throw new EntityNotFoundException("List of receiving stations can not be empty");
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
        return new AddNewTariffResponseDto(tariffForLocationAndCourierAlreadyExistIdList, idListToCheck);
    }

    private TariffsInfo createTariff(AddNewTariffDto addNewTariffDto, String userUUID, Courier courier) {
        TariffsInfo tariffsInfo = TariffsInfo.builder()
            .createdAt(LocalDate.now())
            .courier(courier)
            .receivingStationList(findReceivingStationsForTariff(addNewTariffDto.getReceivingStationsIdList()))
            .locationStatus(LocationStatus.NEW)
            .creator(userRepository.findByUuid(userUUID))
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .build();
        return tariffsInfoRepository.save(tariffsInfo);
    }

    private List<Long> verifyIfTariffExists(List<Long> locationIds, Long courierId) {
        var tariffLocationListList = tariffsLocationRepository
            .findAllByCourierIdAndLocationIds(courierId, locationIds);
        List<Long> alreadyExistsTariff = tariffLocationListList.stream()
            .map(tariffLocation -> tariffLocation.getLocation().getId())
            .collect(Collectors.toList());
        locationIds.removeAll(alreadyExistsTariff);
        return alreadyExistsTariff;
    }

    private Courier tryToFindCourier(Long courierId) {
        return courierRepository.findById(courierId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
    }

    @Override
    public boolean checkIfTariffExists(AddNewTariffDto addNewTariffDto) {
        List<TariffLocation> tariffLocations = tariffsLocationRepository.findAllByCourierIdAndLocationIds(
            addNewTariffDto.getCourierId(), addNewTariffDto.getLocationIdList());

        return (!CollectionUtils.isEmpty(tariffLocations));
    }

    @Override
    public void setTariffLimitByAmountOfBags(Long tariffId, EditAmountOfBagDto dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        tariffsInfo.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBags());
        tariffsInfo.setMinAmountOfBigBags(dto.getMinAmountOfBigBags());
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public void setTariffLimitBySumOfOrder(Long tariffId, EditPriceOfOrder dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMaxPriceOfOrder(dto.getMaxPriceOfOrder());
        tariffsInfo.setMinPriceOfOrder(dto.getMinPriceOfOrder());
        tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public String deactivateTariffCard(Long tariffId) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        if (tariffsInfo.getOrders().isEmpty()) {
            tariffsInfoRepository.delete(tariffsInfo);
            return "Deleted";
        } else {
            tariffsInfo.setLocationStatus(LocationStatus.DEACTIVATED);
            tariffsInfoRepository.save(tariffsInfo);
            return "Deactivated";
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
}
