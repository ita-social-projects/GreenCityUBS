package greencity;

import greencity.dto.table.UbsTableCreationDto;
import greencity.dto.user.UserVO;
import greencity.dto.violation.UserViolationMailDto;

public class ModelUtils {

    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(13L)
            .email("email").build();
    }

    public static UbsTableCreationDto getUbsTableCreationDto() {
        return UbsTableCreationDto.builder()
            .uuid("87df9ad5-6393-441f-8423-8b2e770b01a8")
            .build();
    }

    public static UserViolationMailDto getUserViolationMailDto() {
        return UserViolationMailDto.builder()
            .name("String")
            .email("string@gmail.com")
            .violationDescription("Description")
            .build();
    }

}
