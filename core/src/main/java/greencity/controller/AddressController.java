package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.address.AddressDto;
import greencity.dto.location.api.LocationDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.dto.order.OrderWithAddressesResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
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
    @ApiOperation(value = "Get all addresses for order")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/findAll-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> getAllAddressesForCurrentUser(
        @ApiIgnore @CurrentUserUuid String userUuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.findAllAddressesForCurrentOrder(userUuid));
    }

    /**
     * Controller save address for current order.
     *
     * @param dtoRequest {@link CreateAddressRequestDto}.
     * @param uuid       {@link UserVO} id.
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Save order address")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/save-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> saveAddressForOrder(
        @Valid @RequestBody CreateAddressRequestDto dtoRequest,
        @ApiIgnore @CurrentUserUuid String uuid) {
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
    @ApiOperation(value = "Update order address(if placeId is null updates only addressComment)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/update-order-address")
    public ResponseEntity<OrderWithAddressesResponseDto> updateAddressForOrder(
        @Valid @RequestBody OrderAddressDtoRequest dtoRequest,
        @ApiIgnore @CurrentUserUuid String uuid) {
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
    @ApiOperation(value = "Delete order address")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.CREATED, response = OrderWithAddressesResponseDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/order-addresses/{id}")
    public ResponseEntity<OrderWithAddressesResponseDto> deleteOrderAddress(
        @Valid @PathVariable("id") Long id,
        @ApiIgnore @CurrentUserUuid String uuid) {
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
    @ApiOperation(value = "Make address actual (default)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddressDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/makeAddressActual/{addressId}")
    public ResponseEntity<AddressDto> makeAddressActual(
        @PathVariable Long addressId,
        @ApiIgnore @CurrentUserUuid String uuid) {
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
    @GetMapping("/get-all-districts")
    public ResponseEntity<List<LocationDto>> getAllDistrictsForRegionAndCity(String region, String city) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(ubsClientService.getAllDistrictsForRegionAndCity(region, city));
    }
}
