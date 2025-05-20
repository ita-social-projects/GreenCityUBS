package greencity.dto;

public record SuccessSignInDto(Long userId, String accessToken, String refreshToken,
    String name, boolean ownRegistrations) {
}
