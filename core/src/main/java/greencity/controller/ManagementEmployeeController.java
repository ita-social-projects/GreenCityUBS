package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.constants.SwaggerExampleModel;
import greencity.dto.employee.AddEmployeeDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.pageble.PageableAdvancedDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.service.ubs.UBSManagementEmployeeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

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
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = EmployeeDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping(value = "/save-employee",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeDto> saveEmployee(
        @ApiParam(value = SwaggerExampleModel.ADD_NEW_EMPLOYEE,
            required = true) @Valid @RequestPart AddEmployeeDto addEmployeeDto,
        @ApiParam(value = "Employee image") @RequestPart(required = false) MultipartFile image) {
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
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableAdvancedDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ApiPageable
    @GetMapping("/getAll-employees")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(employeeService.findAll(employeePage, employeeFilterCriteria));
    }

    /**
     * Controller gets all active employees.
     *
     * @return {@link PageableAdvancedDto} pageable active employees.
     * @author Yurii Kuzo.
     */
    @ApiOperation(value = "Get all active employees")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableAdvancedDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @ApiPageable
    @GetMapping("/getAll-active-employees")
    public ResponseEntity<Page<EmployeeDto>> getAllActiveEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(employeeService.findAllActiveEmployees(employeePage, employeeFilterCriteria));
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
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping(value = "/update-employee",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeDto> update(
        @ApiParam(value = SwaggerExampleModel.EMPLOYEE_DTO,
            required = true) @RequestPart @Valid EmployeeDto employeeDto,
        @ApiParam(value = "Employee image") @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(employeeDto, image));
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
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-employee/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller creates employee position.
     *
     * @return {@link PositionDto}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Create employee position")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = PositionDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PostMapping("/create-position")
    public ResponseEntity<PositionDto> createPosition(@Valid AddingPositionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    /**
     * Controller updates employee position.
     *
     * @return {@link PositionDto}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Update employee position")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PositionDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping("/update-position")
    public ResponseEntity<PositionDto> updatePosition(@RequestBody @Valid PositionDto dto) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(dto));
    }

    /**
     * Controller gets all employee positions.
     *
     * @return {@link PositionDto}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Get all employee positions")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PositionDto[].class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/get-all-positions")
    public ResponseEntity<List<PositionDto>> getAllPositions() {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getAllPositions());
    }

    /**
     * Controller deletes employee position.
     *
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Deletes employee position")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete-position/{id}")
    public ResponseEntity<HttpStatus> deletePosition(@PathVariable Long id) {
        employeeService.deletePosition(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller deletes employee image.
     *
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Deletes employee image")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @DeleteMapping("/delete-employee-image/{id}")
    public ResponseEntity<HttpStatus> deleteEmployeeImage(@PathVariable Long id) {
        employeeService.deleteEmployeeImage(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
