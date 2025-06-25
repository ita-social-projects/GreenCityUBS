package greencity.service.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.constant.ErrorMessage;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleApiService {
    private static final List<Locale> locales = List.of(Locale.of("uk"), Locale.of("en"));
    private final GeoApiContext context;
    private static final Locale UKRAINIAN = Locale.of("uk");

    /**
     * Send request to the Google and receive response with geocoding.
     *
     * @param placeId - place id
     * @return GeocodingResults - return result from geocoding service
     */
    @Cacheable(value = "geoCodeCache", key = "#placeId + '-' + #langCode")
    public GeocodingResult getResultFromGeoCode(String placeId, Integer langCode) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context)
                .place(placeId).language(locales.get(langCode).getLanguage()).await();
            return results[0];
        } catch (IOException | ApiException e) {
            log.error("Error during the call to Google API,"
                + " in method getResultFromGeoCode reason: {}", e.getMessage());
            if (e instanceof InvalidRequestException) {
                throw new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_BY_PLACE_ID + placeId);
            }
            throw new GoogleApiException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleApiException(e.getMessage());
        }
    }

    /**
     * Send request to the Google Api and receive GeocodingResult.
     *
     * @param countryName - name of country to search by google api
     * @param cityName    - name of city to search by google api
     * @param lang        - language code
     * @return {@link GeocodingResult} - result from geocoding service
     */
    public GeocodingResult getGeocodingResultByCityAndCountryAndLocale(String countryName, String cityName,
        String lang) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, cityName + ", " + countryName)
                .language(lang)
                .await();
            return results[0];
        } catch (IOException | ApiException e) {
            log.error("Occurred error during the call on google API, "
                + "in method getCoordinatesByGoogleMapsGeocoding, reason: {}", e.getMessage());
            if (e instanceof InvalidRequestException) {
                throw new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_BY_CITY_AND_COUNTRY
                    + cityName + ", " + countryName);
            }
            throw new GoogleApiException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GoogleApiException(e.getMessage());
        }
    }

    /**
     * Retrieves the address details from Google API based on the given geographical
     * coordinates. This method sends a request to the Google Geocoding API with the
     * provided latitude and longitude. It processes the response to extract address
     * components such as city and region. If the API call fails or no results are
     * found, it returns {@code null}.
     *
     * @param coordinates the {@link LatLng} object containing the latitude and
     *                    longitude.
     * @return an {@link AddressResponseFromGoogleAPI} containing city, region, and
     *         district details, or {@code null} if the API call fails or no results
     *         are returned.
     */
    @Cacheable(value = "coordinatesCache", key = "#coordinates.lat + '-' + #coordinates.lng")
    public AddressResponseFromGoogleAPI getResultFromGoogleByCoordinates(LatLng coordinates) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context)
                .latlng(coordinates)
                .language(UKRAINIAN.getLanguage())
                .await();
            return Optional.ofNullable(results)
                .filter(r -> r.length > 0)
                .map(r -> getAddressResponse(r[0].addressComponents))
                .orElse(null);
        } catch (IOException | ApiException e) {
            log.error("Error during the call to Google API, reason: {}", e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private AddressResponseFromGoogleAPI getAddressResponse(AddressComponent[] addressComponents) {
        AddressResponseFromGoogleAPI response = new AddressResponseFromGoogleAPI();

        Arrays.stream(addressComponents).forEach(component -> {
            List<AddressComponentType> componentTypes = Arrays.asList(component.types);

            if (isRegion(componentTypes)) {
                response.setRegion(component.longName);
            } else if (isCity(componentTypes)) {
                response.setCity(component.longName);
            } else if (isDistrict(componentTypes)) {
                response.setDistrict(component.longName);
            }
        });

        return response;
    }

    private boolean isRegion(List<AddressComponentType> componentTypes) {
        return componentTypes.contains(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1);
    }

    private boolean isCity(List<AddressComponentType> componentTypes) {
        return componentTypes.contains(AddressComponentType.LOCALITY);
    }

    private boolean isDistrict(List<AddressComponentType> componentTypes) {
        return componentTypes.contains(AddressComponentType.SUBLOCALITY);
    }
}
