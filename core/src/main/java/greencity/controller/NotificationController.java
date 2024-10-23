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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    @Operation(summary = "Return body of the notification and set status - is read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/{id}")
    @ApiLocale
    public ResponseEntity<NotificationDto> getNotification(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @PathVariable Long id, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getNotification(userUuid, id, locale.getLanguage()));
    }

    /**
     * Controller return page with notifications for current user.
     *
     * @return Page with notifications.
     * @author Ihor Volianskyi
     */
    @Operation(summary = "Get page with notifications for current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content)
    })
    @GetMapping
    @ApiPageableWithLocale
    public ResponseEntity<PageableDto<NotificationShortDto>> getNotificationsForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Parameter(hidden = true) @ValidLanguage Locale locale, @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getAllNotificationsForUser(userUuid, locale.getLanguage(), pageable));
    }

    /**
     * Controller return quantity of unread notifications for current user.
     *
     * @return quantity of unread notifications.
     * @author Igor Boykov
     */
    @Operation(summary = "Get all unread notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
    })
    @GetMapping(value = "quantityUnreadenNotifications")
    public ResponseEntity<Long> getAllUnreadenNotificationsForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationService.getUnreadenNotifications(userUuid));
    }

    /**
     * Method to mark Notification as viewed.
     *
     * @param notificationId id of notification, that should be marked as viewed
     */
    @Operation(summary = "Get single Notification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/{notificationId}/viewNotification")
    public ResponseEntity<Object> viewNotification(@PathVariable Long notificationId,
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        notificationService.viewNotification(notificationId, userUuid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method to mark specific Notification as not viewed.
     *
     * @param notificationId id of notification, that should be marked as not viewed
     */
    @Operation(summary = "Unread single Notification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/{notificationId}/unreadNotification")
    public ResponseEntity<Object> unreadNotification(@PathVariable Long notificationId,
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        notificationService.unreadNotification(notificationId, userUuid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method to delete specific Notification.
     *
     * @param userUuid       User
     * @param notificationId id of notification, that should be deleted
     */
    @Operation(summary = "Delete single Notification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long notificationId,
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        notificationService.deleteNotification(notificationId, userUuid);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
