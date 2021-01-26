package greencity.mapping;

import greencity.dao.entity.user.ubs.UBSuser;
import greencity.dto.PersonalDataDto;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PersonalDataDtoMapper extends AbstractConverter<UBSuser, PersonalDataDto> {
    @Override
    protected PersonalDataDto convert(UBSuser ubs_user) {
        return PersonalDataDto.builder()
                                .id(ubs_user.getId())
                                .firstName(ubs_user.getFirstName())
                                .lastName(ubs_user.getLastName())
                                .phoneNumber(ubs_user.getPhoneNumber())
                                .email(ubs_user.getEmail())
                                .city(ubs_user.getUserAddress().getCity())
                                .street(ubs_user.getUserAddress().getStreet())
                                .district(ubs_user.getUserAddress().getDistrict())
                                .houseNumber(ubs_user.getUserAddress().getHouseNumber())
                                .houseCorpus(ubs_user.getUserAddress().getHouseCorpus())
                                .entranceNumber(ubs_user.getUserAddress().getEntranceNumber())
                                .addressComment(ubs_user.getUserAddress().getComment())
                                .build();
    }
}
