package greencity.validators;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.exceptions.address.InvalidAddressException;
import greencity.service.google.GoogleApiService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
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
    private AddressResponseFromGoogleAPI addressResponseFromGoogleAPI;

    @BeforeEach
    public void setUp() {
        CoordinatesDto coordinatesDto = new CoordinatesDto(50.45, 30.523);
        addressRequestDto = CreateAddressRequestDto.builder()
            .city("Kyiv")
            .region("Kyiv")
            .coordinates(coordinatesDto)
            .placeId("place-id")
            .build();

        geoResult = new GeocodingResult();
        geoResult.geometry = new Geometry();
        geoResult.geometry.location = new LatLng(50.45, 30.523);

        AddressComponent cityComponent = new AddressComponent();
        cityComponent.longName = "Kyiv";
        cityComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent regionComponent = new AddressComponent();
        regionComponent.longName = "Kyiv";
        regionComponent.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        geoResult.addressComponents = new AddressComponent[] {cityComponent, regionComponent};

        addressResponseFromGoogleAPI = new AddressResponseFromGoogleAPI();
        addressResponseFromGoogleAPI.setCity("Kyiv");
        addressResponseFromGoogleAPI.setRegion("Kyiv");
    }

    @Test
	void testIsValidWhenAllConditionsAreMetShouldReturnTrue() {
		when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
		when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(addressResponseFromGoogleAPI);

		boolean isValid = addressValidator.isValid(addressRequestDto, context);

		assertTrue(isValid);
	}

    @Test
    void testIsValidWhenCoordinatesMismatchShouldReturnFalse() {
        geoResult.geometry.location = new LatLng(51.0, 31.0);

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        assertThrows(InvalidAddressException.class,
            () -> addressValidator.isValid(addressRequestDto, context));
    }

    @Test
    void testIsValidWhenCityAndRegionMismatchShouldReturnFalse() {
        geoResult.addressComponents[0].longName = "Lviv";
        geoResult.addressComponents[1].longName = "Lviv Oblast";
        addressResponseFromGoogleAPI.setCity("Lviv");
        addressResponseFromGoogleAPI.setRegion("Lviv Oblast");

        when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        assertThrows(InvalidAddressException.class,
            () -> addressValidator.isValid(addressRequestDto, context));
    }

    @Test
	void testIsValidWhenGoogleServiceReturnsNullShouldReturnFalse() {
		when(googleApiService.getResultFromGeoCode(anyString(), anyInt())).thenReturn(geoResult);
		when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(null);

		assertThrows(InvalidAddressException.class,
			() -> addressValidator.isValid(addressRequestDto,context));
	}
}
