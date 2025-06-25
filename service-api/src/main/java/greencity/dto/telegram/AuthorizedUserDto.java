package greencity.dto.telegram;

public record AuthorizedUserDto(Long id, String chatId, Boolean isSupportStatusActive,
    Boolean isNotify, Long userId) {
}
