package greencity.exceptions.image;

import greencity.constant.ErrorMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileIsNullExceptionTest {
    @Test
    void fileIsNullExceptionTest() {
        String message = "File equals null";
        FileIsNullException ex = new FileIsNullException(message);
        assertEquals(ErrorMessage.FILE_IS_NULL, ex.getMessage());
    }
}
