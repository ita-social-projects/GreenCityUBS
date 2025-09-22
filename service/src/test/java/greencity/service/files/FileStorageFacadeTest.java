package greencity.service.files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import greencity.repository.EmployeeRepository;
import greencity.repository.MessageAssetRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.ViolationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileStorageFacadeTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ViolationRepository violationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private MessageAssetRepository messageAssetRepository;
    @InjectMocks
    private FileStorageFacade fileStorageFacade;

    @Test
    void getFilePathsTest(){
        when(paymentRepository.findDistinctImagePaths()).thenReturn(List.of("payment1.png", "payment2.png"));
        when(violationRepository.findDistinctImagePaths()).thenReturn(List.of("violation1.png", "violation1.png"));
        when(employeeRepository.findDistinctImagePaths()).thenReturn(Arrays.asList("employee1.png", "employee2.png"));
        when(orderRepository.findImagePaths()).thenReturn(Arrays.asList(
                List.of("order1.png", "order2.png"),
                null,
                List.of("order2.png")));
        when(messageAssetRepository.findDistinctImagePaths()).thenReturn(List.of("message1.png", "message1.png"));

        List<String> filePaths = fileStorageFacade.getFilePaths();

        assertThat(filePaths).containsExactlyInAnyOrder("payment1.png", "payment2.png",
                "violation1.png", "employee1.png", "employee2.png",
                "order1.png", "order2.png", "message1.png");
    }
}
