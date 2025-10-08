package greencity.controller;

import greencity.annotations.ValidImage;
import greencity.constant.ValidationConstant;
import greencity.constants.HttpStatuses;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UserEmployeeAuthorityDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.position.PositionAuthoritiesDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.service.ubs.UBSManagementEmployeeService;
import greencity.service.ubs.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

@RestController
@RequestMapping("/admin/ubs-employee")
@RequiredArgsConstructor
@Validated
public class ManagementEmployeeController {
    private final UBSManagementEmployeeService employeeService;
    private final UserService userService;

    /**
     * Saves a new employee with optional image upload.
     *
     * @param employeeWithTariffsIdDto DTO containing employee details and tariffs.
     * @param image                    Optional image file for the employee.
     * @return ResponseEntity containing the saved {@link EmployeeWithTariffsDto}
     *         and HTTP status 201 Created.
     */
    @Operation(summary = "Save employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = EmployeeWithTariffsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('REGISTER_A_NEW_EMPLOYEE', authentication)")
    @PostMapping(value = "/save-employee",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> saveEmployee(
        @RequestPart("employee") @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @RequestPart(value = "image", required = false) @ValidImage MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.save(employeeWithTariffsIdDto, image));
    }

    /**
     * Retrieves all employees with paging and filtering support.
     *
     * @param employeePage           Pagination parameters.
     * @param employeeFilterCriteria Filtering criteria for employees.
     * @return PageableDto containing a list of {@link GetEmployeeDto} and paging
     *         info.
     */
    @Operation(summary = "Get all employees")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
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
     * Updates an existing employee's information, optionally updating their image.
     *
     * @param employeeWithTariffsIdDto DTO containing updated employee details and
     *                                 tariffs.
     * @param image                    Optional updated image file for the employee.
     * @return ResponseEntity containing the updated {@link EmployeeWithTariffsDto}.
     */
    @Operation(summary = "Update information about employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EmployeeWithTariffsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "415", description = HttpStatuses.UNSUPPORTED_MEDIA_TYPE, content = @Content)
    })
    @PutMapping(value = "/update-employee",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployeeWithTariffsDto> update(
        @RequestPart("employee") @Valid EmployeeWithTariffsIdDto employeeWithTariffsIdDto,
        @Parameter(description = "Employee image") @RequestPart(required = false) @ValidImage MultipartFile image) {
        return ResponseEntity.status(HttpStatus.OK).body(employeeService.update(employeeWithTariffsIdDto, image));
    }

    /**
     * Deactivates (soft deletes) an employee by their ID.
     *
     * @param id ID of the employee to deactivate. Must be positive.
     * @return ResponseEntity with HTTP status 200 OK on successful deactivation.
     */
    @Operation(summary = "Delete employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_EMPLOYEE', authentication)")
    @PutMapping("/deactivate-employee/{id}")
    public ResponseEntity<HttpStatus> deleteEmployee(@Positive @PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Activates an employee by their ID.
     *
     * @param id ID of the employee to activate. Must be positive.
     * @return ResponseEntity with HTTP status 200 OK on successful activation.
     */
    @Operation(summary = "Activate employee")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('DEACTIVATE_EMPLOYEE', authentication)")
    @PutMapping("/activate-employee/{id}")
    public ResponseEntity<HttpStatus> activateEmployee(@Positive @PathVariable Long id) {
        employeeService.activateEmployee(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieves all available employee positions.
     *
     * @return ResponseEntity containing a list of {@link PositionDto} and HTTP
     *         status 200 OK.
     */
    @Operation(summary = "Get all employee positions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PositionDto.class)))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
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
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK, content = @Content),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content),
        @ApiResponse(responseCode = "422", description = HttpStatuses.UNPROCESSABLE_ENTITY, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_EMPLOYEE', authentication)")
    @DeleteMapping("/delete-employee-image/{id}")
    public ResponseEntity<HttpStatus> deleteEmployeeImage(@Positive @PathVariable Long id) {
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/get-all-authorities")
    public ResponseEntity<Object> getAllAuthorities(
        @Email(regexp = ValidationConstant.EMAIL_REGEXP) @RequestParam String email) {
        Set<String> authorities = userService.getAllAuthorities(email);
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/get-positions-authorities")
    public ResponseEntity<PositionAuthoritiesDto> getPositionsAndRelatedAuthorities(
        @Email(regexp = ValidationConstant.EMAIL_REGEXP) @RequestParam String email) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getPositionsAndRelatedAuthorities(email));
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
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('EDIT_EMPLOYEES_AUTHORITIES', authentication)")
    @PutMapping("/edit-authorities")
    public ResponseEntity<Object> editAuthorities(@Valid @RequestBody UserEmployeeAuthorityDto dto) {
        userService.updateEmployeesAuthorities(dto);
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
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('SEE_TARIFFS', authentication)")
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
    @Operation(summary = "Get all employees with enabled chat by tariff id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping(value = "/get-employees/{tariffId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeWithTariffsDto>> getEmployeesByTariffId(@Positive @PathVariable Long tariffId) {
        return ResponseEntity.ok().body(employeeService.getEmployeesByTariffId(tariffId));
    }

    /**
     * Endpoint to fetch an employee along with their tariffs by their email.
     *
     * @param email The email of the employee to be fetched.
     * @return Containing an EmployeeWithTariffsDto object representing the employee
     *         with the given email. This object includes details of the employee
     *         and the tariffs associated with them.
     */
    @Operation(summary = "Get employee with tariffs by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/{email}")
    public ResponseEntity<EmployeeWithTariffsDto> getEmployeesByUserId(
        @Email(regexp = ValidationConstant.EMAIL_REGEXP) @PathVariable String email) {
        return ResponseEntity.ok().body(employeeService.getEmployeeByEmail(email));
    }
}
