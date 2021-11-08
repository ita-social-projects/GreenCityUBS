package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.UserProfileDto;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@Validated
@RequestMapping("/ubs/userProfile")
public class UserProfileController {
    private final UBSClientService ubsClientService;

    /**
     * Constructor with parameters.
     */
    public UserProfileController(UBSClientService ubsClientService) {
        this.ubsClientService = ubsClientService;
    }

    /**
     * Controller returns user`s data or update {@link UserProfileDto} date.
     * 
     * @param userUuid       {@link UserProfileDto} id.
     * @param userProfileDto {@link UserProfileDto}
     * @return {@link UserProfileDto}.
     * @author Mykhaolo Berezhinskiy
     */
    @ApiOperation(value = "Update user profile")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = UserProfileDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @PutMapping("/user/update")
    public ResponseEntity<UserProfileDto> updateUserData(@ApiIgnore @CurrentUserUuid String userUuid,
        @Valid @RequestBody UserProfileDto userProfileDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsClientService.updateProfileData(userUuid, userProfileDto));
    }

    /**
     * Controller returns user's profile ..
     *
     *
     * @author Liubomyr Bratakh
     */
    @ApiOperation(value = "Get user's profile data.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserProfileDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/user/getUserProfile")
    public ResponseEntity<UserProfileDto> getUserData(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getProfileData(userUuid));
    }

    /**
     * Controller updates user (mark as DEACTIVATED).
     *
     * @return {@link ResponseEntity} update user.
     * @author Liubomyr Bratakh.
     */
    @ApiOperation(value = "mark user as DEACTIVATED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/user/markUserAsDeactivated")
    public ResponseEntity<HttpStatus> deactivateUser(
        @RequestParam Long id) {
        ubsClientService.markUserAsDeactivated(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
