package greencity.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageValidatorTest {
    @Mock
    private ConstraintValidatorContext context;

    private ImageValidator.SingleImageValidator singleImageValidator;
    private ImageValidator.ArrayImageValidator arrayImageValidator;

    @BeforeEach
    void setUp() {
        singleImageValidator = new ImageValidator.SingleImageValidator();
        arrayImageValidator = new ImageValidator.ArrayImageValidator();
    }

    @Test
    void singleImageValidatorShouldReturnTrueWhenFileIsNull() {
        assertTrue(singleImageValidator.isValid(null, context));
    }

    @Test
    void singleImageValidatorShouldReturnTrueWhenMimeTypeStartsWithImage() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");

        boolean result = singleImageValidator.isValid(file, context);

        assertTrue(result);
    }

    @Test
    void singleImageValidatorShouldReturnFalseWhenMimeTypeDoesNotStartWithImage() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");

        boolean result = singleImageValidator.isValid(file, context);

        assertFalse(result);
    }

    @Test
    void singleImageValidatorShouldReturnFalseWhenMimeTypeIsNull() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(null);

        boolean result = singleImageValidator.isValid(file, context);

        assertFalse(result);
    }

    @Test
    void arrayImageValidatorShouldReturnTrueWhenFilesArrayIsNull() {
        assertTrue(arrayImageValidator.isValid(null, context));
    }

    @Test
    void arrayImageValidatorShouldReturnTrueWhenAllFilesAreValidImages() {
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        when(file1.getContentType()).thenReturn("image/jpeg");
        when(file2.getContentType()).thenReturn("image/png");

        MultipartFile[] files = new MultipartFile[] {file1, file2};

        assertTrue(arrayImageValidator.isValid(files, context));
    }

    @Test
    void arrayImageValidatorShouldReturnTrueWhenArrayContainsNullFilesButOthersValid() {
        MultipartFile file1 = mock(MultipartFile.class);
        when(file1.getContentType()).thenReturn("image/gif");

        MultipartFile[] files = new MultipartFile[] {file1, null};

        assertTrue(arrayImageValidator.isValid(files, context));
    }

    @Test
    void arrayImageValidatorShouldReturnFalseWhenAnyFileHasInvalidMimeType() {
        MultipartFile validFile = mock(MultipartFile.class);
        MultipartFile invalidFile = mock(MultipartFile.class);
        when(validFile.getContentType()).thenReturn("image/jpeg");
        when(invalidFile.getContentType()).thenReturn("text/plain");

        MultipartFile[] files = new MultipartFile[] {validFile, invalidFile};

        assertFalse(arrayImageValidator.isValid(files, context));
    }

    @Test
    void arrayImageValidatorShouldReturnFalseWhenAnyFileHasNullMimeType() {
        MultipartFile fileWithNullMimeType = mock(MultipartFile.class);
        MultipartFile validFile = mock(MultipartFile.class);
        when(fileWithNullMimeType.getContentType()).thenReturn(null);

        MultipartFile[] files = new MultipartFile[] {fileWithNullMimeType, validFile};

        assertFalse(arrayImageValidator.isValid(files, context));
    }

    @Test
    void arrayImageValidatorShouldReturnTrueWhenFilesArrayIsEmpty() {
        MultipartFile[] files = new MultipartFile[] {};
        assertTrue(arrayImageValidator.isValid(files, context));
    }
}
