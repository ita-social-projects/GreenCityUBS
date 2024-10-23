package greencity.validators;

import com.google.maps.model.LatLng;
import greencity.annotations.ValidUpdateAddress;
import greencity.dto.google.AddressResponseFromGoogleAPI;
import greencity.dto.location.CoordinatesDto;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.api.GoogleApiException;
import greencity.service.google.GoogleApiService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateAddressValidator implements ConstraintValidator<ValidUpdateAddress, OrderAddressDtoRequest> {
    private final GoogleApiService googleApiService;

    @Override
    public boolean isValid(OrderAddressDtoRequest dto, ConstraintValidatorContext context) {
        CoordinatesDto coordinates = dto.getCoordinates();
        LatLng latLng = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());

        AddressResponseFromGoogleAPI resultFromCoordinates;

        try {
            resultFromCoordinates = googleApiService.getResultFromGoogleByCoordinates(latLng);
        } catch (NotFoundException | GoogleApiException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Google API error: " + e.getMessage())
                .addConstraintViolation();
            return false;
        }

        if (resultFromCoordinates == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid coordinates or address.")
                .addConstraintViolation();
            return false;
        }

        if (!checkRegionAndCityIsValid(resultFromCoordinates, dto)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid region or city.")
                .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean checkRegionAndCityIsValid(AddressResponseFromGoogleAPI resultFromCoordinates,
        OrderAddressDtoRequest dto) {
        return resultFromCoordinates.getRegion().equalsIgnoreCase(dto.getRegion())
            && resultFromCoordinates.getCity().equalsIgnoreCase(dto.getCity());
    }
}
