package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.annotations.ValidAddress;
import greencity.annotations.ValidUpdateAddress;
import greencity.constants.HttpStatuses;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.service.ubs.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.security.Principal;
import jakarta.validation.constraints.Positive;
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
    private final AddressService addressService;

    /**
     * Controller for getting all addresses for current order.
     *
     * @param userUuid - user's uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Get all addresses for order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content)
    })
    @GetMapping("/findAll-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> getAllAddressesForCurrentUser(
        @Parameter(hidden = true) @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(addressService.findAllAddressesForCurrentOrder(userUuid));
    }

    /**
     * Controller save address for current order.
     *
     * @param dtoRequest {@link CreateAddressRequestDto}.
     * @param uuid       - user's uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Save order address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/save-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> saveAddressForOrder(
        @Valid @ValidAddress @RequestBody CreateAddressRequestDto dtoRequest,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(addressService.saveCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller update address for current order.
     *
     * @param dtoRequest {@link OrderAddressDtoRequest}.
     * @param uuid       - user's uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Update order address(if placeId is null updates only addressComment)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/update-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> updateAddressForOrder(
        @Valid @ValidUpdateAddress @RequestBody OrderAddressDtoRequest dtoRequest,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(addressService.updateCurrentAddressForOrder(dtoRequest, uuid));
    }

    /**
     * Controller delete order address.
     *
     * @param id   {@link Long}.
     * @param uuid - user's uuid.
     * @return {@link HttpStatus} - http status.
     */
    @Operation(summary = "Delete order address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = OrderWithAddressesResponseDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/order-addresses/{id}")
    public ResponseEntity<OrderWithAddressesResponseDto> deleteOrderAddress(
        @Positive @PathVariable("id") Long id,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(addressService.deleteCurrentAddressForOrder(id, uuid));
    }

    /**
     * Controller make address actual (default).
     *
     * @param addressId {@link Long}.
     * @param uuid      - user's uuid.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Make address actual (default)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = AddressDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PatchMapping("/makeAddressActual/{addressId}")
    public ResponseEntity<AddressDto> makeAddressActual(
        @Positive @PathVariable Long addressId,
        @Parameter(hidden = true) @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(addressService.makeAddressActual(addressId, uuid));
    }

    /**
     * Controller to get all districts for a given region and city.
     *
     * @param region Name of the region.
     * @param city   Name of the city.
     * @return A List of LocationDTOs containing a list of all districts for the
     *         specified region and city.
     */
    @Operation(summary = "Get all districts for a given region and city",
        description = "Provide a region and a city to look up for associated districts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistrictDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/get-all-districts")
    public ResponseEntity<List<DistrictDto>> getAllDistrictsForRegionAndCity(@RequestParam String region,
        @RequestParam String city) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(addressService.getAllDistricts(region, city));
    }

    /**
     * Returns a list of all districts for Kyiv.
     *
     * @return A list of DistrictDTOs containing all districts for Kyiv.
     */
    @Operation(summary = "Get all districts for Kyiv")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DistrictDto.class)))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/districts-for-kyiv")
    public ResponseEntity<List<DistrictDto>> getAllDistrictsForKyiv() {
        return ResponseEntity.ok(addressService.getAllDistrictsForKyiv());
    }

    /**
     * Update address for current order. This endpoint updates a users address for
     * their current order. The address is updated on the big order table.
     *
     * @param addressDto The updated address information.
     * @param principal  The user principal.
     * @return {@link HttpStatus}
     */
    @Operation(summary = "Update address for current order",
        description = "Update address for current order on big order table")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = OrderAddressDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PatchMapping("/update-address")
    public ResponseEntity<OrderAddressDtoResponse> updateAddress(
        @RequestBody @Valid UpdateAddressDto addressDto, @Parameter(hidden = true) Principal principal) {
        return ResponseEntity.ok(addressService.addressUpdate(addressDto, principal.getName()));
    }

    /**
     * Retrieves the address for an order with the given id.
     *
     * @param orderId The id of the order
     * @return The address for the order
     */
    @Operation(summary = "Get address for order",
        description = "Get address for order for given order id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/get-address-for-order/{orderId}")
    public ResponseEntity<UpdateAddressDto> getAddressForOrder(@Positive @PathVariable Long orderId) {
        return ResponseEntity.ok(addressService.getAddressForOrder(orderId));
    }

    /**
     * Controller read address by order id.
     *
     * @param id {@link Long}.
     * @return {@link HttpStatus} - http status.
     * @author Orest Mahdziak
     */
    @Operation(hidden = true)
    @GetMapping("/read-address-order/{id}")
    public ResponseEntity<ReadAddressByOrderDto> getAddressByOrderId(
        @Positive @PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(addressService.getAddressByOrderId(id));
    }
}
