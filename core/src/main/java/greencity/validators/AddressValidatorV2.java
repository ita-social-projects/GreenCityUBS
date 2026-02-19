package greencity.validators;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import greencity.annotations.ValidAddressV2;
import greencity.dto.address.UpdateAddressDto;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import greencity.service.google.GoogleApiService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class AddressValidatorV2 implements ConstraintValidator<ValidAddressV2, UpdateAddressDto> {
    private final GoogleApiService googleApiService;
    private static final double DELTA = 0.007;
    private static final int LANGUAGE_CODE_FOR_UK = 0;
    private static final int LANGUAGE_CODE_FOR_EN = 1;

    @Override
    public boolean isValid(UpdateAddressDto updateAddressDto, ConstraintValidatorContext context) {
        String placeId = updateAddressDto.getOrderAddressExportDetails().getPlaceId();
        if (Objects.isNull(placeId)) {
            return false;
        }
        CoordinatesDto coordinates = updateAddressDto.getOrderAddressExportDetails().getCoordinates();
        LatLng latLng = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());

        GeocodingResult geoResultUk;
        GeocodingResult geoResultEn;
        AddressResponseFromGoogleAPI resultFromCoordinates;

        try {
            geoResultUk = googleApiService.getResultFromGeoCode(placeId, LANGUAGE_CODE_FOR_UK);
            geoResultEn = googleApiService.getResultFromGeoCode(placeId, LANGUAGE_CODE_FOR_EN);
            resultFromCoordinates = googleApiService.getResultFromGoogleByCoordinates(latLng);
        } catch (NotFoundException | GoogleApiException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Google API error: " + e.getMessage())
                .addConstraintViolation();
            return false;
        }

        if (resultFromCoordinates == null || !isCoordinatesValid(geoResultUk, coordinates)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid coordinates or address.")
                .addConstraintViolation();
            return false;
        }

        if (!areCityUkAndRegionUkValid(geoResultUk, resultFromCoordinates, updateAddressDto)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("City and region do not match the provided address.")
                .addConstraintViolation();
            return false;
        }

        if (!isStreetValid(geoResultUk, geoResultEn, updateAddressDto)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Street does not match the provided address.")
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

    private boolean areCityUkAndRegionUkValid(GeocodingResult geoResultUk,
        AddressResponseFromGoogleAPI resultFromCoordinates,
        UpdateAddressDto dto) {
        String apiCity = getLongName(geoResultUk.addressComponents, AddressComponentType.LOCALITY);

        if (apiCity == null) {
            return false;
        }

        return dto.getOrderAddressExportDetails().getCityUk().equalsIgnoreCase(apiCity)
            && apiCity.equalsIgnoreCase(resultFromCoordinates.getCity())
            && dto.getOrderAddressExportDetails().getCityUk().equalsIgnoreCase(resultFromCoordinates.getCity());
    }

    private String getLongName(AddressComponent[] addressComponents, AddressComponentType type) {
        return Arrays.stream(addressComponents)
            .filter(component -> Arrays.asList(component.types).contains(type))
            .map(component -> component.longName)
            .findFirst()
            .orElse(null);
    }

    private boolean isStreetValid(GeocodingResult geoResultUk, GeocodingResult geoResultEn,
        UpdateAddressDto dto) {
        String apiStreetUk = getLongName(geoResultUk.addressComponents, AddressComponentType.ROUTE);
        String apiStreetEn = getLongName(geoResultEn.addressComponents, AddressComponentType.ROUTE);
        if (apiStreetUk == null || apiStreetEn == null || dto.getOrderAddressExportDetails().getStreetUk() == null
            || dto.getOrderAddressExportDetails().getStreetEn() == null) {
            return false;
        }
        return apiStreetUk.equalsIgnoreCase(dto.getOrderAddressExportDetails().getStreetUk())
            && apiStreetEn.equalsIgnoreCase(dto.getOrderAddressExportDetails().getStreetEn());
    }
}