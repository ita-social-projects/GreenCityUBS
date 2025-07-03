package greencity.controller;

import greencity.annotations.CurrentUserUuid;
import greencity.constants.HttpStatuses;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.*;
import greencity.service.ubs.TelegramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/ubs/telegram")
@RequiredArgsConstructor
public class TelegramController {
    private final TelegramService telegramService;


    @Operation(summary = "Get all messages in chat by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<PageableDto<TelegramMessageDto>> getUserMessages(
        @PathVariable(name = "chatId") Long chatId, Pageable page) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.findUserMessageByChatId(chatId, page));
    }


    @Operation(summary = "Get all chats")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/chats")
    public ResponseEntity<PageableDto<ChatDto>> getChats(@RequestParam(required = false) String search, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getChats(search, pageable));
    }


    @Operation(summary = "Get last user order by chatId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/last-order")
    public ResponseEntity<OrdersDataForUserDto> getLastOrderByChatId(@RequestParam String chatId) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getLastOrderByChatId(chatId));
    }

    @Operation(summary = "Send message to user chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PostMapping(value = "/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendMessage(
            @RequestPart("data") @Valid CreateTelegramMessageRequest request,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) {
        telegramService.sendMessageToUser(request, files);
        return ResponseEntity.ok("OK");
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

//    /**
//     * Starts a server-sent event stream for the given chat ID. The stream will send
//     * any messages sent by the user with the given chat ID to the client.
//     *
//     * @param chatId the Telegram chat ID
//     * @return an SseEmitter that sends messages to the client
//     */
//    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
//    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter stream(@RequestParam String chatId) {
//        SseEmitter emitter = new SseEmitter(0L);
//        telegramStrimingService.addEmitter(emitter, chatId);
//
//        emitter.onCompletion(() -> telegramStrimingService.removeEmitter(emitter));
//        emitter.onTimeout(() -> telegramStrimingService.removeEmitter(emitter));
//
//        return emitter;
//    }

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
