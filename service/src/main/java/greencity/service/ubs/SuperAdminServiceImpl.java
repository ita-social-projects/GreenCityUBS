package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.AddServiceDto;
import greencity.dto.GetTariffServiceDto;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.BagTranslationRepository;
import greencity.repository.UserRepository;
import greencity.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final BagTranslationRepository translationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Bag addTariffService(AddServiceDto dto, String uuid) {
        Language language = new Language();
        language.setId(dto.getLanguageId());
        Bag bag = modelMapper.map(dto, Bag.class);
        BagTranslation translation = modelMapper.map(dto, BagTranslation.class);
        bag.setFullPrice(dto.getPrice() + dto.getCommission());
        bag.setCreatedAt(LocalDate.now());
        translation.setBag(bag);
        translation.setLanguage(language);
        User user = userRepository.findByUuid(uuid);
        bag.setCreatedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        bagRepository.save(bag);
        translationRepository.save(translation);
        return bag;
    }

    @Override
    public List<GetTariffServiceDto> getTariffService() {
        return translationRepository.findAll()
            .stream()
            .map(this::getTariffService)
            .collect(Collectors.toList());
    }

    private GetTariffServiceDto getTariffService(BagTranslation bagTranslation) {
        return GetTariffServiceDto.builder()
            .description(bagTranslation.getDescription())
            .price(bagTranslation.getBag().getPrice())
            .capacity(bagTranslation.getBag().getCapacity())
            .name(bagTranslation.getName())
            .commission(bagTranslation.getBag().getCommission())
            .languageCode(bagTranslation.getLanguage().getCode())
            .fullPrice(bagTranslation.getBag().getFullPrice())
            .build();
    }

    /**
     * This method delete tariff service by Id.
     * @param id - Tariff Service Id.
     */
    public void deleteTariffService(long id) {
        try {
            Bag bag = new Bag();
            bag.setId((int) id);
            BagTranslation bagTranslation = new BagTranslation();
            bagTranslation.setBag(bag);
            bagRepository.deleteById(bag.getId());
            translationRepository.delete(bagTranslation);
        } catch (Exception e) {
            throw new BagNotFoundException(ErrorMessage.BAG_NOT_FOUND + id);
        }
    }
}
