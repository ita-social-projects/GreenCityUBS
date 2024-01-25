package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.ubs.UBSClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/ubs")
@Validated
@RequiredArgsConstructor
public class AddressController {
    private final UBSClientService ubsClientService;

    /**
     * Controller for getting all addresses for current order.
     *
     * @param userUuid {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Get all addresses for order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/findAll-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> getAllAddressesForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.findAllAddressesForCurrentOrder(userUuid));
    }

    /**
     * Controller save address for current order.
     *
     * @param dtoRequest {@link CreateAddressRequestDto}.
     * @param uuid       {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Save order address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> saveAddressForOrder(
        @Valid @RequestBody CreateAddressRequestDto dtoRequest,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ubsClientService.saveCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller update address for current order.
     *
     * @param dtoRequest {@link OrderAddressDtoRequest}.
     * @param uuid       {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Update order address(if placeId is null updates only addressComment)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/update-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> updateAddressForOrder(
        @Valid @RequestBody OrderAddressDtoRequest dtoRequest,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.updateCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller delete order address.
     *
     * @param id   {@link Long}.
     * @param uuid {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Delete order address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/order-addresses/{id}")
    public ResponseEntity<OrderWithAddressesResponseDto> deleteOrderAddress(
        @Valid @PathVariable("id") Long id,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.deleteCurrentAddressForOrder(id, uuid));
    }

    /**
     * Controller make address actual (default).
     *
     * @param addressId {@link Long}.
     * @param uuid      {@link UserVO} id.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Make address actual (default)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/makeAddressActual/{addressId}")
    public ResponseEntity<AddressDto> makeAddressActual(
        @PathVariable Long addressId,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.makeAddressActual(addressId, uuid));
    }

    /**
     * Controller to get all districts for a given region and city.
     *
     * @param region Name of the region.
     * @param city   Name of the city.
     * @return A List of LocationDtos containing a list of all districts for the
     *         specified region and city.
     */
    @Operation(summary = "Get all districts for a given region and city",
        description = "Provide a region and a city to look up for associated districts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistrictDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-districts")
    public ResponseEntity<List<DistrictDto>> getAllDistrictsForRegionAndCity(@RequestParam String region,
        @RequestParam String city) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getAllDistricts(region, city));
    }
}
