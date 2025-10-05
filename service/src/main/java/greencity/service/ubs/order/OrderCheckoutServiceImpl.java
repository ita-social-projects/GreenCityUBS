package greencity.service.ubs.order;

import static greencity.constant.ErrorMessage.LOCATION_DOESNT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.LOCATION_IS_DEACTIVATED_FOR_TARIFF;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST;
import static greencity.constant.ErrorMessage.TARIFF_NOT_FOUND;
import static greencity.constant.ErrorMessage.TARIFF_OR_LOCATION_IS_DEACTIVATED;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.bag.BagTranslationDto;
import greencity.dto.user.PersonalDataDto;
import greencity.dto.user.UserPointsAndAllBagsDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.LocationStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.repository.BagRepository;
import greencity.repository.LocationRepository;
import greencity.repository.OrderBagRepository;
import greencity.repository.OrderRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCheckoutServiceImpl implements OrderCheckoutService {
    private final TariffsInfoRepository tariffsInfoRepository;
    private final TariffLocationRepository tariffLocationRepository;
    private final LocationRepository locationRepository;
    private final BagRepository bagRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderBagRepository orderBagRepository;
    private final UBSUserRepository ubsUserRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserPointsAndAllBagsDto getFirstPageDataByTariffAndLocationId(Long tariffId, Long locationId) {
        TariffsInfo tariffsInfo = tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(TARIFF_NOT_FOUND + tariffId));

        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new NotFoundException(LOCATION_DOESNT_FOUND_BY_ID + locationId));

        checkIfTariffIsAvailableForCurrentLocation(tariffsInfo, location);

        return getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(tariffsInfo.getId(), 0);
    }

    @Override
    public UserPointsAndAllBagsDto getFirstPageDataByOrderId(String uuid, Long orderId) {
        User user = userRepository.findUserByUuid(uuid).orElseThrow(
            () -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        Order order = orderRepository.findById(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));

        checkIsOrderOfCurrentUser(user, order);

        TariffsInfo tariffsInfo = order.getTariffsInfo();

        Location location = getLocationByOrderIdThroughLazyInitialization(order);

        checkIfTariffIsAvailableForCurrentLocation(tariffsInfo, location);

        return getUserPointsAndAllBagsDtoByTariffIdAndOrderIdAndUserPoints(tariffsInfo.getId(), user.getCurrentPoints(),
            orderId);
    }

    @Override
    public PersonalDataDto getSecondPageData(String uuid) {
        User currentUser = userRepository.findByUuid(uuid);
        List<UBSuser> ubsUser = ubsUserRepository.findUBSuserByUser(currentUser);

        if (ubsUser.isEmpty()) {
            ubsUser = Collections.singletonList(UBSuser.builder().id(null).build());
        }
        PersonalDataDto dto = modelMapper.map(currentUser, PersonalDataDto.class);
        dto.setUbsUserId(ubsUser.getFirst().getId());
        if (currentUser.getAlternateEmail() != null
            && !currentUser.getAlternateEmail().isEmpty()) {
            dto.setEmail(currentUser.getAlternateEmail());
        }
        return dto;
    }

    private void checkIfTariffIsAvailableForCurrentLocation(TariffsInfo tariffsInfo, Location location) {
        if (tariffsInfo.getTariffStatus() == TariffStatus.DEACTIVATED
            || location.getLocationStatus() == LocationStatus.DEACTIVATED) {
            throw new BadRequestException(TARIFF_OR_LOCATION_IS_DEACTIVATED);
        } else {
            boolean isAvailable = isTariffAvailableForCurrentLocation(tariffsInfo, location);
            if (!isAvailable) {
                throw new BadRequestException(LOCATION_IS_DEACTIVATED_FOR_TARIFF + tariffsInfo.getId());
            }
        }
    }

    private boolean isTariffAvailableForCurrentLocation(TariffsInfo tariffsInfo, Location location) {
        return tariffLocationRepository
            .findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location)
            .orElseThrow(() -> new NotFoundException(TARIFF_FOR_LOCATION_NOT_EXIST + location.getId()))
            .getLocationStatus() != LocationStatus.DEACTIVATED;
    }

    private UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoByTariffIdAndUserPoints(Long tariffId,
        Integer userPoints) {
        List<BagTranslationDto> bagTranslationDtoList =
            bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
                .map(bag -> modelMapper.map(bag, BagTranslationDto.class))
                .sorted(Comparator.comparing(BagTranslationDto::getCapacity).reversed())
                .toList();
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private void checkIsOrderOfCurrentUser(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorMessage.ORDER_DOES_NOT_BELONG_TO_USER);
        }
    }

    private Location getLocationByOrderIdThroughLazyInitialization(Order order) {
        return order
            .getUbsUser()
            .getOrderAddress()
            .getLocation();
    }

    private UserPointsAndAllBagsDto getUserPointsAndAllBagsDtoByTariffIdAndOrderIdAndUserPoints(Long tariffId,
        Integer userPoints,
        Long orderId) {
        List<BagTranslationDto> bagTranslationDtoList =
            bagRepository.findAllActiveBagsByTariffsInfoId(tariffId).stream()
                .map(bag -> buildBagTranslationDto(orderId, bag))
                .toList();
        return new UserPointsAndAllBagsDto(bagTranslationDtoList, userPoints);
    }

    private BagTranslationDto buildBagTranslationDto(Long orderId, Bag source) {
        return BagTranslationDto.builder()
            .id(source.getId())
            .capacity(source.getCapacity())
            .price(BigDecimal.valueOf(source.getFullPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .nameUk(source.getNameUk())
            .nameEn(source.getNameEn())
            .limitedIncluded(source.getLimitIncluded())
            .quantity(getQuantityOfBagsByBagIdAndOrderId(orderId, source.getId()))
            .build();
    }

    private Integer getQuantityOfBagsByBagIdAndOrderId(Long orderId, Integer bagId) {
        return orderBagRepository.getAmountOfOrderBagsByOrderIdAndBagId(orderId, bagId)
            .orElse(0);
    }
}
