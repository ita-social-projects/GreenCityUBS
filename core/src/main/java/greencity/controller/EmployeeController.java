package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.constants.SwaggerExampleModel;
import greencity.dto.AddEmployeeDto;
import greencity.dto.EmployeeDto;
import greencity.dto.OrderClientDto;
import greencity.dto.UserVO;
import greencity.service.ubs.UBSEmployeeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;


@RestController
@RequestMapping("/admin/ubs-employee")
public class EmployeeController {

    private UBSEmployeeService employeeService;

    /**
     * Constructor with parameters.
     */
    public EmployeeController(UBSEmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Controller saves employee.
     *
     * @param addEmployeeDto {@link AddEmployeeDto}
     * @return {@link EmployeeDto} saved employee.
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Save employee")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = HttpStatuses.OK, response = AddEmployeeDto.class),
            @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST)
    })
    @PostMapping(value = "/save-employee", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeDto> saveEmployee(
            @ApiParam(value = SwaggerExampleModel.ADD_NEW_EMPLOYEE, required = true)
            @RequestPart AddEmployeeDto addEmployeeDto, @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(addEmployeeDto, image));
    }
}
