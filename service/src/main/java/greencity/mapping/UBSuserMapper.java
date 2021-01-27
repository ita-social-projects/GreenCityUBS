package greencity.mapping;

import greencity.dto.PersonalDataDto;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.UBSuser;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link PersonalDataDto} into
 * {@link UBSuser}.
 */
@Component
public class UBSuserMapper extends AbstractConverter<PersonalDataDto, UBSuser> {
    /**
     * Method convert {@link PersonalDataDto} to {@link UBSuser}.
     *
     * @return {@link UBSuser}
     */
    @Override
    protected UBSuser convert(PersonalDataDto personalDataDto) {
        UBSuser ubsUser = UBSuser.builder()
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
                .build())
            .build();

        return ubsUser;
    }
}
