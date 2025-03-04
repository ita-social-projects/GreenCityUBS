package greencity.controller;

import greencity.configuration.SecurityConfig;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.ubs.PdfExporterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.security.Principal;
import java.util.Locale;
import java.util.UUID;
import static greencity.ModelUtils.getUuid;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class OrderPdfExporterControllerTest {

    @Mock
    private PdfExporterService pdfExporterService;
    @InjectMocks
    private OrderPdfExporterController controller;

    private MockMvc mockMvc;
    private final Principal principal = getUuid();

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    private static final String ubsLink = "/ubs/order/pdf/export";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setCustomArgumentResolvers(
            new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
            .build();
    }

    @Test
    void exportTest() throws Exception {
        long id = 1L;
        Locale locale = Locale.ENGLISH;
        String uuid = UUID.randomUUID().toString();
        mockMvc.perform(get(ubsLink)
            .param("id", "1")
            .param("lang", "en")
            .param("userUuid", uuid)
            .principal(principal))
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk());
        verify(pdfExporterService, times(1)).exportById(id, locale, uuid);
    }
}