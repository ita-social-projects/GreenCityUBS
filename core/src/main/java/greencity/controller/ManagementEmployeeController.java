package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.constants.SwaggerExampleModel;
import greencity.dto.*;
import greencity.service.ubs.UBSManagementEmployeeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/ubs-employee")
public class ManagementEmployeeController {
    private final UBSManagementEmployeeService employeeService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public ManagementEmployeeController(UBSManagementEmployeeService employeeService) {
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
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = AddEmployeeDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping(value = "/save-employee",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeDto> saveEmployee(
        @ApiParam(value = SwaggerExampleModel.ADD_NEW_EMPLOYEE,
            required = true) @Valid @RequestPart AddEmployeeDto addEmployeeDto,
        @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(addEmployeeDto, image));
    }

    /**
     * Controller gets all employees.
     *
     * @return {@link PageableAdvancedDto} pageable employees.
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Get all employees")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EmployeeDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED)
    })
    @GetMapping("/getAll-employees")
    public ResponseEntity<PageableAdvancedDto<EmployeeDto>> getAllEmployees(@ApiIgnore Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.findAll(pageable));
    }

    /**
     * Controller updates information about employee.
     *
     * @return {@link EmployeeDto} update employee.
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Update information about employee")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EmployeeDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-employee")
    public ResponseEntity<EmployeeDto> update(@RequestBody @Valid EmployeeDto employeeDto) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(employeeDto));
    }

    /**
     * Controller deletes employee.
     *
     * @return {@link HttpStatus}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Delete employee")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EmployeeDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
    })
    @DeleteMapping("/{id}/delete-employee")
    public ResponseEntity<HttpStatus> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
