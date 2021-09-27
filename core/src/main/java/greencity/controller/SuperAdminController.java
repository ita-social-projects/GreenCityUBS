package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.AddServiceDto;
import greencity.entity.order.Bag;
import greencity.service.SuperAdminService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/ubs/superAdmin")
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    /**
     * Constructor for initialize SuperAdminService.
     */
    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    /**
     * Controller created Service.
     *
     * @param dto {@link AddServiceDto} dto for service.
     * @return {@link Bag}
     * @author Vadym Makitra.
     */
    @ApiOperation(value = "Create service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = AddServiceDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @PostMapping("/createService")
    public ResponseEntity<Bag> createService(
        @RequestBody AddServiceDto dto,
        @ApiIgnore @CurrentUserUuid String uuid) {
        return ResponseEntity.status(HttpStatus.OK).body(superAdminService.addService(dto, uuid));
    }
}
