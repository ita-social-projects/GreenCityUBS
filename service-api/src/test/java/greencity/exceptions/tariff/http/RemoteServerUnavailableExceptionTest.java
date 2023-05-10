package greencity.exceptions.tariff.http;

import greencity.constant.ErrorMessage;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemoteServerUnavailableExceptionTest {
    @Test
    void remoteServerUnavailableExceptionMessageTest() {
        String message = "Could not retrieve user data";
        var exception = new RemoteServerUnavailableException(message);
        Assertions.assertEquals(ErrorMessage.COULD_NOT_RETRIEVE_USER_DATA, exception.getMessage());
    }
}
