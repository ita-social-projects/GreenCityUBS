package greencity.service.ubs;

import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.*;
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
import greencity.repository.*;
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
    public SaveEmployeeDto save(SaveEmployeeDto dto, MultipartFile image) {
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPosition(dto.getEmployeePositions());

        Employee employee = modelMapper.map(dto, Employee.class);
        employee.setUuid(UUID.randomUUID().toString());
        employee.setTariffInfos(tariffsInfoRepository.findTariffsInfoById(dto.getTariffId()));
        if (image != null) {
            employee.setImagePath(fileService.upload(image));
        } else {
            employee.setImagePath(defaultImagePath);
        }
        signUpEmployee(employee);
        return modelMapper.map(employeeRepository.save(employee), SaveEmployeeDto.class);
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
        userRemoteClient.signUpEmployee(signUpDto);
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
    public UpdateEmployeeDto update(UpdateEmployeeDto dto, MultipartFile image) {
        final Employee upEmployee = employeeRepository.findById(dto.getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId()));
        if (!employeeRepository.findEmployeeWithEmailAndNotId(dto.getEmail(), dto.getId()).isEmpty()) {
            throw new BadRequestException("Email already exist in another employee: " + dto.getEmail());
        }
        checkValidPosition(dto.getEmployeePositions());
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        updateEmployeeEmail(dto);
        updateEmployeeAuthorities(dto);

        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        updatedEmployee.setTariffInfos(tariffsInfoRepository.findTariffsInfoById(dto.getTariffId()));
        updatedEmployee.setUuid(upEmployee.getUuid());
        if (image != null) {
            if (!updatedEmployee.getImagePath().equals(defaultImagePath)) {
                fileService.delete(updatedEmployee.getImagePath());
            }
            updatedEmployee.setImagePath(fileService.upload(image));
        } else {
            updatedEmployee.setImagePath(defaultImagePath);
        }
        return modelMapper.map(employeeRepository.save(updatedEmployee), UpdateEmployeeDto.class);
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

    private void updateEmployeeAuthorities(UpdateEmployeeDto dto) {
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
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
        if (employee.getEmployeeStatus().equals(EmployeeStatus.ACTIVE)) {
            employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
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

    private void updateEmployeeEmail(UpdateEmployeeDto dto) {
        Employee employee = employeeRepository.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId()));
        String oldEmail = employee.getEmail();
        String newEmail = dto.getEmail();
        if (!oldEmail.equals(newEmail)) {
            userRemoteClient.updateEmployeeEmail(oldEmail, newEmail);
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
