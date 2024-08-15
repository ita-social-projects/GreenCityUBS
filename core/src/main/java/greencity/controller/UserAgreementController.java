package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.dto.pageble.PageableDto;
import greencity.dto.user.UserAgreementDetailDto;
import greencity.dto.user.UserAgreementDto;
import greencity.service.ubs.UserAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static greencity.constant.AppConstant.USER_AGREEMENT_LINK;

@RestController
@RequestMapping(USER_AGREEMENT_LINK)
@AllArgsConstructor
public class UserAgreementController {
    private final UserAgreementService userAgreementService;

    /**
     * Retrieves all user agreements with pagination.
     *
     * @param page the pageable information.
     * @return a pageable list of {@link UserAgreementDetailDto}.
     */
    @Operation(summary = "Get all user agreements with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping
    public ResponseEntity<PageableDto<UserAgreementDetailDto>> getAllUserAgreements(
        @Parameter(hidden = true) Pageable page) {
        PageableDto<UserAgreementDetailDto> userAgreements = userAgreementService.findAll(page);
        return ResponseEntity.ok(userAgreements);
    }

    /**
     * Retrieves the latest user agreement.
     *
     * @return the latest {@link UserAgreementDto}.
     */
    @Operation(summary = "Get the latest user agreement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/latest")
    public ResponseEntity<UserAgreementDto> getLatestUserAgreement() {
        UserAgreementDto latestAgreement = userAgreementService.findLatest();
        return ResponseEntity.status(HttpStatus.OK).body(latestAgreement);
    }

    /**
     * Retrieves a user agreement by its ID.
     *
     * @param id the ID of the user agreement.
     * @return the {@link UserAgreementDetailDto}.
     */
    @Operation(summary = "Get a user agreement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserAgreementDetailDto> getUserAgreementById(@PathVariable Long id) {
        UserAgreementDetailDto userAgreement = userAgreementService.read(id);
        return ResponseEntity.status(HttpStatus.OK).body(userAgreement);
    }

    /**
     * Creates a new user agreement.
     *
     * @param userAgreementDto the user agreement to create.
     * @return the created {@link UserAgreementDetailDto}.
     */
    @Operation(summary = "Create a new user agreement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserAgreementDetailDto> createUserAgreement(
        @Valid @RequestBody UserAgreementDto userAgreementDto) {
        UserAgreementDetailDto createdAgreement = userAgreementService.create(userAgreementDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAgreement);
    }

    /**
     * Updates an existing user agreement by ID.
     *
     * @param id               the ID of the user agreement.
     * @param userAgreementDto the updated user agreement.
     * @return the updated {@link UserAgreementDetailDto}.
     */
    @Operation(summary = "Update a user agreement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserAgreementDetailDto> updateUserAgreement(
        @PathVariable Long id,
        @Valid @RequestBody UserAgreementDto userAgreementDto) {
        UserAgreementDetailDto updatedAgreement = userAgreementService.update(id, userAgreementDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAgreement);
    }

    /**
     * Deletes a user agreement by ID.
     *
     * @param id the ID of the user agreement.
     * @return an empty response entity with HTTP status 204.
     */
    @Operation(summary = "Delete a user agreement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = HttpStatuses.NO_CONTENT),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST, content = @Content),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND, content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserAgreement(@PathVariable Long id) {
        userAgreementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
