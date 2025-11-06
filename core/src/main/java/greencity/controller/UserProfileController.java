package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.user.UserDeletionReasonDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.enums.UserStatus;
import greencity.service.ubs.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/ubs/userProfile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserService userService;

    /**
     * Controller returns user`s data or update {@link UserProfileDto} date.
     *
     * @param userUuid             {@link UserProfileDto} id.
     * @param userProfileUpdateDto {@link UserProfileDto}
     * @return {@link UserProfileDto}.
     * @author Mykhaolo Berezhinskiy
     */
    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserProfileUpdateDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/user/update")
    public ResponseEntity<UserProfileUpdateDto> updateUserData(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Valid @RequestBody UserProfileUpdateDto userProfileUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(userService.updateProfileData(userUuid, userProfileUpdateDto));
    }

    /**
     * Controller returns user's profile ..
     *
     * @author Liubomyr Bratakh
     */
    @Operation(summary = "Get user's profile data.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/user/getUserProfile")
    public ResponseEntity<UserProfileDto> getUserData(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getProfileData(userUuid));
    }

    /**
     * Controller creates ubs user profile.
     *
     * @param userProfileCreateDto {@link UserProfileCreateDto}.
     * @return id of ubs profile {@link Long}.
     * @author Maksym Golik.
     */
    @Operation(summary = "Create user profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = Long.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PostMapping("/user/create")
    public ResponseEntity<Long> createUserProfile(
        @Valid @RequestBody UserProfileCreateDto userProfileCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUserProfile(userProfileCreateDto));
    }

    /**
     * Change user status.
     *
     * @param uuid   - current user uuid
     * @param userId - target user id
     * @param status - user status
     */
    @Operation(summary = "Change user status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
    })
    @PutMapping("/status/{userId}")
    public ResponseEntity<Void> changeUserStatus(
        @Parameter(hidden = true) @CurrentUserUuid String uuid,
        @PathVariable Long userId,
        @RequestParam UserStatus status) {
        userService.updateUserStatusById(uuid, userId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for getting a {@link List} of {@link String} - reasons for
     * deactivation of the current user.
     *
     * @param id   {@link Long} - user's id.
     * @param uuid {@link String} - user's uuid.
     * @return {@link List} of {@link String} - reasons for deactivation of the
     *         current user.
     */
    @Operation(summary = "Get list reasons of deactivating the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping("/reasons")
    public ResponseEntity<List<String>> getReasonsOfDeactivation(
        @RequestParam("id") Long id, @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.ok().body(userService.getDeactivationReasons(id, uuid));
    }

    @Operation(summary = "Get user status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping("/user/status")
    public ResponseEntity<UserStatus> getUserStatus(@RequestParam String uuid) {
        return ResponseEntity.ok(userService.getUserStatusByUuid(uuid));
    }

    /**
     * Method for deleting current authenticated user. Deleted user is still existed
     * in system but with DELETED status and can be restored.
     *
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Delete current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/user/delete")
    public ResponseEntity<Object> deleteUser(@Parameter(hidden = true) @CurrentUserUuid String uuid,
        @RequestBody @Valid UserDeletionReasonDto reason) {
        userService.deleteUserByUuid(uuid, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * Counts all users by user {@link UserStatus} ACTIVATED.
     *
     * @return amount of users with {@link UserStatus} ACTIVATED.
     */
    @Operation(summary = "Get all activated users amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
    })
    @GetMapping("/activatedUsersAmount")
    public ResponseEntity<Long> getActivatedUsersAmount() {
        return ResponseEntity.ok().body(userService.getActivatedUsersAmount());
    }
}
