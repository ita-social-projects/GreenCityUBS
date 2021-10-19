package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.ServiceNotFoundException;
import greencity.repository.*;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceImplTest {
    @InjectMocks
    private SuperAdminServiceImpl superAdminService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private BagTranslationRepository bagTranslationRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ServiceTranslationRepository serviceTranslationRepository;

    @Test
    void addTariffServiceTest() {
        User user = ModelUtils.getUser();
        Bag bag = new Bag();
        AddServiceDto dto = ModelUtils.addServiceDto();
        Language language = new Language();
        BagTranslation bagTranslation = new BagTranslation();
        Location location = new Location();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto, Bag.class)).thenReturn(bag);
        when(modelMapper.map(dto, BagTranslation.class)).thenReturn(bagTranslation);
        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(bagRepository.save(bag)).thenReturn(bag);
        when(bagTranslationRepository.save(bagTranslation)).thenReturn(bagTranslation);

        superAdminService.addTariffService(dto, "123233");

        verify(modelMapper).map(dto, Bag.class);
        verify(modelMapper).map(dto, BagTranslation.class);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).save(bagTranslation);
    }

    @Test
    void getTariffServiceTest() {
        when(bagTranslationRepository.findAll()).thenReturn(new ArrayList<BagTranslation>());

        superAdminService.getTariffService();

        verify(bagTranslationRepository).findAll();
    }

    @Test
    void deleteTariffServiceTest() {
        when(bagRepository.findById(1)).thenReturn(ModelUtils.getBag());

        superAdminService.deleteTariffService(1);

        verify(bagRepository).delete(ModelUtils.getBag().get());
    }

    @Test
    void deleteTariffServiceThrowException() {
        assertThrows(BagNotFoundException.class, () -> superAdminService.deleteTariffService(1));
    }

    @Test
    void editTariffService_Throw_Exception() {
        EditTariffServiceDto dto = new EditTariffServiceDto();
        assertThrows(BagNotFoundException.class, () -> superAdminService.editTariffService(dto, 1, "testUUid"));
    }

    @Test
    void editTariffService() {
        String uuid = "testUUid";
        BagTranslation bagTranslation = ModelUtils.getBagTranslationForEditMethod();
        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        Bag bag = ModelUtils.getBag().get();
        User user = new User();
        user.setRecipientName("John");
        user.setRecipientSurname("Doe");

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(bagTranslationRepository.findBagTranslationByBagAndLanguageCode(bag, dto.getLangCode()))
            .thenReturn(bagTranslation);
        when(bagTranslationRepository.save(bagTranslation)).thenReturn(bagTranslation);

        superAdminService.editTariffService(dto, 1, uuid);

        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).findBagTranslationByBagAndLanguageCode(bag, dto.getLangCode());
        verify(bagTranslationRepository).save(bagTranslation);
    }

    @Test
    void deleteService() {
        Service service = new Service();
        service.setId(1L);

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        superAdminService.deleteService(service.getId());
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(serviceRepository, times(1)).delete(service);
    }

    @Test
    void getService() {
        when(serviceTranslationRepository.findAll()).thenReturn(Arrays.asList(new ServiceTranslation()));
        List<ServiceTranslation> serviceTranslations = serviceTranslationRepository.findAll();

        verify(serviceTranslationRepository, times(1)).findAll();
        assertEquals(1, serviceTranslations.size());
    }

    @Test
    void editService() {
        String uuid = "testUUid";
        Service service = new Service();
        service.setId(1L);
        User user = new User();
        CreateServiceDto dto = new CreateServiceDto();
        dto.setPrice(1);
        dto.setCommission(1);
        Location location = ModelUtils.getLocation();
        Language language = new Language();
        ServiceTranslation serviceTranslation = new ServiceTranslation();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        when(userRepository.findByUuid(uuid)).thenReturn(user);

        when(locationRepository.findById(dto.getLocationId())).thenReturn(Optional.ofNullable(location));

        when(languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()))
            .thenReturn(Optional.ofNullable(language));

        when(serviceTranslationRepository.findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode())).thenReturn(serviceTranslation);

        when(serviceRepository.save(service)).thenReturn(service);

        superAdminService.editService(service.getId(), dto, uuid);

        verify(userRepository, times(1)).findByUuid(uuid);
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(locationRepository, times(1)).findById(dto.getLocationId());
        verify(languageRepository, times(1)).findLanguageByLanguageCode(dto.getLanguageCode());
        verify(serviceTranslationRepository, times(1)).findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void addService() {
        User user = ModelUtils.getUser();
        Location location = new Location();
        Language language = new Language();
        Service service = new Service();
        CreateServiceDto dto = new CreateServiceDto();
        dto.setPrice(1);
        dto.setCommission(1);
        ServiceTranslation serviceTranslation = new ServiceTranslation();

        when(modelMapper.map(dto, Service.class)).thenReturn(service);
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(locationRepository.findById(dto.getLocationId())).thenReturn(Optional.of(location));
        when(languageRepository.findLanguageByLanguageCode(dto.getLanguageCode())).thenReturn(Optional.of(language));
        when(serviceRepository.save(service)).thenReturn(service);
        when(modelMapper.map(dto, ServiceTranslation.class)).thenReturn(serviceTranslation);
        when(serviceTranslationRepository.save(serviceTranslation)).thenReturn(serviceTranslation);

        superAdminService.addService(dto, user.getUuid());

        verify(modelMapper).map(dto, Service.class);
        verify(modelMapper).map(dto, ServiceTranslation.class);
        verify(userRepository).findByUuid(user.getUuid());
        verify(locationRepository).findById(dto.getLocationId());
        verify(languageRepository).findLanguageByLanguageCode(dto.getLanguageCode());
        verify(serviceRepository).save(service);
        verify(serviceTranslationRepository).save(serviceTranslation);
    }

}
