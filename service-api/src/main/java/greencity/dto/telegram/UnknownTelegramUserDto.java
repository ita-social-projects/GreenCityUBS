package greencity.dto.telegram;

public record UnknownTelegramUserDto(Long id, String chatId, Boolean isSupportStatusActive, String firstName,
    String lastName, String userName, String mobileNumber) {
}
