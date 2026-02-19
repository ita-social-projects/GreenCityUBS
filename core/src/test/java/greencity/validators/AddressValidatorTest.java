package greencity.validators;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import greencity.dto.CreateAddressRequestDto;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressValidatorTest {

    @Mock
    private GoogleApiService googleApiService;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private AddressValidator addressValidator;

    private CreateAddressRequestDto addressRequestDto;
    private GeocodingResult geoResult;
    private GeocodingResult geoResultEn;
    private AddressResponseFromGoogleAPI addressResponseFromGoogleAPI;

    @BeforeEach
    void setUp() {
        CoordinatesDto coordinatesDto = new CoordinatesDto(50.45, 30.523);
        addressRequestDto = CreateAddressRequestDto.builder()
            .cityUk("Київ")
            .cityEn("Kyiv")
            .regionUk("Київ")
            .regionEn("Kyiv")
            .coordinates(coordinatesDto)
            .placeId("place-id")
            .streetEn("Khreschatyk Street")
            .streetUk("вулиця Хрещатик")
            .build();

        geoResult = new GeocodingResult();
        geoResult.geometry = new Geometry();
        geoResult.geometry.location = new LatLng(50.45, 30.523);

        AddressComponent cityComponent = new AddressComponent();
        cityComponent.longName = "Київ";
        cityComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent regionComponent = new AddressComponent();
        regionComponent.longName = "Київ";
        regionComponent.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent streetComponent = new AddressComponent();
        streetComponent.longName = "вулиця Хрещатик";
        streetComponent.types = new AddressComponentType[] {AddressComponentType.ROUTE};

        geoResult.addressComponents = new AddressComponent[] {cityComponent, regionComponent, streetComponent};

        geoResultEn = new GeocodingResult();
        AddressComponent streetEn = new AddressComponent();
        streetEn.longName = "Khreschatyk Street";
        streetEn.types = new AddressComponentType[] {AddressComponentType.ROUTE};
        geoResultEn.addressComponents = new AddressComponent[] {streetEn};

        addressResponseFromGoogleAPI = new AddressResponseFromGoogleAPI();
        addressResponseFromGoogleAPI.setCity("Київ");
        addressResponseFromGoogleAPI.setRegion("Київ");
    }

    @Test
    void testIsValidWhenAllConditionsAreMetShouldReturnTrue() {
        when(googleApiService.getResultFromGeoCode(anyString(), eq(0))).thenReturn(geoResult);
        when(googleApiService.getResultFromGeoCode(anyString(), eq(1))).thenReturn(geoResultEn);
        when(googleApiService.getResultFromGoogleByCoordinates(any())).thenReturn(addressResponseFromGoogleAPI);

        assertTrue(addressValidator.isValid(addressRequestDto, context));
    }

    @Test
	void testIsValidWhenGoogleServiceReturnsNullShouldReturnFalse() {
		when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
		when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(null);

		ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

		boolean isValid = addressValidator.isValid(addressRequestDto, context);

		assertFalse(isValid);
		verify(violationBuilder).addConstraintViolation();
	}

    @Test
    void testIsValidWhenCoordinatesDoNotMatchShouldReturnFalse() {
        CoordinatesDto invalidCoordinatesDto = new CoordinatesDto(0.0, 0.0);
        addressRequestDto.setCoordinates(invalidCoordinatesDto);

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(addressRequestDto, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void testIsValidWhenCityDoesNotMatchShouldReturnFalse() {
        AddressComponent cityComponent = new AddressComponent();
        cityComponent.longName = "Lviv";
        cityComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        geoResult.addressComponents[0] = cityComponent;

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(addressRequestDto, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    void testIsValidWhenNotFoundExceptionOccursShouldReturnFalse() {
        doThrow(new NotFoundException("Location not found")).when(googleApiService)
            .getResultFromGeoCode(any(), any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(addressRequestDto, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void testIsValidWhenGoogleApiExceptionOccursShouldReturnFalse() {
        doThrow(new GoogleApiException("Google API error")).when(googleApiService)
            .getResultFromGeoCode(any(), any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(addressRequestDto, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void testWhenPlaceIdIsNullShouldReturnFalse() {
        addressRequestDto.setPlaceId(null);

        boolean isValid = addressValidator.isValid(addressRequestDto, context);

        assertFalse(isValid);
    }
}