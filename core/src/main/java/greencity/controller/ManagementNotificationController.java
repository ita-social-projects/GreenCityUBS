package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.dto.notification.NotificationTemplateDto;
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
    @GetMapping("/get-all")
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
    @PutMapping("/update")
    public ResponseEntity<HttpStatuses> updateNotificationTemplate(
        @RequestBody @Valid NotificationTemplateDto notificationTemplateDto) {
        notificationTemplateService.update(notificationTemplateDto);
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
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDto> getNotificationTemplate(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(notificationTemplateService.findById(id));
    }
}
