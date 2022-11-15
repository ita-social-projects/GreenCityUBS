package greencity.service.ubs;

import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.TariffsInfoDto;
import greencity.entity.order.TariffsInfo;
import greencity.enums.EmployeeStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Data
public class UBSManagementEmployeeServiceImpl implements UBSManagementEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
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
    public EmployeeDto save(EmployeeDto dto, MultipartFile image) {
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
        }
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPositionAndReceivingStation(dto.getEmployeePositions(), dto.getReceivingStations());
        setTariffsForEmployeeDto(dto);
        Employee employee = modelMapper.map(dto, Employee.class);
        if (image != null) {
            employee.setImagePath(fileService.upload(image));
        } else {
            employee.setImagePath(defaultImagePath);
        }
        signUpEmployee(employee);
        return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
    }

    private void signUpEmployee(Employee employee) {
        EmployeeSignUpDto signUpDto = EmployeeSignUpDto.builder()
            .email(employee.getEmail())
            .name(employee.getFirstName() + employee.getLastName())
            .isUbs(true)
            .build();
        userRemoteClient.signUpEmployee(signUpDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        Page<Employee> employees = employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria);
        List<EmployeeDto> employeeDtos = employees.stream()
            .map(employee -> modelMapper.map(employee, EmployeeDto.class)).collect(Collectors.toList());
        return new PageImpl<>(employeeDtos, employees.getPageable(), employees.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EmployeeDto> findAllActiveEmployees(EmployeePage employeePage,
        EmployeeFilterCriteria employeeFilterCriteria) {
        Page<Employee> employees =
            employeeCriteriaRepository.findAllActiveEmployees(employeePage, employeeFilterCriteria);
        List<EmployeeDto> employeesDto = employees.stream()
            .map(employee -> modelMapper.map(employee, EmployeeDto.class)).collect(Collectors.toList());
        return new PageImpl<>(employeesDto, employees.getPageable(), employees.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmployeeDto update(EmployeeDto dto, MultipartFile image) {
        if (!employeeRepository.existsById(dto.getId())) {
            throw new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId());
        }
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.existsByPhoneNumberAndId(dto.getPhoneNumber(), dto.getId())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
        }
        if (dto.getEmail() != null
            && employeeRepository.existsByEmailAndId(dto.getEmail(), dto.getId())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPositionAndReceivingStation(dto.getEmployeePositions(), dto.getReceivingStations());
        updateEmployeeEmail(dto);
        setTariffsForEmployeeDto(dto);
        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        if (image != null) {
            if (!updatedEmployee.getImagePath().equals(defaultImagePath)) {
                fileService.delete(updatedEmployee.getImagePath());
            }
            updatedEmployee.setImagePath(fileService.upload(image));
        }
        return modelMapper.map(employeeRepository.save(updatedEmployee), EmployeeDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    private void updateEmployeeEmail(EmployeeDto dto) {
        Employee employee = employeeRepository.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId()));
        String oldEmail = employee.getEmail();
        String newEmail = dto.getEmail();
        userRemoteClient.updateEmployeeEmail(oldEmail, newEmail);
    }

    private void checkValidPositionAndReceivingStation(List<PositionDto> positions,
        List<ReceivingStationDto> stations) {
        if (!existPositions(positions)) {
            throw new NotFoundException(ErrorMessage.POSITION_NOT_FOUND);
        }
        if (!existReceivingStation(stations)) {
            throw new NotFoundException(ErrorMessage.RECEIVING_STATION_NOT_FOUND);
        }
    }

    private boolean existPositions(List<PositionDto> positions) {
        return positions.stream()
            .allMatch(p -> positionRepository.existsPositionByIdAndName(p.getId(), p.getName()));
    }

    private boolean existReceivingStation(List<ReceivingStationDto> stations) {
        return stations.stream()
            .allMatch(s -> stationRepository.existsReceivingStationByIdAndName(s.getId(), s.getName()));
    }

    private void setTariffsForEmployeeDto(EmployeeDto dto) {
        Set<TariffsInfoDto> tariffInfos = new HashSet<>();
        Long courierId = dto.getCourier().getCourierId();
        Long locationId = dto.getLocation().getLocationId();
        TariffsInfo tariff = tariffsInfoRepository.findTariffsInfoLimitsByCourierIdAndLocationId(courierId, locationId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_FOR_LOCATION_NOT_EXIST + locationId));
        tariffInfos.add(modelMapper.map(tariff, TariffsInfoDto.class));
        dto.setTariffs(tariffInfos);
    }
}
