//package greencity.mapping.employee;
//
//import greencity.dto.position.PositionDto;
//import greencity.dto.position.PositionWithTranslateDto;
//import greencity.entity.user.employee.Position;
//import org.modelmapper.AbstractConverter;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Class that used by {@link ModelMapper} to map {@link Position} into
// * {@link PositionDto}.
// */
//@Component
//public class PositionDtoWithTranslateMapper extends AbstractConverter<PositionDto, PositionWithTranslateDto> {
//    /**
//     * Method convert {@link Position} to {@link PositionDto}.
//     *
//     * @return {@link PositionDto}
//     */
//    @Override
//    protected PositionWithTranslateDto convert(PositionDto position) {
//        Map<String, String> nameTranslations = new HashMap<>();
//        nameTranslations.put("ua", position.getName());
//        nameTranslations.put("en", position.getName_eng());
//
//        return PositionWithTranslateDto.builder()
//                .id(position.getId())
//                .name(nameTranslations)
//                .build();
//    }
//}
