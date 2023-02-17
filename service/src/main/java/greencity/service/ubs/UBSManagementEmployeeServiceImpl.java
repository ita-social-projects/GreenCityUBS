package greencity.service.ubs;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.UpdateEmployeeAuthoritiesDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.repository.EmployeeCriteriaRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Data
public class UBSManagementEmployeeServiceImpl implements UBSManagementEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final ReceivingStationRepository stationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final UserRemoteClient userRemoteClient;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    private String defaultImagePath = AppConstant.DEFAULT_IMAGE;
    private final EmployeeCriteriaRepository employeeCriteriaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeWithTariffsDto save(EmployeeDto dto, MultipartFile image) {
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPosition(dto.getEmployeePositions());

        Employee employee = modelMapper.map(dto, Employee.class);
        employee.setUuid(UUID.randomUUID().toString());
        employee.setTariffInfos(tariffsInfoRepository.findTariffsInfosByIdIsIn(dto.getTariffId()));
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
        if (image != null) {
            employee.setImagePath(fileService.upload(image));
        } else {
            employee.setImagePath(defaultImagePath);
        }
        signUpEmployee(employee);
        return modelMapper.map(employeeRepository.save(employee), EmployeeWithTariffsDto.class);
    }

    private void signUpEmployee(Employee employee) {
        EmployeeSignUpDto signUpDto = EmployeeSignUpDto.builder()
            .email(employee.getEmail())
            .name(employee.getFirstName() + employee.getLastName())
            .uuid(employee.getUuid())
            .positions(employee.getEmployeePosition().stream()
                .map(position -> PositionDto.builder()
                    .id(position.getId())
                    .name(position.getName())
                    .build())
                .collect(Collectors.toList()))
            .isUbs(true)
            .build();
        try {
            userRemoteClient.signUpEmployee(signUpDto);
        } catch (HystrixRuntimeException e) {
            throw new BadRequestException("User with this email already exists: " + signUpDto.getEmail());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GetEmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        Page<Employee> employees = employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria);
        List<GetEmployeeDto> employeeDtos = employees.stream()
            .map(employee -> modelMapper.map(employee, GetEmployeeDto.class)).collect(Collectors.toList());
        return new PageImpl<>(employeeDtos, employees.getPageable(), employees.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GetEmployeeDto> findAllActiveEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        Page<Employee> employees =
            employeeCriteriaRepository.findAllActiveEmployees(employeePage, employeeFilterCriteria);
        List<GetEmployeeDto> employeesDto = employees.stream()
            .map(employee -> modelMapper.map(employee, GetEmployeeDto.class)).collect(Collectors.toList());
        return new PageImpl<>(employeesDto, employees.getPageable(), employees.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmployeeWithTariffsDto update(EmployeeDto dto, MultipartFile image) {
        final Employee upEmployee = employeeRepository.findById(dto.getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId()));

        if (!employeeRepository.findEmployeesByEmailAndIdNot(dto.getEmail(), dto.getId()).isEmpty()) {
            throw new BadRequestException("Email already exist in another employee: " + dto.getEmail());
        }
        checkValidPosition(dto.getEmployeePositions());
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        updateEmployeeEmail(dto, upEmployee.getUuid());
        updateEmployeeAuthorities(dto);

        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        updatedEmployee.setTariffInfos(tariffsInfoRepository.findTariffsInfosByIdIsIn(dto.getTariffId()));
        updatedEmployee.setUuid(upEmployee.getUuid());
        updatedEmployee.setEmployeeStatus(upEmployee.getEmployeeStatus());

        if (image != null) {
            updatedEmployee.setImagePath(fileService.upload(image));
        } else {
            updatedEmployee.setImagePath(upEmployee.getImagePath());
        }
        return modelMapper.map(employeeRepository.save(updatedEmployee), EmployeeWithTariffsDto.class);
    }

    @Override
    @Transactional
    public PositionDto update(PositionDto dto) {
        if (!positionRepository.existsById(dto.getId())) {
            throw new NotFoundException(ErrorMessage.POSITION_NOT_FOUND_BY_ID + dto.getId());
        }
        if (!positionRepository.existsPositionByName(dto.getName())) {
            Position position = modelMapper.map(dto, Position.class);
            return modelMapper.map(positionRepository.save(position), PositionDto.class);
        }
        throw new UnprocessableEntityException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getName());
    }

    private void updateEmployeeAuthorities(EmployeeDto dto) {
        UpdateEmployeeAuthoritiesDto authoritiesDto =
            UpdateEmployeeAuthoritiesDto.builder()
                .email(dto.getEmail())
                .positions(dto.getEmployeePositions())
                .build();
        userRemoteClient.updateAuthorities(authoritiesDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
        if (employee.getEmployeeStatus().equals(EmployeeStatus.ACTIVE)) {
            employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
            try {
                userRemoteClient.deactivateEmployee(employee.getUuid());
            } catch (HystrixRuntimeException e) {
                throw new BadRequestException("Employee with current uuid doesn't exist: " + employee.getUuid());
            }
            employeeRepository.save(employee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteEmployeeImage(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
        if (!employee.getImagePath().equals(defaultImagePath)) {
            fileService.delete(employee.getImagePath());
            employee.setImagePath(defaultImagePath);
            employeeRepository.save(employee);
        } else {
            throw new UnprocessableEntityException(ErrorMessage.CANNOT_DELETE_DEFAULT_IMAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PositionDto create(AddingPositionDto dto) {
        if (!positionRepository.existsPositionByName(dto.getName())) {
            Position position = positionRepository.save(buildPosition(dto));
            return modelMapper.map(position, PositionDto.class);
        }
        throw new UnprocessableEntityException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getName());
    }

    private Position buildPosition(AddingPositionDto dto) {
        return Position.builder()
            .name(dto.getName())
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PositionDto> getAllPositions() {
        return positionRepository.findAll().stream()
            .map(p -> modelMapper.map(p, PositionDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePosition(Long id) {
        Position position = positionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.POSITION_NOT_FOUND_BY_ID + id));
        if (position.getEmployees() == null || position.getEmployees().isEmpty()) {
            positionRepository.delete(position);
        } else {
            throw new UnprocessableEntityException(ErrorMessage.EMPLOYEES_ASSIGNED_POSITION);
        }
    }

    private void updateEmployeeEmail(EmployeeDto dto, String uuid) {
        Employee employee = employeeRepository.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId()));
        String oldEmail = employee.getEmail();
        String newEmail = dto.getEmail();
        if (!oldEmail.equals(newEmail)) {
            try {
                userRemoteClient.updateEmployeeEmail(newEmail, uuid);
            } catch (HystrixRuntimeException e) {
                throw new BadRequestException("User with this email already exists");
            }
        }
    }

    private void checkValidPosition(List<PositionDto> positions) {
        if (!existPositions(positions)) {
            throw new NotFoundException(ErrorMessage.POSITION_NOT_FOUND);
        }
    }

    private boolean existPositions(List<PositionDto> positions) {
        return positions.stream()
            .allMatch(p -> positionRepository.existsPositionByIdAndName(p.getId(), p.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GetTariffInfoForEmployeeDto> getTariffsForEmployee() {
        List<TariffsInfo> tariffs = tariffsInfoRepository.findAll();
        return tariffs
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffInfoForEmployeeDto.class))
            .collect(Collectors.toList());
    }
}
