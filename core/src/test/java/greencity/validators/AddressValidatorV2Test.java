package greencity.validators;

import com.google.maps.model.*;
import greencity.ModelUtils;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import greencity.service.google.GoogleApiService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AddressValidatorV2Test {
    @Mock
    private GoogleApiService googleApiService;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @InjectMocks
    private AddressValidatorV2 validator;

    private UpdateAddressDto updateAddressDto;
    private GeocodingResult geoResultUK;
    private GeocodingResult geoResultEN;
    private AddressResponseFromGoogleAPI addressResponseFromGoogleAPI;

    @BeforeEach
    void setUp() {
        updateAddressDto = ModelUtils.getUpdateAddressDto();

        geoResultUK = new GeocodingResult();
        geoResultUK.geometry = new Geometry();
        geoResultUK.geometry.location = new LatLng(50.4501, 30.5234);

        AddressComponent cityUKComponent = new AddressComponent();
        cityUKComponent.longName = updateAddressDto.getOrderAddressExportDetails().getCityUk();
        cityUKComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent regionUKComponent = new AddressComponent();
        regionUKComponent.longName = updateAddressDto.getOrderAddressExportDetails().getRegionUk();
        regionUKComponent.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent streetUKComponent = new AddressComponent();
        streetUKComponent.longName = updateAddressDto.getOrderAddressExportDetails().getStreetUk();
        streetUKComponent.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geoResultUK.addressComponents = new AddressComponent[] {cityUKComponent, regionUKComponent, streetUKComponent};

        geoResultEN = new GeocodingResult();
        AddressComponent cityENComponent = new AddressComponent();
        cityENComponent.longName = updateAddressDto.getOrderAddressExportDetails().getCityEn();
        cityENComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent regionENComponent = new AddressComponent();
        regionENComponent.longName = updateAddressDto.getOrderAddressExportDetails().getRegionEn();
        regionENComponent.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent streetENComponent = new AddressComponent();
        streetENComponent.longName = updateAddressDto.getOrderAddressExportDetails().getStreetEn();
        streetENComponent.types = new AddressComponentType[] {AddressComponentType.ROUTE};
        geoResultEN.addressComponents = new AddressComponent[] {cityENComponent, regionENComponent, streetENComponent};

        addressResponseFromGoogleAPI = new AddressResponseFromGoogleAPI();
        addressResponseFromGoogleAPI.setCity(updateAddressDto.getOrderAddressExportDetails().getCityUk());
        addressResponseFromGoogleAPI.setRegion(updateAddressDto.getOrderAddressExportDetails().getRegionUk());
    }

    @Test
    void testIsValidWhenAllConditionsAreMetShouldReturnTrue() {
        when(googleApiService.getResultFromGeoCode(anyString(), eq(0))).thenReturn(geoResultUK);
        when(googleApiService.getResultFromGeoCode(anyString(), eq(1))).thenReturn(geoResultEN);
        when(googleApiService.getResultFromGoogleByCoordinates(any())).thenReturn(addressResponseFromGoogleAPI);

        assertTrue(validator.isValid(updateAddressDto, constraintValidatorContext));
    }

    @Test
    void testIsValidWhenGoogleServiceReturnsNullShouldReturnFalse() {
        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResultUK);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(null);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void testIsValidWhenCoordinatesDoNotMatchShouldReturnFalse() {
        CoordinatesDto invalidCoordinatesDto = new CoordinatesDto(0.0, 0.0);
        updateAddressDto.getOrderAddressExportDetails().setCoordinates(invalidCoordinatesDto);

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResultUK);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void testIsValidWhenCityDoesNotMatchShouldReturnFalse() {
        AddressComponent cityComponent = new AddressComponent();
        cityComponent.longName = "Lviv";
        cityComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        geoResultUK.addressComponents[0] = cityComponent;

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResultUK);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void testIsValidWhenNotFoundExceptionOccursShouldReturnFalse() {
        doThrow(new NotFoundException("Location not found")).when(googleApiService)
            .getResultFromGeoCode(any(), any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);
        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
    }

    @Test
    void testIsValidWhenGoogleApiExceptionOccursShouldReturnFalse() {
        doThrow(new GoogleApiException("Google API error")).when(googleApiService)
            .getResultFromGeoCode(any(), any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(constraintValidatorContext).disableDefaultConstraintViolation();
    }

    @Test
    void testWhenPlaceIdIsNullShouldReturnFalse() {
        updateAddressDto.getOrderAddressExportDetails().setPlaceId(null);

        boolean isValid = validator.isValid(updateAddressDto, constraintValidatorContext);

        assertFalse(isValid);
    }
}