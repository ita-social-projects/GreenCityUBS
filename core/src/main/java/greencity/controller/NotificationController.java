package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageableWithLocale;
import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.NotificationDto;
import greencity.dto.notification.NotificationShortDto;
import greencity.dto.pageble.PageableDto;
import greencity.service.ubs.NotificationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Locale;

@RestController
@RequestMapping("/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

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
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
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
     * Controller return quantity of unread notifications for current user.
     *
     * @return quantity of unread notifications.
     * @author Igor Boykov
     */
    @ApiOperation(value = "Get all unread notifications")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
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
}
