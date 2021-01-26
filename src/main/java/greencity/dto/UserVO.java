package greencity.dto;

import greencity.dao.entity.enums.EmailNotification;
import greencity.dao.entity.enums.Role;
import greencity.dao.entity.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserVO {
    private Long id;

    private String name;

    private String email;

    private Role role;

    private String userCredo;

    private UserStatus userStatus;

    private LocalDateTime lastVisit;

    private Double rating;

    private EmailNotification emailNotification;

    private LocalDateTime dateOfRegistration;

    private List<UserVO> userFriends = new ArrayList<>();

    private String refreshTokenKey;

    private String profilePicturePath;

    private String firstName;

    private String city;

    private Boolean showLocation;

    private Boolean showEcoPlace;

    private Boolean showShoppingList;

    private LocalDateTime lastActivityTime;

}
