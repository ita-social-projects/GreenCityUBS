package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.constants.HttpStatuses;
import greencity.constants.ValidationConstant;
import greencity.dto.*;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ubs")
@Validated
public class OrderController {
    private final UBSClientService ubsClientService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public OrderController(UBSClientService ubsClientService) {
        this.ubsClientService = ubsClientService;
    }

    /**
     * Controller returns all available bags and bonus points of current user.
     * {@link greencity.dto.UserVO}.
     *
     * @param userId {@link UserVO} id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get current user points and all bags list.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = UserPointsAndAllBagsDto.class),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/order-details")
    public ResponseEntity<UserPointsAndAllBagsDto> getCurrentUserPoints(
        @ApiIgnore @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getFirstPageData(userId));
    }

    /**
     * Controller returns entered certificate status if not absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Check if certificate is available.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CertificateDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/certificate/{code}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
        @PathVariable @Pattern(regexp = ValidationConstant.SERTIFICATE_CODE_REGEXP,
            message = ValidationConstant.SERTIFICATE_CODE_REGEXP_MESSAGE) String code) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.checkCertificate(code));
    }

    /**
     * Controller returns list of saved {@link UserVO} data.
     *
     * @param userId {@link UserVO} id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get user's personal data.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PersonalDataDto[].class),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/personal-data")
    public ResponseEntity<List<PersonalDataDto>> getUBSusers(
        @ApiIgnore @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getSecondPageData(userId));
    }

    /**
     * Controller saves all entered by user data to database.
     *
     * @param userId {@link UserVO} id.
     * @param dto    {@link OrderResponseDto} order data.
     * @return {@link HttpStatus}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Process user order.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PostMapping("/processOrder")
    public ResponseEntity<HttpStatus> processOrder(
        @ApiIgnore @CurrentUserId Long userId,
        @Valid @RequestBody OrderResponseDto dto) {
        ubsClientService.saveFullOrderToDB(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
