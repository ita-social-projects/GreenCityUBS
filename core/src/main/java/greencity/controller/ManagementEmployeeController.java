package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constants.HttpStatuses;
import greencity.constants.SwaggerExampleModel;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.pageble.PageableAdvancedDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementEmployeeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/ubs-employee")
@RequiredArgsConstructor
public class ManagementEmployeeController {
    private final UBSManagementEmployeeService employeeService;
    private final UBSClientService ubsClientService;

    /**
     * Controller saves employee.
     *
     * @param employeeWithTariffsIdDto {@link EmployeeWithTariffsIdDto}
     * @return {@link EmployeeWithTariffsDto} saved employee.
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Save employee")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED, response = EmployeeWithTariffsIdDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('REGISTER_A_NEW_EMPLOYEE', authentication)")
    @PostMapping(value = "/save-employee")
    public ResponseEntity<EmployeeWithTariffsDto> saveEmployee(
        @ApiParam(value = SwaggerExampleModel.ADD_NEW_EMPLOYEE,
            required = true) @Valid @RequestPart EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @ApiParam(value = "Employee image") @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(employeeWithTariffsIdDto, image));
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_EMPLOYEES_PAGE', authentication)")
    @GetMapping("/getAll-employees")
    public ResponseEntity<Page<GetEmployeeDto>> getAllEmployees(
        @RequestParam EmployeePage employeePage,
        @RequestParam EmployeeFilterCriteria employeeFilterCriteria) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(employeeService.findAll(employeePage, employeeFilterCriteria));
    }

    /**
     * Controller updates information about employee.
     *
     * @return {@link EmployeeWithTariffsDto} update employee.
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Update information about employee")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = EmployeeWithTariffsIdDto.class),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND),
        @ApiResponse(code = 422, message = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping(value = "/update-employee",
        consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> update(
        @ApiParam(value = SwaggerExampleModel.EMPLOYEE_DTO,
            required = true) @RequestPart @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @ApiParam(value = "Employee image") @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(employeeWithTariffsIdDto, image));
    }

    /**
     * Controller deletes employee.
     *
     * @return {@link HttpStatus}
     * @author Mykola Danylko.
     */
    @ApiOperation(value = "Delete employee")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_EMPLOYEE', authentication)")
    @PutMapping("/deactivate-employee/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
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
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_EMPLOYEES_PAGE', authentication)")
    @GetMapping("/get-all-positions")
    public ResponseEntity<List<PositionDto>> getAllPositions() {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getAllPositions());
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
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_EMPLOYEE', authentication)")
    @DeleteMapping("/delete-employee-image/{id}")
    public ResponseEntity<HttpStatus> deleteEmployeeImage(@PathVariable Long id) {
        employeeService.deleteEmployeeImage(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Controller to get information about all employee's authorities.
     *
     * @return @return Set of {@link String}
     *
     * @author Inna Yashna.
     */
    @ApiOperation(value = "Get information about all employee's authorities")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @GetMapping("/get-all-authorities")
    public ResponseEntity<Object> getAllAuthorities(@RequestParam String email) {
        Set<String> authorities = ubsClientService.getAllAuthorities(email);
        return ResponseEntity.status(HttpStatus.OK).body(authorities);
    }

    /**
     * Controller edit an employee`s authorities.
     *
     * @return {@link UserEmployeeAuthorityDto}
     *
     * @author Inna Yashna.
     */
    @ApiOperation(value = "Edit an employee`s authorities")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_EMPLOYEES_AUTHORITIES', authentication)")
    @PutMapping("/edit-authorities")
    public ResponseEntity<Object> editAuthorities(@Valid @RequestBody UserEmployeeAuthorityDto dto) {
        ubsClientService.updateEmployeesAuthorities(dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Controller that return list of all tariffs.
     *
     * @return list of all tariffs.
     */
    @ApiOperation(value = "Get all tariffs for working with employee page")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_EMPLOYEES_PAGE', authentication)")
    @GetMapping("/getTariffs")
    public ResponseEntity<List<GetTariffInfoForEmployeeDto>> getTariffInfoForEmployee() {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getTariffsForEmployee());
    }
}