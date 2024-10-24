package greencity.validators;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.annotations.ValidAddress;
import greencity.dto.CreateAddressRequestDto;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import greencity.service.google.GoogleApiService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressValidator implements ConstraintValidator<ValidAddress, CreateAddressRequestDto> {
    private final GoogleApiService googleApiService;
    private static final double DELTA = 0.007;
    private static final int LANGUAGE_CODE_FOR_UA = 0;

    @Override
    public boolean isValid(CreateAddressRequestDto createAddressRequestDto, ConstraintValidatorContext context) {
        String placeId = createAddressRequestDto.getPlaceId();

        if (Objects.isNull(placeId)) {
            return false;
        }

        CoordinatesDto coordinates = createAddressRequestDto.getCoordinates();
        LatLng latLng = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());

        GeocodingResult geoResult;
        AddressResponseFromGoogleAPI resultFromCoordinates;

        try {
            geoResult = googleApiService.getResultFromGeoCode(placeId, LANGUAGE_CODE_FOR_UA);
            resultFromCoordinates = googleApiService.getResultFromGoogleByCoordinates(latLng);
        } catch (NotFoundException | GoogleApiException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Google API error: " + e.getMessage())
                .addConstraintViolation();
            return false;
        }

        if (resultFromCoordinates == null || !isCoordinatesValid(geoResult, coordinates)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid coordinates or address.")
                .addConstraintViolation();
            return false;
        }

        if (!areCityAndRegionValid(geoResult, resultFromCoordinates, createAddressRequestDto)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("City and region do not match the provided address.")
                .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isCoordinatesValid(GeocodingResult geoResult, CoordinatesDto coordinates) {
        double geoLat = geoResult.geometry.location.lat;
        double geoLng = geoResult.geometry.location.lng;
        return isWithinDelta(coordinates.getLatitude(), coordinates.getLongitude(), geoLat, geoLng);
    }

    private boolean isWithinDelta(double lat1, double lon1, double lat2, double lon2) {
        return Math.abs(lat1 - lat2) <= DELTA && Math.abs(lon1 - lon2) <= DELTA;
    }

    private boolean areCityAndRegionValid(GeocodingResult geoResult, AddressResponseFromGoogleAPI resultFromCoordinates,
        CreateAddressRequestDto dto) {
        String apiCity = getLongName(geoResult.addressComponents, AddressComponentType.LOCALITY);

        if (apiCity == null) {
            return false;
        }

        return dto.getCity().equalsIgnoreCase(apiCity)
            && apiCity.equalsIgnoreCase(resultFromCoordinates.getCity())
            && dto.getCity().equalsIgnoreCase(resultFromCoordinates.getCity());
    }

    private String getLongName(AddressComponent[] addressComponents, AddressComponentType type) {
        return Arrays.stream(addressComponents)
            .filter(component -> Arrays.asList(component.types).contains(type))
            .map(component -> component.longName)
            .findFirst()
            .orElse(null);
    }
}
