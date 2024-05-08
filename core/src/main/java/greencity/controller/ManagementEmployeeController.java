package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.pageble.PageableAdvancedDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.position.PositionAuthoritiesDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
     * Controller method to save an employee.
     *
     * @param employeeWithTariffsIdDto DTO for {@link EmployeeWithTariffsIdDto}.
     * @param image                    Image of the employee (optional).
     * @return ResponseEntity with {@link EmployeeWithTariffsDto} instance.
     * @author [Author Name]
     */
    @ApiOperation(value = "Save employee")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = HttpStatuses.CREATED),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('REGISTER_A_NEW_EMPLOYEE', authentication)")
    @PostMapping(value = "/save-employee",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> saveEmployee(
        @RequestPart("employee") @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(employeeWithTariffsIdDto, image));
    }

    /**
     * Controller gets all employees.
     *
     * @return PageableDto of {@link GetEmployeeDto} employees.
     * @author Mykola Danylko.
     * @author Olena Sotnik.
     */
    @ApiOperation(value = "Get all employees")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK, response = PageableAdvancedDto.class),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_EMPLOYEES_PAGE', authentication)")
    @GetMapping("/getAll-employees")
    public ResponseEntity<PageableDto<GetEmployeeDto>> getAllEmployees(
        EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
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
        @RequestPart("employee") @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
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
     * Controller activate employee.
     *
     * @return {@link HttpStatus}
     * @author Oksana Spodaryk.
     */
    @ApiOperation(value = "Activate employee")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_EMPLOYEE', authentication)")
    @PutMapping("/activate-employee/{id}")
    public ResponseEntity<HttpStatus> activateEmployee(@PathVariable Long id) {
        employeeService.activateEmployee(id);
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
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-authorities")
    public ResponseEntity<Object> getAllAuthorities(@RequestParam String email) {
        Set<String> authorities = ubsClientService.getAllAuthorities(email);
        return ResponseEntity.status(HttpStatus.OK).body(authorities);
    }

    /**
     * Controller to get an employee`s positions and all possible related
     * authorities to these positions.
     *
     * @param email {@link String} - employee email.
     * @return {@link PositionAuthoritiesDto}
     *
     * @author Anton Bondar.
     */
    @ApiOperation(value = "Get information about an employee`s positions and all possible "
        + "related authorities to these positions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-positions-authorities")
    public ResponseEntity<PositionAuthoritiesDto> getPositionsAndRelatedAuthorities(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getPositionsAndRelatedAuthorities(email));
    }

    /**
     * Controller to get a list of login employee`s positions.
     *
     * @param email {@link String} - employee email.
     * @return List of {@link String} - list of employee positions.
     *
     * @author Anton Bondar.
     */
    @ApiOperation(value = "Get information about login employee`s positions.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = HttpStatuses.OK),
        @ApiResponse(code = 400, message = HttpStatuses.BAD_REQUEST),
        @ApiResponse(code = 401, message = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(code = 403, message = HttpStatuses.FORBIDDEN),
        @ApiResponse(code = 404, message = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-employee-login-positions")
    public ResponseEntity<List<String>> getEmployeeLoginPositionNames(@RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(ubsClientService.getEmployeeLoginPositionNames(email));
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

    /**
     * Retrieves all employees associated with a specific tariff ID.
     *
     * @param tariffId The ID of the tariff.
     * @return ResponseEntity containing a list of GetEmployeeDto objects
     *         representing the employees, with HttpStatus OK if successful.
     */
    @ApiOperation(value = "Get all employees with enabled chat by tariff id")
    @GetMapping(value = "/get-employees/{tariffId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeWithTariffsDto>> getEmployeesByTariffId(@PathVariable Long tariffId) {
        return ResponseEntity.ok().body(employeeService.getEmployeesByTariffId(tariffId));
    }

    @ApiOperation(value = "Get employee with tariffs by email")
    @GetMapping(value = "/{email}")
    public ResponseEntity<EmployeeWithTariffsDto> getEmployeesByUserId(@PathVariable String email) {
        return ResponseEntity.ok().body(employeeService.getEmployeeByEmail(email));
    }
}
