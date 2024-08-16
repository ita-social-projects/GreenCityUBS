package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.dto.useragreement.UserAgreementDetailDto;
import greencity.dto.useragreement.UserAgreementDto;
import greencity.service.ubs.UserAgreementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import java.util.List;
import static greencity.constant.AppConstant.USER_AGREEMENT_LINK;

@RestController
@RequestMapping(USER_AGREEMENT_LINK)
@AllArgsConstructor
public class UserAgreementController {
    private final UserAgreementService userAgreementService;

    /**
     * Retrieves all user agreement IDs sorted by creation date in ascending order.
     *
     * @return a {@link ResponseEntity} containing a list of user agreement IDs
     *         sorted from oldest to newest, with an HTTP status of 200 (OK).
     */
    @Operation(summary = "Get all user agreements ids sorted in ASC")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED, content = @Content),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN, content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Long>> getAllUserAgreements() {
        List<Long> userAgreementsIds = userAgreementService.findAllIdSortedByAsc();
        return ResponseEntity.status(HttpStatus.OK).body(userAgreementsIds);
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
        @Valid @RequestBody UserAgreementDto userAgreementDto, Principal principal) {
        UserAgreementDetailDto createdAgreement = userAgreementService.create(userAgreementDto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAgreement);
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
