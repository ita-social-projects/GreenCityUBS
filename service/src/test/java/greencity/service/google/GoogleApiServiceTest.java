package greencity.service.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.constant.ErrorMessage;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleApiServiceTest {

    @InjectMocks
    GoogleApiService googleApiService;
    @Mock
    GeoApiContext geoApiContext;
    @Mock
    GeocodingApiRequest geocodingApiRequest;

    private LatLng coordinates;
    private GeocodingResult geocodingResult;

    @BeforeEach
    public void setUp() {
        coordinates = new LatLng(50.45, 30.523);

        AddressComponent cityComponent = new AddressComponent();
        cityComponent.longName = "Kyiv";
        cityComponent.types = new AddressComponentType[] {AddressComponentType.LOCALITY};

        AddressComponent regionComponent = new AddressComponent();
        regionComponent.longName = "Kyiv";
        regionComponent.types = new AddressComponentType[] {AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1};

        AddressComponent districtComponent = new AddressComponent();
        districtComponent.longName = "Pechersk";
        districtComponent.types = new AddressComponentType[] {AddressComponentType.SUBLOCALITY};

        geocodingResult = new GeocodingResult();
        geocodingResult.addressComponents = new AddressComponent[] {cityComponent, regionComponent, districtComponent};
    }

    @Test
    @SneakyThrows
    void testGetResultFromGeoCode() {
        String placeId = "qwe";
        Integer langCode = 0;
        String language = "uk";

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(geoApiContext))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.place(placeId)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(language)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.await()).thenReturn(new GeocodingResult[1]);

            assertDoesNotThrow(() -> googleApiService.getResultFromGeoCode(placeId, langCode));
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGeoCodeThrowsNotFoundException() {
        String placeId = "qwe";
        Integer langCode = 0;
        String language = "uk";

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(geoApiContext))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.place(placeId)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(language)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.await()).thenThrow(new InvalidRequestException("message"));

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

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.newRequest(geoApiContext))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.place(placeId)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(language)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.await()).thenThrow(new InterruptedException());

            assertThrows(GoogleApiException.class, () -> googleApiService.getResultFromGeoCode(placeId, langCode));
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGoogleMapsGeocodingByCityAndCounty() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(geoApiContext, cityName + ", " + countryName))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(lang)).thenReturn(geocodingApiRequest);
            when(geocodingApiRequest.await()).thenReturn(new GeocodingResult[1]);

            assertDoesNotThrow(() -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(geocodingApiRequest).language(lang);
            verify(geocodingApiRequest).await();
        }
    }

    @Test
    @SneakyThrows
    void testGetResultFromGoogleMapsGeocodingByCityAndCountryThrowsGoogleApiException() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(geoApiContext, cityName + ", " + countryName))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(lang)).thenReturn(geocodingApiRequest);
            when(geocodingApiRequest.await()).thenThrow(new InterruptedException());

            assertThrows(GoogleApiException.class, () -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(geocodingApiRequest).language(lang);
            verify(geocodingApiRequest).await();
        }
    }

    @Test
    @SneakyThrows
    void testGetGeocodingResultByCityAndCountryAndLocaleThrowsNotFoundException() {
        String lang = "en";
        String cityName = "Kyiv";
        String countryName = "Ukraine";

        try (MockedStatic<GeocodingApi> utilities = mockStatic(GeocodingApi.class)) {
            utilities.when(() -> GeocodingApi.geocode(geoApiContext, cityName + ", " + countryName))
                .thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.language(lang)).thenReturn(geocodingApiRequest);
            when(geocodingApiRequest.await()).thenThrow(new InvalidRequestException("message"));

            assertThrows(NotFoundException.class, () -> googleApiService
                .getGeocodingResultByCityAndCountryAndLocale(countryName, cityName, lang));

            verify(geocodingApiRequest).language(lang);
            verify(geocodingApiRequest).await();
        }
    }

    @Test
    void testGetResultFromGoogleByCoordinatesWhenValidResponseShouldReturnAddressResponse() throws Exception {
        GeocodingResult[] results = new GeocodingResult[] {geocodingResult};

        try (MockedStatic<GeocodingApi> mockedStatic = mockStatic(GeocodingApi.class)) {
            mockedStatic.when(() -> GeocodingApi.newRequest(geoApiContext)).thenReturn(geocodingApiRequest);

            when(geocodingApiRequest.latlng(coordinates)).thenReturn(geocodingApiRequest);
            when(geocodingApiRequest.language("uk")).thenReturn(geocodingApiRequest);
            when(geocodingApiRequest.await()).thenReturn(results);

            AddressResponseFromGoogleAPI response = googleApiService.getResultFromGoogleByCoordinates(coordinates);

            assertNotNull(response);
            assertEquals("Kyiv", response.getCity());
            assertEquals("Kyiv", response.getRegion());
            assertEquals("Pechersk", response.getDistrict());

            verify(geocodingApiRequest).latlng(coordinates);
            verify(geocodingApiRequest).language("uk");
            verify(geocodingApiRequest).await();
        }
    }
}
