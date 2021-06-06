package greencity;

import greencity.dto.UserVO;
import greencity.dto.UserViolationMailDto;

public class ModelUtils {

    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(13L)
            .email("email").build();
    }

    public static UserViolationMailDto getUserViolationMailDto() {
        return UserViolationMailDto.builder()
            .name("String")
            .email("string@gmail.com")
            .violationDescription("Description")
            .build();
    }

}
