package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.constants.HttpStatuses;
import greencity.service.UBS_service;
import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ubs")
public class OrderController {
    private final UBS_service ubs_service;

    @Autowired
    public OrderController(UBS_service ubs_service) {
        this.ubs_service = ubs_service;
    }

    @ApiOperation(value = "Get current user points and all list of bags")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserPointsAndAllBagsDto.class),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/first")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPoints(
            @ApiIgnore @CurrentUserId Long userId) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ubs_service.getFirstPageData(userId));
    }

    @ApiOperation(value = "Check if certificate is active")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = CertificateDto.class),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/first/certificate/{code}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
            @PathVariable String code
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ubs_service.checkCertificate(code));
    }

    @ApiOperation(value = "Get user's order data")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = HttpStatuses.OK, response = List.class),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
            @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/second")
    public ResponseEntity<List<PersonalDataDto>> getUBSusers(
            @ApiIgnore @CurrentUserId Long userId
    ) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ubs_service.getSecondPageData(userId));
    }

    @ApiOperation(value = "Process user order")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = UserPointsAndAllBagsDto.class),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
            @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PostMapping("/processOrder")
    public ResponseEntity<Object> processOrder(
            @ApiIgnore @CurrentUserId Long userId,
            @Valid @RequestBody OrderResponseDto dto
    ) {

        ubs_service.processOrder(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
    }

}
