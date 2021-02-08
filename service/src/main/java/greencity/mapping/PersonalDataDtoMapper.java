package greencity.mapping;

import greencity.dto.PersonalDataDto;
import greencity.entity.user.ubs.UBSuser;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link UBSuser} into
 * {@link PersonalDataDto}.
 */
@Component
public class PersonalDataDtoMapper extends AbstractConverter<UBSuser, PersonalDataDto> {
    /**
     * Method convert {@link UBSuser} to {@link PersonalDataDto}.
     *
     * @return {@link PersonalDataDto}
     */
    @Override
    protected PersonalDataDto convert(UBSuser ubsUser) {
        return PersonalDataDto.builder()
            .id(ubsUser.getId())
            .firstName(ubsUser.getFirstName())
            .lastName(ubsUser.getLastName())
            .phoneNumber(ubsUser.getPhoneNumber())
            .email(ubsUser.getEmail())
            .city(ubsUser.getUserAddress().getCity())
            .street(ubsUser.getUserAddress().getStreet())
            .district(ubsUser.getUserAddress().getDistrict())
            .latitude(ubsUser.getUserAddress().getCoordinates().getLatitude())
            .longitude(ubsUser.getUserAddress().getCoordinates().getLongitude())
            .houseNumber(ubsUser.getUserAddress().getHouseNumber())
            .houseCorpus(ubsUser.getUserAddress().getHouseCorpus())
            .entranceNumber(ubsUser.getUserAddress().getEntranceNumber())
            .addressComment(ubsUser.getUserAddress().getComment())
            .build();
    }
}
