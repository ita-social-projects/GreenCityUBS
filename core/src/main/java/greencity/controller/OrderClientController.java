package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.OrderClientDto;
import greencity.service.ubs.UBSClientService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("order-client")
public class OrderClientController {
    private final UBSClientService ubsClientService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public OrderClientController(UBSClientService ubsClientService) {
        this.ubsClientService = ubsClientService;
    }

    /**
     * Controller returns all user's orders.
     *
     * @param uuid {@link String} id;
     * @return {@link HttpStatus} - http status.
     */
    @ApiOperation(value = "Get all orders done by user")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = HttpStatuses.OK, response = OrderClientDto[].class),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getAll-users-orders")
    public ResponseEntity<List<OrderClientDto>> getAllOrdersDoneByUser(
            @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getAllOrdersDoneByUser(uuid));
    }
}
