package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.service.FileExporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("ubs/export")
@AllArgsConstructor
@Slf4j
public class OrderPdfExporterController {
    FileExporterService fileExporterService;


    @Operation(summary = "Returns PDF file with requested order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> exportOrderPdf(@Parameter(hidden = true) @CurrentUserUuid String userUuid,
                                                   @Parameter @RequestParam Long orderId,
                                                   @Parameter @RequestParam @ValidLanguage Locale locale) {
        Resource resource = fileExporterService.export(orderId, locale, userUuid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_details_%d_%s_%s.pdf"
                .formatted(orderId, UUID.randomUUID(), locale.getLanguage()));
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart / form-data");
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
