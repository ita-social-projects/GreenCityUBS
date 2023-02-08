package greencity.service.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import greencity.exceptions.api.GoogleApiException;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@Data
public class GoogleApiService {
    private final GeoApiContext context;
    private static final List<Locale> locales = List.of(new Locale("uk"), new Locale("en"));

    /**
     * Send request to the google and receive response with geocoding.
     *
     * @param searchRequest - address for search
     * @return GeocodingResults - return result from geocoding service
     */
    public List<GeocodingResult> getResultFromGeoCode(String searchRequest, Integer langCode) {
        List<GeocodingResult> geocodingResults = new ArrayList<>();

        try {
            GeocodingResult[] results = GeocodingApi.newRequest(context)
                .address(searchRequest).language(locales.get(langCode).getLanguage()).await();
            Collections.addAll(geocodingResults, results);
        } catch (IOException | InterruptedException | ApiException e) {
            Thread.currentThread().interrupt();
            throw new GoogleApiException(e.getMessage());
        }
        return geocodingResults;
    }
}
