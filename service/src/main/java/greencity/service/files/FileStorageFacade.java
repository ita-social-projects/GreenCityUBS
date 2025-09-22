package greencity.service.files;

import greencity.repository.EmployeeRepository;
import greencity.repository.MessageAssetRepository;
import greencity.repository.OrderRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FileStorageFacade {
    private final PaymentRepository paymentRepository;
    private final ViolationRepository violationRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final MessageAssetRepository messageAssetRepository;

    public List<String> getFilePaths() {
        List<String> filePaths = new ArrayList<>();

        filePaths.addAll(paymentRepository.findDistinctImagePaths());
        filePaths.addAll(violationRepository.findDistinctImagePaths());
        filePaths.addAll(employeeRepository.findDistinctImagePaths());
        filePaths.addAll(messageAssetRepository.findDistinctImagePaths());
        filePaths.addAll(orderRepository.findImagePaths()
            .stream()
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList());

        return filePaths.stream().distinct().toList();
    }
}
