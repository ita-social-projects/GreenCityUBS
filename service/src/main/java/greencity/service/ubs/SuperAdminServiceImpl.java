package greencity.service.ubs;

import greencity.dto.AddServiceDto;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.user.User;
import greencity.repository.BagRepository;
import greencity.repository.BagTranslationRepository;
import greencity.repository.UserRepository;
import greencity.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final BagTranslationRepository translationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public Bag addService(AddServiceDto dto, String uuid) {
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
}
