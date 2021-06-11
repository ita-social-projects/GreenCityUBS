package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.EmployeeConstraintException;
import greencity.exceptions.EmployeeNotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UBSEmployeeServiceImpl implements UBSEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ReceivingStationRepository stationRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    private final PositionRepository positionRepository;
    @Value("${DEFAULT_IMAGE_URL}")
    private String defaultImagePath;

    @Override
    public EmployeeDto save(AddEmployeeDto dto, MultipartFile image) {
        Employee employee = modelMapper.map(dto, Employee.class);
        if (image != null) {
            employee.setImagePath(fileService.upload(image));
        }
        else {
            employee.setImagePath(defaultImagePath);
        }
        return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
    }

    @Override
    public PageableAdvancedDto<EmployeeDto> findAll(Pageable pageable) {
        return buildPageableAdvancedDto(employeeRepository.findAll(pageable));
    }

    @Override
    public EmployeeDto update(EmployeeDto dto) {
        if (employeeRepository.existsById(dto.getId())) {
            Employee employee = modelMapper.map(dto, Employee.class);
            return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
        }
        throw new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId());
    }

    @Override
    public void delete(EmployeeDto dto) {
        if (employeeRepository.existsById(dto.getId())) {
            employeeRepository.deleteById(dto.getId());
        }
        throw new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId());
    }
    private PageableAdvancedDto<EmployeeDto> buildPageableAdvancedDto(Page<Employee> employeePage) {
        List<EmployeeDto> employeeDtos = employeePage.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .collect(Collectors.toList());

        return new PageableAdvancedDto<>(
                employeeDtos,
                employeePage.getTotalElements(),
                employeePage.getPageable().getPageNumber(),
                employeePage.getTotalPages(),
                employeePage.getNumber(),
                employeePage.hasPrevious(),
                employeePage.hasNext(),
                employeePage.isFirst(),
                employeePage.isLast());
    }
}
