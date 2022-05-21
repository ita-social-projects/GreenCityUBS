package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageableWithLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.notification.UpdateNotificationTemplatesDto;
import greencity.service.ubs.NotificationService;
import greencity.service.ubs.NotificationTemplatesService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/notifications")
@Validated
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationTemplatesService notificationTemplatesService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public NotificationController(NotificationService notificationService,
        NotificationTemplatesService notificationTemplatesService) {
        this.notificationService = notificationService;
        this.notificationTemplatesService = notificationTemplatesService;
    }

    /**
     * Controller return body of the notification and set status - is read.
     *
     * @author Ihor Volianskyi
     */
    @ApiOperation("Return body of the notification and set status - is read")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping(value = "/{id}")
    @ApiLocale
    public ResponseEntity<NotificationDto> getNotification(@ApiIgnore @CurrentUserUuid String userUuid,
        @PathVariable Long id, @ApiIgnore @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getNotification(userUuid, id, locale.getLanguage()));
    }

    /**
     * Controller return page with notifications for current user.
     *
     * @return Page with notifications.
     * @author Ihor Volianskyi
     */
    @ApiOperation(value = "Get page with notifications for current user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<NotificationShortDto>> getNotificationsForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid,
        @ApiIgnore @ValidLanguage Locale locale, @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getAllNotificationsForUser(userUuid, locale.getLanguage(), pageable));
    }

    /**
     * Controller return quantity of unreaden notifications for current user.
     *
     * @return quantity of unreaden notifications.
     * @author Igor Boykov
     */
    @ApiOperation(value = "get all unreaden notifications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping(value = "quantityUnreadenNotifications")
    public ResponseEntity<Long> getAllUnreadenNotificationsForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getUnreadenNotifications(userUuid));
    }

    /**
     * Controller updates body in notification templates for receiving type SITE.
     *
     * @author Natalia Kozak
     */

    @ApiOperation(value = "Update body in notification template for SITE notifications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping(value = "/updateTemplateForSITE")
    public ResponseEntity<HttpStatus> updateNotificationTemplateForSITE(
        @Valid @RequestBody UpdateNotificationTemplatesDto updateNotificationTemplatesDto) {
        notificationTemplatesService.updateNotificationTemplateForSITE(updateNotificationTemplatesDto.getBody(),
            updateNotificationTemplatesDto.getNotificationType(), updateNotificationTemplatesDto.getLanguageId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller updates body in notification templates for receiving type OTHER.
     *
     * @author Natalia Kozak
     */

    @ApiOperation(value = "Update body in notification template for OTHER notifications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping(value = "/updateTemplateForOTHER")
    public ResponseEntity<HttpStatus> updateNotificationTemplateForOTHER(
        @Valid @RequestBody UpdateNotificationTemplatesDto updateNotificationTemplatesDto) {
        notificationTemplatesService.updateNotificationTemplateForOTHER(updateNotificationTemplatesDto.getBody(),
            updateNotificationTemplatesDto.getNotificationType(), updateNotificationTemplatesDto.getLanguageId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}