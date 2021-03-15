package greencity;

import greencity.dto.UserVO;
import java.time.LocalDateTime;

public class ModelUtils {

    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(13L)
            .email("email").build();
    }

}
