package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.EmployeeNotFoundException;
import greencity.exceptions.EmployeeValidationException;
import greencity.repository.EmployeeRepository;
import greencity.service.PhoneNumberFormatterService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UBSManagementEmployeeServiceImpl implements UBSManagementEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    private final PhoneNumberFormatterService phoneFormatter;
    @Value("${employee.default.image.path}")
    private String defaultImagePath;

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeDto save(AddEmployeeDto dto, MultipartFile image) {
        dto.setPhoneNumber(phoneFormatter.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
        }
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        Employee employee = modelMapper.map(dto, Employee.class);
        if (image != null && image.getSize() < 10_000_000L) {
            employee.setImagePath(fileService.upload(image));
        } else {
            employee.setImagePath(defaultImagePath);
        }
        return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<EmployeeDto> findAll(Pageable pageable) {
        return buildPageableAdvancedDto(employeeRepository.findAll(pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeDto update(EmployeeDto dto) {
        dto.setPhoneNumber(phoneFormatter.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.existsById(dto.getId())) {
            if (employeeRepository.checkIfPhoneNumberUnique(dto.getPhoneNumber(), dto.getId()) != null) {
                throw new EmployeeValidationException(
                    ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
            }
            if (dto.getEmail() != null
                && employeeRepository.checkIfEmailUnique(dto.getEmail(), dto.getId()) != null) {
                throw new EmployeeValidationException(
                    ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
            }
            Employee employee = modelMapper.map(dto, Employee.class);
            return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
        }
        throw new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
        } else {
            throw new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PositionDto create(AddingPositionDto dto) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PositionDto update(PositionDto dto) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PositionDto> getAllPositions() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePosition(Long id) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceivingStationDto create(AddingReceivingStationDto dto) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceivingStationDto update(ReceivingStationDto dto) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReceivingStationDto> getAllReceivingStation() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteReceivingStation(Long id) {

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
