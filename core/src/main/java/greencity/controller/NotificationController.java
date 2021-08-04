package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.constants.ValidationConstant;
import greencity.dto.CertificateDto;
import greencity.dto.NotificationDto;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {
    @Autowired
    private final NotificationService notificationService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Controller for getting all notifications for current user.
     *
     * @return @List of all notifications.
     * @author Ann Sakhno
     */
    @ApiOperation(value = "Get all notifications for active user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = NotificationDto[].class),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotificationsForCurrentUser(
            @ApiIgnore @CurrentUserUuid String userUuid,
            @ApiIgnore @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(notificationService.getAllNotificationsForUser(userUuid, locale.getLanguage()));
    }
}