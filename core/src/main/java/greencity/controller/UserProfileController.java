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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * Controller returns user`s date of saved or update {@link UserProfileDto}
     * date.
     *
     * @param userUuid       {@link UserProfileDto} id.
     * @param userProfileDto {@link UserProfileDto}
     * @return {@link UserProfileDto}.
     * @author Mykhaolo Berezhinskiy
     */
    @ApiOperation(value = "Create user profile")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = UserProfileDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/user/save")
    public ResponseEntity<UserProfileDto> saveUserDate(@ApiIgnore @CurrentUserUuid String userUuid,
        @Valid @RequestBody UserProfileDto userProfileDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsClientService.saveProfileData(userUuid, userProfileDto));
    }
}
