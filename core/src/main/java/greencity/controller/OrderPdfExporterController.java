package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.service.ubs.PdfExporterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Locale;

@RestController
@RequestMapping("ubs/order/pdf/export")
@RequiredArgsConstructor
public class OrderPdfExporterController {
    private final PdfExporterService pdfExporterService;

    @Operation(summary = "Returns PDF file with requested order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> exportOrderPdf(@Parameter(hidden = true) @CurrentUserUuid String userUuid,
        @Parameter @RequestParam("id") Long orderId,
        @Parameter @RequestParam(name = "lang") @ValidLanguage Locale locale) {
        Resource resource = pdfExporterService.generatePdfFileByObjectId(orderId, locale, userUuid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_details_%d_%s.pdf"
            .formatted(orderId, locale.getLanguage()));
        return ResponseEntity.ok()
            .headers(headers)
            .body(resource);
    }
}