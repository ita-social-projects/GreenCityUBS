package greencity.service.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.errors.InvalidRequestException;
import com.google.maps.model.GeocodingResult;
import greencity.constant.ErrorMessage;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import lombok.Data;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
@Data
public class GoogleApiService {
    private final GeoApiContext context;
    private static final List<Locale> locales = List.of(new Locale("uk"), new Locale("en"));

    /**
     * Send request to the Google and receive response with geocoding.
     *
     * @param placeId - place id
     * @return GeocodingResults - return result from geocoding service
     */
    public GeocodingResult getResultFromGeoCode(String placeId, Integer langCode) {
        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context)
                .place(placeId).language(locales.get(langCode).getLanguage()).await();
            return results[0];
        } catch (IOException | InterruptedException | ApiException e) {
            Thread.currentThread().interrupt();
            if (e instanceof InvalidRequestException) {
                throw new NotFoundException(ErrorMessage.NOT_FOUND_ADDRESS_BY_PLACE_ID + placeId);
            }
            throw new GoogleApiException(e.getMessage());
        }
    }
}
