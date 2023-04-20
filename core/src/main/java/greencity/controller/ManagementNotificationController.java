package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.NotificationTemplateDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsDto;
import greencity.dto.notification.NotificationTemplateWithPlatformsUpdateDto;
import greencity.dto.pageble.PageableDto;
import greencity.service.notification.NotificationTemplateService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

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
    @ApiOperation(value = "Get all notification templates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-templates")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationTemplateDto>> getAll(@ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationTemplateService.findAll(pageable));
    }

    /**
     * Controller that updates notification template.
     *
     * @author Dima Sannytski.
     */
    @ApiOperation("Update notification template")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Get notification template by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
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
    @ApiOperation(value = "Change notification template status by id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/change-template-status/{id}")
    public ResponseEntity<HttpStatus> deactivateNotificationTemplate(
        @PathVariable Long id, @RequestParam String status) {
        notificationTemplateService.changeNotificationStatusById(id, status);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
