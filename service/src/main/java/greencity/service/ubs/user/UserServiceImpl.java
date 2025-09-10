package greencity.service.ubs.user;

import static greencity.constant.AppConstant.UBS_EMPLOYEE_WITH_PREFIX;
import static greencity.constant.AppConstant.USER_WITH_PREFIX;
import static greencity.constant.ErrorMessage.CANNOT_ACCESS_PERSONAL_INFO;
import static greencity.constant.ErrorMessage.EMPLOYEE_DOESNT_EXIST;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS;
import static greencity.constant.ErrorMessage.USER_WITH_CURRENT_UUID_DOES_NOT_EXIST;
import static java.util.Objects.nonNull;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.address.AddressDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.BotType;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.repository.AddressRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.util.Bot;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UBSUserRepository ubsUserRepository;
    private final ModelMapper modelMapper;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepo;
    private final UserRemoteClient userRemoteClient;
    private final EventService eventService;
    private final TelegramChatRepository telegramBotRepository;
    private final AddressService addressService;

    @Value("${greencity.bots.ubs-bot-name}")
    private String telegramBotName;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public UserInfoDto getUserAndUserUbsAndViolationsInfoByOrderId(Long orderId, String uuid) {
        UBSuser ubsUser = ubsUserRepository.findUbsUserByOrderId(orderId).orElseThrow(
            () -> new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId));
        User user = ubsUser.getUser();
        if (!Objects.equals(user.getUuid(), uuid)) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
        return UserInfoDto.builder()
            .customerName(ubsUser.getFirstName())
            .customerSurname(ubsUser.getLastName())
            .customerPhoneNumber(ubsUser.getPhoneNumber())
            .customerEmail(ubsUser.getEmail())
            .totalUserViolations(user.getViolations())
            .customerId(user.getId())
            .senderName(ubsUser.getSenderFirstName() == null ? ubsUser.getFirstName() : ubsUser.getSenderFirstName())
            .senderSurname(ubsUser.getSenderLastName() == null ? ubsUser.getLastName() : ubsUser.getSenderLastName())
            .senderEmail(ubsUser.getSenderEmail() == null ? ubsUser.getEmail() : ubsUser.getSenderEmail())
            .senderPhoneNumber(
                ubsUser.getSenderPhoneNumber() == null ? ubsUser.getPhoneNumber() : ubsUser.getSenderPhoneNumber())
            .userViolationForCurrentOrder(userRepository.checkIfUserHasViolationForCurrentOrder(user.getId(), orderId))
            .build();
    }

    @Override
    public UbsCustomersDto updateUbsUserInfoInOrder(UbsCustomersDtoUpdate dtoUpdate, String userUuid) {
        UBSuser ubsUser = getUbsUserById(dtoUpdate.getCustomerId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        checkUserHasAccessToUpdateData(ubsUser, userUuid, authentication);

        ubsUserRepository.save(updateRecipientDataInOrder(ubsUser, dtoUpdate));
        if (!isAdmin(authentication)) {
            eventService.save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.CLIENT_UK,
                ubsUser.getOrders().getFirst());
        } else {
            eventService.save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.UBS_ADMIN,
                ubsUser.getOrders().getFirst());
        }

        return UbsCustomersDto.builder()
            .name(ubsUser.getSenderFirstName() + " " + ubsUser.getSenderLastName())
            .email(ubsUser.getSenderEmail())
            .phoneNumber(ubsUser.getSenderPhoneNumber())
            .build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(UBS_EMPLOYEE_WITH_PREFIX));
    }

    private UBSuser getUbsUserById(Long recipientId) {
        return ubsUserRepository.findById(recipientId)
            .orElseThrow(() -> new UBSuserNotFoundException(RECIPIENT_WITH_CURRENT_ID_DOES_NOT_EXIST + recipientId));
    }

    private void checkUserHasAccessToUpdateData(UBSuser ubsUser, String userUuid, Authentication authentication) {
        String uuid = ubsUser.getUser().getUuid();
        if (checkUserRoleIsUser(authentication) && !(uuid.equals(userUuid))) {
            throw new AccessDeniedException(CANNOT_ACCESS_PERSONAL_INFO);
        }
    }

    private boolean checkUserRoleIsUser(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(USER_WITH_PREFIX));
    }

    @Override
    public Long createUserProfile(UserProfileCreateDto userProfileCreateDto) {
        if (!userRemoteClient.checkIfUserExistsByUuid(userProfileCreateDto.getUuid())) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }
        User user = userRepository.findByUuid(userProfileCreateDto.getUuid());
        if (user != null) {
            throw new BadRequestException(USER_WITH_CURRENT_UUID_ALREADY_EXISTS_IN_UBS);
        }
        user = userRepository.save(User.builder()
            .uuid(userProfileCreateDto.getUuid())
            .recipientEmail(userProfileCreateDto.getEmail())
            .recipientName(userProfileCreateDto.getName())
            .currentPoints(0)
            .violations(0)
            .dateOfRegistration(LocalDate.now()).build());
        return user.getId();
    }

    private UBSuser updateRecipientDataInOrder(UBSuser ubsUser, UbsCustomersDtoUpdate dto) {
        if (nonNull(dto.getCustomerEmail())) {
            ubsUser.setSenderEmail(dto.getCustomerEmail());
        }
        if (nonNull(dto.getCustomerName())) {
            ubsUser.setSenderFirstName(dto.getCustomerName());
        }
        if (nonNull(dto.getCustomerSurname())) {
            ubsUser.setSenderLastName(dto.getCustomerSurname());
        }
        if (nonNull(dto.getCustomerPhoneNumber())) {
            ubsUser.setSenderPhoneNumber(dto.getCustomerPhoneNumber());
        }

        return ubsUser;
    }

    @Override
    @Transactional
    public UserProfileUpdateDto updateProfileData(String uuid, UserProfileUpdateDto userProfileUpdateDto) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        setUserData(user, userProfileUpdateDto);
        setTelegramBot(user, userProfileUpdateDto.getTelegramIsNotify());
        userProfileUpdateDto.getAddressDto().stream()
            .map(a -> modelMapper.map(a, OrderAddressDtoRequest.class))
            .forEach(addressRequestDto -> addressService.updateCurrentAddressForOrder(addressRequestDto, uuid));
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserProfileUpdateDto.class);
    }

    @Override
    public UserProfileDto getProfileData(String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST));
        List<Address> allAddress = addressRepo.findAllNonDeletedAddressesByUserId(user.getId());
        UserProfileDto userProfileDto = modelMapper.map(user, UserProfileDto.class);
        List<Bot> botList = getListOfBots(user.getUuid());
        List<AddressDto> addressDto =
            allAddress.stream()
                .map(a -> modelMapper.map(a, AddressDto.class))
                .toList();
        userProfileDto.setAddressDto(addressDto);
        userProfileDto.setBotList(botList);
        userProfileDto.setHasPassword(userRemoteClient.getPasswordStatus().isHasPassword());
        return userProfileDto;
    }

    private void setUserData(User user, UserProfileUpdateDto userProfileUpdateDto) {
        user.setRecipientName(userProfileUpdateDto.getRecipientName());
        user.setRecipientSurname(userProfileUpdateDto.getRecipientSurname());
        user.setAlternateEmail(userProfileUpdateDto.getAlternateEmail());
        String phone = userProfileUpdateDto.getRecipientPhone();
        user.setRecipientPhone(
            (phone == null || phone.trim().isEmpty()) ? null : UAPhoneNumberUtil.getE164PhoneNumberFormat(phone));
    }

    private void setTelegramBot(User user, Boolean telegramIsNotify) {
        TelegramChat telegramBot = telegramBotRepository.findByUser(user).orElse(null);
        if (telegramBot != null) {
            telegramBot.setIsNotify(telegramIsNotify);
            user.setTelegramBot(telegramBot);
        }
    }

    @Override
    public void markUserAsDeactivated(String uuid, DeactivateUserRequestDto request) {
        User currentUser = userRepository.findByUuid(uuid);
        if (currentUser == null) {
            throw new NotFoundException(USER_WITH_CURRENT_UUID_DOES_NOT_EXIST);
        }
        userRemoteClient.markUserDeactivated(currentUser.getUuid(), request);
    }

    @Override
    public UserPointDto getUserPoint(String uuid) {
        User user = userRepository.findByUuid(uuid);
        int currentUserPoints = user.getCurrentPoints();

        return new UserPointDto(currentUserPoints);
    }

    private List<Bot> getListOfBots(String uuid) {
        return EnumSet.allOf(BotType.class)
            .stream()
            .map(type -> new Bot(type.name(), createLink(type, uuid)))
            .toList();
    }

    private String createLink(BotType type, String uuid) {
        String linkTemplate = null;
        if ("TELEGRAM".equals(type.name())) {
            linkTemplate = String.format("%s%s%s%s",
                AppConstant.TELEGRAM_PART_1_OF_LINK, telegramBotName, AppConstant.TELEGRAM_PART_3_OF_LINK, uuid);
        }
        return linkTemplate;
    }

    @Override
    @Cacheable(value = "positionsAndAuthorities", key = "#email")
    public PositionAuthoritiesDto getPositionsAndRelatedAuthorities(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST + email));
        return userRemoteClient.getPositionsAndRelatedAuthorities(employee.getEmail());
    }

    @Override
    public Set<String> getAllAuthorities(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_DOESNT_EXIST + email));
        return userRemoteClient.getAllAuthorities(employee.getEmail());
    }

    @Override
    public void updateEmployeesAuthorities(UserEmployeeAuthorityDto dto) {
        userRemoteClient.updateEmployeesAuthorities(dto);
    }
}
