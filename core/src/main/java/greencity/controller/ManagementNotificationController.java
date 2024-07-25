package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.AddNotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import greencity.service.notification.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
    })
    @GetMapping("/get-all-templates")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationTemplateDto>> getAll(@Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationTemplateService.findAll(pageable));
    }

    /**
     * Controller that updates notification template.
     *
     * @author Dima Sannytski.
     */
    @Operation(summary = "Update notification template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/change-template-status/{id}")
    public ResponseEntity<HttpStatus> deactivateNotificationTemplate(
        @PathVariable Long id, @RequestParam String status) {
        notificationTemplateService.changeNotificationStatusById(id, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller that creates notification template with platforms.
     *
     * @author Denys Ryhal.
     */
    @Operation(summary = "Create notification template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
    })
    @PostMapping("/add-template")
    public ResponseEntity<HttpStatus> addNotificationTemplate(
        @RequestBody @Validated AddNotificationTemplateWithPlatformsDto template) {
        notificationTemplateService.createNotificationTemplate(template);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Controller that removes notification template with platforms.
     *
     * @author Denys Ryhal.
     */
    @Operation(summary = "Remove custom notification template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
    })
    @DeleteMapping("/remove-custom-template/{id}")
    public ResponseEntity<HttpStatus> removeNotificationTemplate(@PathVariable Long id) {
        notificationTemplateService.removeNotificationTemplate(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
