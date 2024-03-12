package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.constants.SwaggerExampleModel;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.service.ubs.UBSClientService;
import greencity.service.ubs.UBSManagementEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import jakarta.validation.Valid;
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
    @Operation(summary = "Save employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = EmployeeWithTariffsDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('REGISTER_A_NEW_EMPLOYEE', authentication)")
    @PostMapping(value = "/save-employee",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> saveEmployee(
        @Parameter(description = SwaggerExampleModel.ADD_NEW_EMPLOYEE,
            required = true) @Valid @RequestPart EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @Parameter(description = "Employee image") @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(employeeWithTariffsIdDto, image));
    }

    /**
     * Controller gets all employees.
     *
     * @return PageableDto of {@link GetEmployeeDto} employees.
     * @author Mykola Danylko.
     * @author Olena Sotnik.
     */
    @Operation(summary = "Get all employees")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
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
    @Operation(summary = "Update information about employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EmployeeWithTariffsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY)
    })
    @PutMapping(value = "/update-employee",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> update(
        @Parameter(description = SwaggerExampleModel.EMPLOYEE_DTO,
            required = true) @RequestPart @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @Parameter(description = "Employee image") @RequestPart(required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(employeeWithTariffsIdDto, image));
    }

    /**
     * Controller deletes employee.
     *
     * @return {@link HttpStatus}
     * @author Mykola Danylko.
     */
    @Operation(summary = "Delete employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Activate employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Get all employee positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PositionDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
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
    @Operation(summary = "Deletes employee image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY)
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
    @Operation(summary = "Get information about all employee's authorities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Get information about an employee`s positions and all possible "
        + "related authorities to these positions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Get information about login employee`s positions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Edit an employee`s authorities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
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
    @Operation(summary = "Get all tariffs for working with employee page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_EMPLOYEES_PAGE', authentication)")
    @GetMapping("/getTariffs")
    public ResponseEntity<List<GetTariffInfoForEmployeeDto>> getTariffInfoForEmployee() {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.getTariffsForEmployee());
    }
}
