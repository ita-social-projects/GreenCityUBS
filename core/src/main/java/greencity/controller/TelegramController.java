package greencity.controller;

import greencity.constants.HttpStatuses;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.ChatDto;
import greencity.dto.telegram.CreateTelegramMessageRequest;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.MarkMessagesAsReadRequest;
import greencity.dto.telegram.TelegramMessageDto;
import greencity.service.ubs.TelegramFeedbackService;
import greencity.service.ubs.TelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller that handles Telegram-related endpoints. Provides
 * functionality for managing chats, messages, feedback, and orders related to
 * Telegram bot users.
 */
@RestController
@RequestMapping("/ubs/telegram")
@RequiredArgsConstructor
@Validated
public class TelegramController {
    private final TelegramService telegramService;
    private final TelegramFeedbackService telegramFeedbackService;

    /**
     * Retrieves all messages for a given chat ID with pagination support.
     *
     * @param chatId the chat identifier
     * @param page   pagination parameters
     * @return pageable list of Telegram messages belonging to the chat
     */
    @Operation(summary = "Get all messages in chat by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<PageableDto<TelegramMessageDto>> getUserMessages(
        @Positive @PathVariable(name = "chatId") Long chatId, Pageable page) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.findUserMessageByChatId(chatId, page));
    }

    /**
     * Retrieves all chats with optional filtering by search term and pagination.
     *
     * @param search   optional search term to filter chats
     * @param pageable pagination parameters
     * @return pageable list of chats matching the criteria
     */
    @Operation(summary = "Get all chats")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/chats")
    public ResponseEntity<PageableDto<ChatDto>> getChats(@RequestParam(required = false) String search,
        @PageableDefault(sort = "lastMessage.sendAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getChats(search, pageable));
    }

    /**
     * Retrieves the last order details associated with the specified chat ID.
     *
     * @param chatId the chat identifier
     * @return last order data for the user linked to the chat
     */
    @Operation(summary = "Get last user order by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/last-order")
    public ResponseEntity<OrdersDataForUserDto> getLastOrderByChatId(@Positive @RequestParam Long chatId) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramService.getLastOrderByChatId(chatId));
    }

    /**
     * Sends a message to a user chat, optionally with attached files. Access
     * restricted to users with TELEGRAM_MANAGEMENT authority.
     *
     * @param request the message request data
     * @param files   optional files to attach to the message
     * @return HTTP 200 OK response if a message sent successfully
     */
    @Operation(summary = "Send message to user chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PostMapping(value = "/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendMessage(
        @RequestPart("data") @Valid CreateTelegramMessageRequest request,
        @RequestPart(value = "files", required = false) MultipartFile[] files) {
        telegramService.sendMessageToUser(request, files);
        return ResponseEntity.ok("OK");
    }

    /**
     * Retrieves chat information by its ID. Access restricted to users with
     * TELEGRAM_MANAGEMENT authority.
     *
     * @param chatId the chat identifier
     * @return chat data
     */
    @Operation(summary = "Get chat by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/chat/{chatId}")
    public ResponseEntity<ChatDto> getChat(@Positive @PathVariable Long chatId) {
        return ResponseEntity.ok(telegramService.getChatById(chatId));
    }

    /**
     * Retrieves all feedback entries with pagination. Access restricted to users
     * with TELEGRAM_MANAGEMENT authority.
     *
     * @param pageable pagination parameters
     * @return pageable list of feedback DTOs
     */
    @Operation(summary = "Get all feedbacks")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/feedbacks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageableDto<FeedbackDto>> getAllFeedbacks(Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(telegramFeedbackService.getAllFeedbacks(pageable));
    }

    /**
     * Retrieves all feedback entries filtered by a specific chat ID with
     * pagination. Access restricted to users with TELEGRAM_MANAGEMENT authority.
     *
     * @param chatId   the chat identifier as a string
     * @param pageable pagination parameters
     * @return pageable list of feedback DTOs for the given chat
     */
    @Operation(summary = "Get all feedbacks by chatId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @GetMapping(value = "/feedbacks/{chatId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageableDto<FeedbackDto>> getAllFeedbacksByChatId(
        @PathVariable(name = "chatId") String chatId, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(telegramFeedbackService.getAllFeedbacksByChatId(chatId, pageable));
    }

    @Operation(summary = "Mark messages as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = HttpStatuses.NO_CONTENT),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PreAuthorize("@preAuthorizer.hasAuthority('TELEGRAM_MANAGEMENT', authentication)")
    @PutMapping(value = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markMessagesAsRead(@RequestBody MarkMessagesAsReadRequest request) {
        telegramService.markMessagesAsRead(request);
    }
}
