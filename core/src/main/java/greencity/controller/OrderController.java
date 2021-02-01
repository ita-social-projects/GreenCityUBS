package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.service.UBSService;
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
    private final UBSService ubsService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public OrderController(UBSService ubsService) {
        this.ubsService = ubsService;
    }

    /**
     * Method returns all available bags and bonus points of current
     * {@link greencity.dto.UserVO}.
     *
     * @param userId {@link UserVO} id.
     * @return {@link UserPointsAndAllBagsDto}.
     * @author Oleh Bilonizhka
     */
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
            .body(ubsService.getFirstPageData(userId));
    }

    /**
     * Method returns entered certificate status if not absent.
     *
     * @param code {@link String} code of certificate.
     * @return {@link CertificateDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Check if certificate is active")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = CertificateDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/first/certificate/{code}")
    public ResponseEntity<CertificateDto> checkIfCertificateAvailable(
        @PathVariable String code) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsService.checkCertificate(code));
    }

    /**
     * Method returns list of saved {@link UserVO} data.
     *
     * @param userId {@link UserVO} id.
     * @return list of {@link PersonalDataDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get user's order data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = List.class),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/second")
    public ResponseEntity<List<PersonalDataDto>> getUBSusers(
        @ApiIgnore @CurrentUserId Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsService.getSecondPageData(userId));
    }

    /**
     * Methods saves all entered by user data to database.
     *
     * @param userId {@link UserVO} id.
     * @param dto    {@link OrderResponseDto} order data.
     * @return {@link HttpStatus}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Process user order")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PostMapping("/processOrder")
    public ResponseEntity<HttpStatus> processOrder(
        @ApiIgnore @CurrentUserId Long userId,
        @Valid @RequestBody OrderResponseDto dto) {
        ubsService.saveFullOrderToDB(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
