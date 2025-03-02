package greencity.validators;

import com.google.maps.model.LatLng;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.order.OrderAddressDtoRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAddressValidatorTest {
    @Mock
    private GoogleApiService googleApiService;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private UpdateAddressValidator addressValidator;

    private OrderAddressDtoRequest orderAddressDtoRequest;
    private AddressResponseFromGoogleAPI addressResponseFromGoogleAPI;

    @BeforeEach
    public void setUp() {
        CoordinatesDto coordinatesDto = new CoordinatesDto(50.45, 30.523);
        orderAddressDtoRequest = OrderAddressDtoRequest.builder()
            .city("Kyiv")
            .region("Kyiv")
            .coordinates(coordinatesDto)
            .build();

        addressResponseFromGoogleAPI = new AddressResponseFromGoogleAPI();
        addressResponseFromGoogleAPI.setCity("Kyiv");
        addressResponseFromGoogleAPI.setRegion("Kyiv");
    }

    @Test
	void testIsValidWhenAllConditionsAreMetShouldReturnTrue() {
		when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(addressResponseFromGoogleAPI);

		boolean isValid = addressValidator.isValid(orderAddressDtoRequest, context);

		assertTrue(isValid);
		verify(googleApiService).getResultFromGoogleByCoordinates(any());
	}

    @Test
    void testWithInvalidCoordinatesShouldReturnFalse() {
        orderAddressDtoRequest.setCity("Invalid");
        orderAddressDtoRequest.setRegion("Invalid");

        when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class)))
            .thenReturn(addressResponseFromGoogleAPI);

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(orderAddressDtoRequest, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(googleApiService).getResultFromGoogleByCoordinates(any());
    }

    @Test
	void testIsValidWhenGoogleServiceReturnsNullShouldReturnFalse() {
		when(googleApiService.getResultFromGoogleByCoordinates(any(LatLng.class))).thenReturn(null);

		ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);

		boolean isValid = addressValidator.isValid(orderAddressDtoRequest, context);

		assertFalse(isValid);
		verify(violationBuilder).addConstraintViolation();
		verify(googleApiService).getResultFromGoogleByCoordinates(any());
	}

    @Test
    void testIsValidWhenNotFoundExceptionOccursShouldReturnFalse() {
        doThrow(new NotFoundException("Location not found")).when(googleApiService)
            .getResultFromGoogleByCoordinates(any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(orderAddressDtoRequest, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(context).disableDefaultConstraintViolation();
        verify(googleApiService).getResultFromGoogleByCoordinates(any());
    }

    @Test
    void testIsValidWhenGoogleApiExceptionOccursShouldReturnFalse() {
        doThrow(new GoogleApiException("Google API error")).when(googleApiService)
            .getResultFromGoogleByCoordinates(any());

        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(violationBuilder);

        boolean isValid = addressValidator.isValid(orderAddressDtoRequest, context);

        assertFalse(isValid);
        verify(violationBuilder).addConstraintViolation();
        verify(context).disableDefaultConstraintViolation();
        verify(googleApiService).getResultFromGoogleByCoordinates(any());
    }
}
