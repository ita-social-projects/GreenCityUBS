package greencity.dto.user;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String userCredo;
    private String role;
    private String userStatus;
}