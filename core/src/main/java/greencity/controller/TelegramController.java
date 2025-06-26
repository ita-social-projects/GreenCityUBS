package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.AuthorizedUserDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.TelegramImageDto;
import greencity.dto.telegram.TelegramTextMessageDto;
import greencity.dto.telegram.UnknownTelegramUserDto;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.TelegramService;
import greencity.service.ubs.TelegramStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ubs/telegram")
@RequiredArgsConstructor
public class TelegramController {
    private final TelegramService telegramService;
    private final TelegramPhotoService telegramPhotoService;
    private final TelegramStreamingService telegramStrimingService;

    /**
     * Retrieves a list of TelegramUserMessages for a given chatId.
     *
     * @param chatId the Telegram chat ID
     * @return a list of TelegramUserMessages
     */
    @Operation(summary = "Get all user messages by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/user-messages/{chatId}")
    public ResponseEntity<PageableDto<TelegramTextMessageDto>> getUserMessages(
        @PathVariable(name = "chatId") String chatId, Pageable page) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.findUserMessageByChatId(chatId, page));
    }

    /**
     * Retrieves a list of TelegramPhotos for a given chatId.
     *
     * @param chatId the Telegram chat ID
     * @return a list of TelegramPhotos
     */
    @Operation(summary = "Get all photos by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/user-photos/{chatId}")
    public ResponseEntity<PageableDto<TelegramImageDto>> getUserPhotos(@PathVariable(name = "chatId") String chatId,
                                                                       Pageable page) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.findUserPhotosByChatId(chatId, page));
    }

    /**
     * Retrieves a paginated list of authorized Telegram users (TelegramBots),
     * optionally filtered by a search term.
     *
     * @param search   optional keyword to filter users by recipient name or surname
     * @param pageable pagination and sorting information
     * @return {@link ResponseEntity} containing a {@link PageableDto} of {@link AuthorizedUserDto}
     */
    @Operation(summary = "Get all authorized tg users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/get-all-authorized-users")
    public ResponseEntity<PageableDto<AuthorizedUserDto>> getAllAuthoredUsers(@RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getAllUsers(search, pageable));
    }

    @GetMapping("/get-all-unauthorized-users")
    public ResponseEntity<PageableDto<UnknownTelegramUserDto>> getAllUnauthorizedUsers(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getAllUnauthorizedUsers(pageable));
    }

    /**
     * Sends a message to a Telegram user.
     *
     * @param chatId  the Telegram chat ID
     * @param message the message to send
     * @return an OK response if the message was sent successfully
     */
    @Operation(summary = "Send message to user chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PostMapping("/send-message/{chatId}")
    public ResponseEntity<String> sendMessage(@PathVariable(name = "chatId") String chatId,
        @RequestParam String message) {
        telegramService.sendMessageToUser(chatId, message);
        return ResponseEntity.ok("OK");
    }

    /**
     * Sends a photo to a Telegram user.
     *
     * @param chatId   the Telegram chat ID
     * @param photoUrl the URL of the photo to send
     * @param caption  the caption for the photo
     * @return an OK response if the photo was sent successfully
     */
    @Operation(summary = "Send photo to user chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/send-photo/{chatId}")
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    public ResponseEntity<String> sendPhoto(@PathVariable(name = "chatId") String chatId, @RequestParam String photoUrl,
        @RequestParam String caption) {
        telegramPhotoService.sendPhotoToUser(chatId, photoUrl, caption);
        return ResponseEntity.ok("OK");
    }

    /**
     * Uploads a photo to Azure Blob storage and sends it to a Telegram user.
     *
     * @param file    the photo file to upload
     * @param chatId  the Telegram chat ID
     * @param caption the caption for the photo
     * @return an OK response if the photo was uploaded and sent successfully
     */
    @Operation(summary = "Upload photo to user chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PostMapping("/upload-photo/{chatId}")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file,
        @PathVariable(name = "chatId") String chatId,
        @RequestParam("caption") String caption) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select a file to upload");
        }
        var link = telegramPhotoService.savePhotoToAzureBlob(file);
        telegramPhotoService.sendPhotoToUser(chatId, link, caption.isEmpty() ? "" : caption);

        telegramPhotoService.deletePhotoFromAzureBlob(link);
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    /**
     * Generates a link for a manager to communicate with a user.
     *
     * @param employeeUUID the UUID of the user to communicate with
     * @return a link to communicate with the user
     */
    @Operation(summary = "Generate manager authorization link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PostMapping("/generate-manager-link")
    public ResponseEntity<String> generateManagerLink(@CurrentUserUuid String employeeUUID) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.generateManagerStartLink(employeeUUID));
    }

    /**
     * Starts a server-sent event stream for the given chat ID. The stream will send
     * any messages sent by the user with the given chat ID to the client.
     *
     * @param chatId the Telegram chat ID
     * @return an SseEmitter that sends messages to the client
     */
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String chatId) {
        SseEmitter emitter = new SseEmitter(0L);
        telegramStrimingService.addEmitter(emitter, chatId);

        emitter.onCompletion(() -> telegramStrimingService.removeEmitter(emitter));
        emitter.onTimeout(() -> telegramStrimingService.removeEmitter(emitter));

        return emitter;
    }

    /**
     * Retrieves a list of {@link FeedbackDto} for all users.
     *
     * @param pageable the page to retrieve
     *
     * @return a list of {@link FeedbackDto} associated with the specified pageable
     */
    @Operation(summary = "Get all feedbacks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/feedbacks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageableDto<FeedbackDto>> getAllFeedbacks(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getAllFeedbacks(pageable));
    }

    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/feedbacks/{chatId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageableDto<FeedbackDto>> getAllFeedbacksByChatId(
        @PathVariable(name = "chatId") String chatId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getAllFeedbacksByChatId(chatId, pageable));
    }
}
