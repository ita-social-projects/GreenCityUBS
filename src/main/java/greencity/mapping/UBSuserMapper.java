package greencity.mapping;

import greencity.dao.entity.user.ubs.Address;
import greencity.dao.entity.user.ubs.UBSuser;
import greencity.dto.PersonalDataDto;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UBSuserMapper extends AbstractConverter<PersonalDataDto, UBSuser> {
    @Override
    protected UBSuser convert(PersonalDataDto personalDataDto) {
        UBSuser ubs_user = UBSuser.builder()
                .id(personalDataDto.getId())
                .firstName(personalDataDto.getFirstName())
                .lastName(personalDataDto.getLastName())
                .email(personalDataDto.getEmail())
                .phoneNumber(personalDataDto.getPhoneNumber())
                .userAddress(Address.builder()
                        .city(personalDataDto.getCity())
                        .street(personalDataDto.getStreet())
                        .district(personalDataDto.getDistrict())
                        .houseNumber(personalDataDto.getHouseNumber())
                        .houseCorpus(personalDataDto.getHouseCorpus())
                        .entranceNumber(personalDataDto.getEntranceNumber())
                        .comment(personalDataDto.getAddressComment())
                        .build()).build();

        return ubs_user;
    }
}
