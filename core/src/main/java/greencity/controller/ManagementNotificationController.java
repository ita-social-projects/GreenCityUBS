package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import greencity.service.notification.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notification")
@RequiredArgsConstructor
public class ManagementNotificationController {
    private final NotificationTemplateService notificationTemplateService;

    /**
     * Controller that returns all notification templates.
     *
     * @author Dima Sannytski.
     */
    @Operation(summary = "Get all notification templates")
    @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    @GetMapping("/get-all-templates")
    @ApiPageable
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_MESSAGES_PAGE', authentication)")
    public ResponseEntity<PageableDto<NotificationTemplateDto>> getAll(@Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationTemplateService.findAll(pageable));
    }

    /**
     * Controller that updates notification template.
     *
     * @author Dima Sannytski.
     */
    @Operation(description = "Update notification template")
    @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    @PutMapping("/update-template/{id}")
    public ResponseEntity<HttpStatuses> updateNotificationTemplate(
        @PathVariable(name = "id") Long id,
        @RequestBody @Valid NotificationTemplateWithPlatformsUpdateDto notificationTemplateDto) {
        notificationTemplateService.update(id, notificationTemplateDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller that returns notification template by id.
     *
     * @author Dima Sannytski.
     */
    @Operation(summary = "Get notification template by id")
    @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    @GetMapping("/get-template/{id}")
    public ResponseEntity<NotificationTemplateWithPlatformsDto> getNotificationTemplate(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationTemplateService.findById(id));
    }

    /**
     * Controller that change status for notification template and all platforms by
     * id.
     *
     * @author Safarov Renat.
     */
    @Operation(summary = "Change notification template status by id")
    @ApiResponse(responseCode = "200", description = HttpStatuses.OK)
    @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    @PutMapping("/change-template-status/{id}")
    public ResponseEntity<HttpStatus> deactivateNotificationTemplate(
        @PathVariable Long id, @RequestParam String status) {
        notificationTemplateService.changeNotificationStatusById(id, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
