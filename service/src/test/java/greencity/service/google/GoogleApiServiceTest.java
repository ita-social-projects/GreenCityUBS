package greencity.service.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.GeocodingResult;
import greencity.constant.ErrorMessage;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleApiServiceTest {

    @InjectMocks
    GoogleApiService googleApiService;
    @Mock
    GeoApiContext context;
    @Mock
    GeocodingApiRequest request;

    @Test
    @SneakyThrows
    void testGetResultFromGeoCode() {
        String placeId = "qwe";
        Integer langCode = 0;
        String language = "uk";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(context))
                .thenReturn(request);

            when(request.place(placeId)).thenReturn(request);

            when(request.language(language)).thenReturn(request);

            when(request.await()).thenReturn(new GeocodingResult[1]);

            assertDoesNotThrow(() -> googleApiService.getResultFromGeoCode(placeId, langCode));
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGeoCodeThrowsNotFoundException() {
        String placeId = "qwe";
        Integer langCode = 0;
        String language = "uk";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(context))
                .thenReturn(request);

            when(request.place(placeId)).thenReturn(request);

            when(request.language(language)).thenReturn(request);

            when(request.await()).thenThrow(new InvalidRequestException("message"));

            NotFoundException exception =
                assertThrows(NotFoundException.class, () -> googleApiService.getResultFromGeoCode(placeId, langCode));

            assertEquals(ErrorMessage.NOT_FOUND_ADDRESS_BY_PLACE_ID + placeId, exception.getMessage());
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGeoCodeThrowsGoogleApiException() {
        String placeId = "qwe";
        Integer langCode = 0;
        String language = "uk";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(context))
                .thenReturn(request);

            when(request.place(placeId)).thenReturn(request);

            when(request.language(language)).thenReturn(request);

            when(request.await()).thenThrow(new InterruptedException());

            assertThrows(GoogleApiException.class, () -> googleApiService.getResultFromGeoCode(placeId, langCode));
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGoogleMapsGeocodingByCityAndCounty() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(context, cityName + ", " + countryName))
                .thenReturn(request);

            when(request.language(lang)).thenReturn(request);
            when(request.await()).thenReturn(new GeocodingResult[1]);

            assertDoesNotThrow(() -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(request).language(lang);
            verify(request).await();
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGoogleMapsGeocodingByCityAndCountryThrowsGoogleApiException() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(context, cityName + ", " + countryName))
                .thenReturn(request);

            when(request.language(lang)).thenReturn(request);
            when(request.await()).thenThrow(new InterruptedException());

            assertThrows(GoogleApiException.class, () -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(request).language(lang);
            verify(request).await();
        }
    }

    @Test
    @SneakyThrows
    void testGetGeocodingResultByCityAndCountryAndLocaleThrowsNotFoundException() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = Mockito.mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(context, cityName + ", " + countryName))
                .thenReturn(request);

            when(request.language(lang)).thenReturn(request);
            when(request.await()).thenThrow(new InvalidRequestException("message"));

            assertThrows(NotFoundException.class, () -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(request).language(lang);
            verify(request).await();
        }
    }
}
