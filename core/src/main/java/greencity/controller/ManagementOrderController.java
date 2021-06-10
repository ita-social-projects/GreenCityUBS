package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.ApiPageable;
import greencity.annotations.ValidLanguage;
import greencity.constants.HttpStatuses;
import greencity.dto.*;
import greencity.service.ubs.UBSManagementService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/ubs/management")
public class ManagementOrderController {
    private final UBSManagementService ubsManagementService;
    private final ModelMapper mapper;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public ManagementOrderController(UBSManagementService ubsManagementService, ModelMapper mapper) {
        this.ubsManagementService = ubsManagementService;
        this.mapper = mapper;
    }

    /**
     * Controller getting all certificates with sorting possibility.
     *
     * @return list of all certificates.
     * @author Nazar Struk
     */
    @ApiOperation(value = "Get all certificates")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/getAllCertificates")
    @ApiPageable
    public ResponseEntity<PageableDto<CertificateDtoForSearching>> allCertificates(
        @ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllCertificates(pageable));
    }

    /**
     * Controller getting all certificates with sorting possibility.
     *
     * @return httpStatus.
     * @author Nazar Struk
     */

    @ApiOperation("Add Certificate")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/addCertificate")
    public ResponseEntity<HttpStatus> addCertificate(
        @Valid @RequestBody CertificateDtoForAdding certificateDtoForAdding) {
        ubsManagementService.addCertificate(certificateDtoForAdding);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * Controller finds undelivered orders.
     *
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get all undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/all-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> allUndeliveredCoords() {
        return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllUndeliveredOrdersWithLiters());
    }

    /**
     * Controller groups undelivered orders.
     *
     * @param radius {@link Double} preferred searching radius.
     * @return list of {@link CoordinatesDto}.
     * @author Oleh Bilonizhka
     */
    @ApiOperation(value = "Get grouped undelivered orders.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/group-undelivered")
    public ResponseEntity<List<GroupedOrderDto>> groupCoords(@RequestParam Double radius,
        @RequestParam(required = false, defaultValue = "3000") Integer litres) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoords(radius, litres));
    }

    /**
     * Controller groups orders along with specified.
     */
    @ApiOperation(value = "Get grouped orders along with specified.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = GroupedOrderDto[].class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/group-undelivered-with-specified")
    public ResponseEntity<List<GroupedOrderDto>> groupCoordsWithSpecifiedOnes(
        @Valid @RequestBody Set<CoordinatesDto> specified,
        @RequestParam(required = false, defaultValue = "3000") Integer litres,
        @RequestParam(required = false, defaultValue = "0") Double additionalDistance) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getClusteredCoordsAlongWithSpecified(specified, litres, additionalDistance));
    }

    /**
     * Controller adding points to user by Email.
     *
     * @return httpStatus.
     * @author Nazar Struk
     */
    @ApiOperation("Add Points to User")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PatchMapping(value = "/addPointsToUser")
    public ResponseEntity<HttpStatus> addPointsToUser(
        @Valid @RequestBody AddingPointsToUserDto addingPointsToUserDto) {
        ubsManagementService.addPointsToUser(addingPointsToUserDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller for getting User violations.
     *
     * @return {@link ViolationsInfoDto} count of Users violations with order id
     *         descriptions.
     * @author Nazar Struk
     */
    @ApiOperation("Get User violations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/getUsersViolations")
    public ResponseEntity<ViolationsInfoDto> getUserViolations(@Valid @Email @RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAllUserViolations(email));
    }

    /**
     * Controller for adding User violation.
     *
     * @return {@link AddingViolationsToUserDto} count of Users violations with
     *         order id descriptions.
     * @author Nazar Struk
     */
    @ApiOperation("Add Violation to User")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ApiLocale
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/addViolationToUser")
    public ResponseEntity<HttpStatus> addUsersViolation(@Valid @RequestBody AddingViolationsToUserDto add,
        @ApiIgnore @ValidLanguage Locale locale) {
        ubsManagementService.addUserViolation(add);
        ubsManagementService.sendNotificationAboutViolation(add, locale.getLanguage());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Controller for getting User violations.
     *
     * @author Nazar Struk
     */
    @ApiOperation("Get all info from Table orders")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/getAllFieldsFromOrderTable")
    public ResponseEntity<List<AllFieldsFromTableDto>> getAllFieldsFromOrderTable2Info(
        @RequestParam(value = "columnName", required = false) String columnName,
        @RequestParam(value = "sortingType", required = false) String sortingType) {
        if (columnName == null || sortingType == null) {
            return ResponseEntity.status(HttpStatus.OK).body(ubsManagementService.getAllValuesFromTable());
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                .body(ubsManagementService.getAllSortedValuesFromTable(columnName, sortingType));
        }
    }

    /**
     * Controller read address by order id.
     *
     * @param id {@link Long}.
     * @return {@link HttpStatus} - http status.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Get address by order id")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = ReadAddressByOrderDto.class),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/read-address-order/{id}")
    public ResponseEntity<ReadAddressByOrderDto> getAddressByOrderId(
        @Valid @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsManagementService.getAddressByOrderId(id));
    }

    /**
     * Controller for update order address.
     *
     * @return {@link OrderAddressDtoResponse}.
     * @author Orest Mahdziak
     */
    @ApiOperation(value = "Update order address")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = OrderAddressDtoResponse.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PutMapping("/update-address")
    public ResponseEntity<OrderAddressDtoResponse> updateAddressByOrderId(
        @Valid @RequestBody OrderAddressDtoUpdate dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsManagementService.updateAddress(dto));
    }
}
