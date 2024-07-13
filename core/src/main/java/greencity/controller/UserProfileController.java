package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.user.DeactivateUserRequestDto;
import greencity.dto.user.UserProfileCreateDto;
import greencity.dto.user.UserProfileDto;
import greencity.dto.user.UserProfileUpdateDto;
import greencity.service.ubs.UBSClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/ubs/userProfile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UBSClientService ubsClientService;

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
            .body(ubsClientService.updateProfileData(userUuid, userProfileUpdateDto));
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
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getProfileData(userUuid));
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PostMapping("/user/create")
    public ResponseEntity<Long> createUserProfile(
        @Valid @RequestBody UserProfileCreateDto userProfileCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ubsClientService.createUserProfile(userProfileCreateDto));
    }

    /**
     * Controller updates user (mark as DEACTIVATED).
     *
     * @return {@link ResponseEntity} update user.
     * @author Liubomyr Bratakh.
     */
    @Operation(summary = "mark user as DEACTIVATED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
    })
    @PutMapping("/user/markUserAsDeactivated")
    public ResponseEntity<HttpStatus> deactivateUser(
        @Parameter(hidden = true) @CurrentUserUuid String uuid,
        @Valid @RequestBody DeactivateUserRequestDto request) {
        ubsClientService.markUserAsDeactivated(uuid, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
