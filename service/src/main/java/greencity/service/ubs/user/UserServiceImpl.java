package greencity.service.ubs.user;

import greencity.client.UserRemoteClient;
import greencity.constant.ErrorMessage;
import greencity.dto.user.UserActivationDto;
import greencity.dto.user.UserDeactivationReasonDto;
import greencity.dto.user.UserExternalDto;
import greencity.entity.user.User;
import greencity.entity.user.UserDeactivationReason;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exceptions.ForbiddenException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.user.UserStatusUpdateException;
import greencity.repository.UserDeactivationRepo;
import greencity.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserDeactivationRepo userDeactivationRepo;

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
    @Transactional
    @Override
    public void deleteUserByUuid(String uuid) {
        User user = userRepository.findUserByUuid(uuid)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid));

        if (user.getStatus() == UserStatus.DEACTIVATED
            || user.getStatus() == UserStatus.BLOCKED) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_USER_DELETION);
        }

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateUserStatusById(String currentUserUuid, Long targetUserId, UserStatus status) {
        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST + targetUserId));
        if (currentUserUuid.equals(targetUser.getUuid())) {
            throw new UserStatusUpdateException(ErrorMessage.USER_CANNOT_DEACTIVATE_YOURSELF);
        }

        UserExternalDto currentUserDto = userRemoteClient.findByUuid(currentUserUuid);
        String lang = userRemoteClient.findUserLanguageByUuid(targetUser.getUuid());
        switch (status) {
            case DEACTIVATED -> {
                UserExternalDto targetUserDto = userRemoteClient.findByUuid(targetUser.getUuid());
                if (targetUserDto.getRole().equals(Role.ROLE_ADMIN)) {
                    throw new UserStatusUpdateException(ErrorMessage.ADMIN_CANNOT_DEACTIVATE_OTHER_ADMIN);
                }

                String reason = String.format("Deactivated by %s[%s] admin.", currentUserDto.getName(),
                    currentUserDto.getUuid());
                userDeactivationRepo.save(UserDeactivationReason.builder()
                    .dateTimeOfDeactivation(LocalDateTime.now())
                    .reason(reason)
                    .user(targetUser)
                    .build());

                UserDeactivationReasonDto notification = UserDeactivationReasonDto.builder()
                    .deactivationReason(reason)
                    .email(targetUser.getRecipientEmail())
                    .name(targetUser.getRecipientName())
                    .lang(lang)
                    .build();
                userRemoteClient.sendReasonOfDeactivation(notification);
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
        List<UserDeactivationReason> userReasons = userDeactivationRepo.getLastDeactivationReasons(id);
        if (userReasons.isEmpty()) {
            throw new NotFoundException(ErrorMessage.USER_DEACTIVATION_REASON_IS_EMPTY);
        }

        UserDeactivationReason lastReason = userReasons.getFirst();
        String userLang = userRemoteClient.findUserLanguageByUuid(currentUserUuid);
        if (userLang.equals("uk")) {
            userLang = "uk";
        }
        return filterReasons(userLang, lastReason.getReason());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getActivatedUsersAmount() {
        return userRepository.countAllByStatus(UserStatus.ACTIVATED);
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
