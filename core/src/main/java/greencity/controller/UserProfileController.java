package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
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
import org.springframework.web.bind.annotation.GetMapping;
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
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
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/user/markUserAsDeactivated")
    public ResponseEntity<HttpStatus> deactivateUser(
        @RequestParam Long id) {
        ubsClientService.markUserAsDeactivated(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
