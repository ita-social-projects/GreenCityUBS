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
import greencity.constant.ErrorMessage;
import greencity.constant.OrderHistory;
import greencity.dto.address.AddressDto;
import greencity.dto.customer.UbsCustomersDto;
import greencity.dto.customer.UbsCustomersDtoUpdate;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.user.UserActivationDto;
import greencity.dto.user.UserDeactivationReasonDto;
import greencity.dto.user.UserDeletionReasonDto;
import greencity.dto.user.UserExternalDto;
import greencity.dto.user.UserInfoDto;
import greencity.dto.user.UserPointDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import greencity.entity.user.UserDeactivationReason;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.BotType;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.ForbiddenException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.user.UBSuserNotFoundException;
import greencity.exceptions.user.UserStatusUpdateException;
import greencity.properties.TelegramProperties;
import greencity.repository.AddressRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.TelegramChatRepository;
import greencity.repository.UBSUserRepository;
import greencity.repository.UserDeactivationRepo;
import greencity.repository.UserRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.AddressService;
import greencity.service.ubs.EventService;
import greencity.dto.user.Bot;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    private final UserDeactivationRepo userDeactivationRepo;
    private final EventService eventService;
    private final TelegramChatRepository telegramBotRepository;
    private final AddressService addressService;
    private final TelegramProperties telegramProperties;

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
                ubsUser.getOrders().getFirst().getId());
        } else {
            eventService.save(OrderHistory.CHANGED_SENDER_UK, OrderHistory.UBS_ADMIN,
                ubsUser.getOrders().getFirst().getId());
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
            .status(UserStatus.ACTIVATED)
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
                AppConstant.TELEGRAM_PART_1_OF_LINK, telegramProperties.getTelegramBotName(),
                AppConstant.TELEGRAM_PART_3_OF_LINK, uuid);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UserStatus getUserStatusByUuid(String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid));
        return user.getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @org.springframework.transaction.annotation.Transactional
    @Override
    public void deleteUserByUuid(String uuid, UserDeletionReasonDto reason) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid));

        if (user.getStatus() == UserStatus.DEACTIVATED
            || user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_USER_DELETION);
        }

        String lang = userRemoteClient.findUserLanguageByUuid(user.getUuid());
        saveAndSendDeactivationReason(user, reason.getReason(), lang);

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateUserStatusById(String currentUserUuid, Long targetUserId, UserStatus status) {
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST + targetUserId));
        if (currentUserUuid.equals(targetUser.getUuid())) {
            throw new UserStatusUpdateException(ErrorMessage.USER_CANNOT_DEACTIVATE_YOURSELF);
        }

        String lang = userRemoteClient.findUserLanguageByUuid(targetUser.getUuid());
        switch (status) {
            case DEACTIVATED -> {
                UserExternalDto currentUserDto = userRemoteClient.findByUuid(currentUserUuid);
                UserExternalDto targetUserDto = userRemoteClient.findByUuid(targetUser.getUuid());
                if (targetUserDto.getRole().equals(Role.ROLE_ADMIN)) {
                    throw new UserStatusUpdateException(ErrorMessage.ADMIN_CANNOT_DEACTIVATE_OTHER_ADMIN);
                }

                String reason = String.format("Deactivated by %s[%s] admin.", currentUserDto.getName(),
                    currentUserDto.getUuid());
                saveAndSendDeactivationReason(targetUser, reason, lang);
            }
            case ACTIVATED -> {
                UserActivationDto notification = UserActivationDto.builder()
                    .email(targetUser.getRecipientEmail())
                    .name(targetUser.getRecipientName())
                    .lang(lang)
                    .build();
                userRemoteClient.sendMessageOfActivation(notification);
            }
            default -> {
            }
        }

        targetUser.setStatus(status);
        userRepository.save(targetUser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getDeactivationReasons(Long id, String currentUserUuid) {
        Optional<UserDeactivationReason> userReason = userDeactivationRepo.getLastDeactivationReason(id);
        if (userReason.isEmpty()) {
            throw new NotFoundException(ErrorMessage.USER_DEACTIVATION_REASON_IS_EMPTY);
        }

        String userLang = userRemoteClient.findUserLanguageByUuid(currentUserUuid);
        if (userLang.equals("uk")) {
            userLang = "uk";
        }
        return filterReasons(userLang, userReason.get().getReason());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getActivatedUsersAmount() {
        return userRepository.countAllByStatus(UserStatus.ACTIVATED);
    }

    private void saveAndSendDeactivationReason(User user, String reason, String lang) {
        userDeactivationRepo.save(UserDeactivationReason.builder()
            .dateTimeOfDeactivation(LocalDateTime.now())
            .reason(reason)
            .user(user)
            .build());

        userRemoteClient.sendReasonOfDeactivation(UserDeactivationReasonDto.builder()
            .reason(reason)
            .email(user.getRecipientEmail())
            .name(user.getRecipientName())
            .lang(lang)
            .build());
    }

    private List<String> filterReasons(String lang, String reasons) {
        List<String> result = null;
        List<String> forAll = List.of(reasons.split("/"));
        if (lang.equals("en")) {
            result = forAll.stream().filter(s -> s.contains("{en}"))
                .map(filterEn -> filterEn.replace("{en}", "").trim()).toList();
        }
        if (lang.equals("uk")) {
            result = forAll.stream().filter(s -> s.contains("{uk}"))
                .map(filterEn -> filterEn.replace("{uk}", "").trim()).toList();
        }
        return result;
    }
}
