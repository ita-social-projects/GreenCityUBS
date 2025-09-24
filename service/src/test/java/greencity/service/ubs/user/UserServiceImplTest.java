package greencity.service.ubs.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock
    private UserDeactivationRepo userDeactivationRepo;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserExternalDto currentUserDto;
    private UserExternalDto targetUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .uuid("test-uuid-123")
            .status(UserStatus.ACTIVATED)
            .recipientEmail("test@example.com")
            .recipientName("Test User")
            .build();

        currentUserDto = UserExternalDto.builder()
            .uuid("current-user-uuid")
            .name("Current User")
            .role(Role.ROLE_ADMIN)
            .build();

        targetUserDto = UserExternalDto.builder()
            .uuid("test-uuid-123")
            .name("Target User")
            .role(Role.ROLE_USER)
            .build();
    }

    @Test
    void getUserStatusByUuid_ShouldReturnUserStatus_WhenUserExists() {
        String uuid = "test-uuid-123";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        UserStatus result = userService.getUserStatusByUuid(uuid);

        assertEquals(UserStatus.ACTIVATED, result);
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void getUserStatusByUuid_ShouldThrowNotFoundException_WhenUserNotExists() {
        String uuid = "non-existent-uuid";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.getUserStatusByUuid(uuid));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid, exception.getMessage());
        verify(userRepository).findUserByUuid(uuid);
    }

    @Test
    void deleteUserByUuid_ShouldSetStatusToDeleted_WhenUserIsActivated() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.ACTIVATED);
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        userService.deleteUserByUuid(uuid);

        assertEquals(UserStatus.DELETED, testUser.getStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUserByUuid_ShouldThrowForbiddenException_WhenUserIsDeactivated() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.DEACTIVATED);
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userService.deleteUserByUuid(uuid));

        assertEquals(ErrorMessage.FORBIDDEN_USER_DELETION, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByUuid_ShouldThrowForbiddenException_WhenUserIsBlocked() {
        String uuid = "test-uuid-123";
        testUser.setStatus(UserStatus.BLOCKED);
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.of(testUser));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userService.deleteUserByUuid(uuid));

        assertEquals(ErrorMessage.FORBIDDEN_USER_DELETION, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByUuid_ShouldThrowNotFoundException_WhenUserNotExists() {
        String uuid = "non-existent-uuid";
        when(userRepository.findUserByUuid(uuid)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.deleteUserByUuid(uuid));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_UUID + uuid, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldDeactivateUser_WhenStatusIsDeactivated() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findByUuid(currentUserUuid)).thenReturn(currentUserDto);
        when(userRemoteClient.findByUuid(testUser.getUuid())).thenReturn(targetUserDto);
        when(userRemoteClient.findUserLanguageByUuid(testUser.getUuid())).thenReturn("en");

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.DEACTIVATED, testUser.getStatus());
        verify(userRepository).save(testUser);

        ArgumentCaptor<UserDeactivationReason> deactivationCaptor =
            ArgumentCaptor.forClass(UserDeactivationReason.class);
        verify(userDeactivationRepo).save(deactivationCaptor.capture());

        UserDeactivationReason savedReason = deactivationCaptor.getValue();
        assertTrue(savedReason.getReason().contains("Current User"));
        assertTrue(savedReason.getReason().contains(currentUserUuid));
        assertEquals(testUser, savedReason.getUser());

        ArgumentCaptor<UserDeactivationReasonDto> notificationCaptor =
            ArgumentCaptor.forClass(UserDeactivationReasonDto.class);
        verify(userRemoteClient).sendReasonOfDeactivation(notificationCaptor.capture());

        UserDeactivationReasonDto notification = notificationCaptor.getValue();
        assertEquals(testUser.getRecipientEmail(), notification.getEmail());
        assertEquals(testUser.getRecipientName(), notification.getName());
        assertEquals("en", notification.getLang());
    }

    @Test
    void updateUserStatusById_ShouldThrowException_WhenAdminTriesToDeactivateAdmin() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        targetUserDto.setRole(Role.ROLE_ADMIN);

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findByUuid(currentUserUuid)).thenReturn(currentUserDto);
        when(userRemoteClient.findByUuid(testUser.getUuid())).thenReturn(targetUserDto);

        UserStatusUpdateException exception = assertThrows(UserStatusUpdateException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.ADMIN_CANNOT_DEACTIVATE_OTHER_ADMIN, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(userDeactivationRepo, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldActivateUser_WhenStatusIsActivated() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.ACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));
        when(userRemoteClient.findUserLanguageByUuid(testUser.getUuid())).thenReturn("uk");

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.ACTIVATED, testUser.getStatus());
        verify(userRepository).save(testUser);

        ArgumentCaptor<UserActivationDto> activationCaptor = ArgumentCaptor.forClass(UserActivationDto.class);
        verify(userRemoteClient).sendMessageOfActivation(activationCaptor.capture());

        UserActivationDto notification = activationCaptor.getValue();
        assertEquals(testUser.getRecipientEmail(), notification.getEmail());
        assertEquals(testUser.getRecipientName(), notification.getName());
        assertEquals("uk", notification.getLang());
    }

    @Test
    void updateUserStatusById_ShouldThrowException_WhenUserTriesToUpdateThemselves() {
        String currentUserUuid = "test-uuid-123";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.DEACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));

        UserStatusUpdateException exception = assertThrows(UserStatusUpdateException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.USER_CANNOT_DEACTIVATE_YOURSELF, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldThrowNotFoundException_WhenTargetUserNotExists() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 999L;
        UserStatus status = UserStatus.ACTIVATED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.updateUserStatusById(currentUserUuid, targetUserId, status));

        assertEquals(ErrorMessage.USER_WITH_CURRENT_ID_DOES_NOT_EXIST + targetUserId, exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatusById_ShouldHandleDefaultStatus_WhenStatusIsOther() {
        String currentUserUuid = "current-user-uuid";
        Long targetUserId = 1L;
        UserStatus status = UserStatus.BLOCKED;

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(testUser));

        userService.updateUserStatusById(currentUserUuid, targetUserId, status);

        assertEquals(UserStatus.BLOCKED, testUser.getStatus());
        verify(userRepository).save(testUser);
        verify(userRemoteClient, never()).sendReasonOfDeactivation(any());
        verify(userRemoteClient, never()).sendMessageOfActivation(any());
    }

    @Test
    void getDeactivationReasons_ShouldReturnEnglishReasons_WhenLangIsEn() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {en} / Reason 2 {uk} / Another reason {en}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.singletonList(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.contains("Reason 1"));
        assertTrue(result.contains("Another reason"));
        assertFalse(result.get(0).contains("{en}"));
        assertFalse(result.get(1).contains("{en}"));
    }

    @Test
    void getDeactivationReasons_ShouldReturnUkrainianReasons_WhenLangIsUk() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {en} / Reason 2 {uk} / Another reason {uk}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.singletonList(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("uk");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.contains("Reason 2"));
        assertTrue(result.contains("Another reason"));
        assertFalse(result.get(0).contains("{uk}"));
        assertFalse(result.get(1).contains("{uk}"));
    }

    @Test
    void getDeactivationReasons_ShouldThrowNotFoundException_WhenNoReasonsFound() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.emptyList());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userService.getDeactivationReasons(userId, currentUserUuid));

        assertEquals(ErrorMessage.USER_DEACTIVATION_REASON_IS_EMPTY, exception.getMessage());
    }

    @Test
    void getDeactivationReasons_ShouldHandleUkLanguageCorrectly() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {uk}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.singletonList(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("uk");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(1, result.size());
        assertEquals("Reason 1", result.getFirst());
    }

    @Test
    void getActivatedUsersAmount_ShouldReturnCorrectCount() {
        long expectedCount = 42L;
        when(userRepository.countAllByStatus(UserStatus.ACTIVATED)).thenReturn(expectedCount);

        long result = userService.getActivatedUsersAmount();

        assertEquals(expectedCount, result);
        verify(userRepository).countAllByStatus(UserStatus.ACTIVATED);
    }

    @Test
    void getActivatedUsersAmount_ShouldReturnZero_WhenNoActivatedUsers() {
        when(userRepository.countAllByStatus(UserStatus.ACTIVATED)).thenReturn(0L);

        long result = userService.getActivatedUsersAmount();

        assertEquals(0L, result);
        verify(userRepository).countAllByStatus(UserStatus.ACTIVATED);
    }

    @Test
    void getDeactivationReasons_ShouldReturnEmptyList_WhenNoMatchingLanguageReasons() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Reason 1 {fr} / Reason 2 {de}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.singletonList(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getDeactivationReasons_ShouldHandleSingleReason_WithoutSlash() {
        Long userId = 1L;
        String currentUserUuid = "current-user-uuid";
        String reasonsText = "Single reason {en}";
        UserDeactivationReason deactivationReason = UserDeactivationReason.builder()
            .reason(reasonsText)
            .dateTimeOfDeactivation(LocalDateTime.now())
            .build();

        when(userDeactivationRepo.getLastDeactivationReasons(userId))
            .thenReturn(Collections.singletonList(deactivationReason));
        when(userRemoteClient.findUserLanguageByUuid(currentUserUuid)).thenReturn("en");

        List<String> result = userService.getDeactivationReasons(userId, currentUserUuid);

        assertEquals(1, result.size());
        assertEquals("Single reason", result.getFirst());
    }
}