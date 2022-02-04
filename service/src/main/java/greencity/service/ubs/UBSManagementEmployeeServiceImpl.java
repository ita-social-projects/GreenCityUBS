package greencity.service.ubs;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.EmployeeStatus;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.exceptions.*;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.repository.EmployeeCriteriaRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.service.UAPhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UBSManagementEmployeeServiceImpl implements UBSManagementEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final ReceivingStationRepository stationRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;
    private String defaultImagePath = AppConstant.DEFAULT_IMAGE;
    private final EmployeeCriteriaRepository employeeCriteriaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeDto save(AddEmployeeDto dto, MultipartFile image) {
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
        }
        if (dto.getEmail() != null && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPositionAndReceivingStation(dto.getEmployeePositions(), dto.getReceivingStations());
        Employee employee = modelMapper.map(dto, Employee.class);
        if (image != null) {
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
        List<EmployeeDto> employeeDtos = employees.stream()
            .map(employee -> modelMapper.map(employee, EmployeeDto.class)).collect(Collectors.toList());
        return new PageImpl<>(employeeDtos, employees.getPageable(), employees.getTotalElements());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmployeeDto update(EmployeeDto dto, MultipartFile image) {
        if (!employeeRepository.existsById(dto.getId())) {
            throw new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getId());
        }
        dto.setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getPhoneNumber()));
        if (employeeRepository.checkIfPhoneNumberUnique(dto.getPhoneNumber(), dto.getId()) != null) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + dto.getPhoneNumber());
        }
        if (dto.getEmail() != null
            && employeeRepository.checkIfEmailUnique(dto.getEmail(), dto.getId()) != null) {
            throw new EmployeeValidationException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmail());
        }
        checkValidPositionAndReceivingStation(dto.getEmployeePositions(), dto.getReceivingStations());
        Employee employee = modelMapper.map(dto, Employee.class);
        if (image != null) {
            if (!employee.getImagePath().equals(defaultImagePath)) {
                fileService.delete(employee.getImagePath());
            }
            employee.setImagePath(fileService.upload(image));
        }
        return modelMapper.map(employeeRepository.save(employee), EmployeeDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PositionDto update(PositionDto dto) {
        if (!positionRepository.existsById(dto.getId())) {
            throw new PositionNotFoundException(ErrorMessage.POSITION_NOT_FOUND_BY_ID + dto.getId());
        }
        if (!positionRepository.existsPositionByName(dto.getName())) {
            Position position = modelMapper.map(dto, Position.class);
            return modelMapper.map(positionRepository.save(position), PositionDto.class);
        }
        throw new PositionValidationException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceivingStationDto update(ReceivingStationDto dto) {
        if (!stationRepository.existsById(dto.getId())) {
            throw new ReceivingStationNotFoundException(ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + dto.getId());
        }
        if (!stationRepository.existsReceivingStationByName(dto.getName())) {
            ReceivingStation receivingStation = stationRepository.save(modelMapper.map(dto, ReceivingStation.class));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new ReceivingStationValidationException(
            ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS + dto.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
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
            .orElseThrow(() -> new EmployeeNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
        if (!employee.getImagePath().equals(defaultImagePath)) {
            fileService.delete(employee.getImagePath());
            employee.setImagePath(defaultImagePath);
            employeeRepository.save(employee);
        } else {
            throw new EmployeeIllegalOperationException(ErrorMessage.CANNOT_DELETE_DEFAULT_IMAGE);
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
        throw new PositionValidationException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReceivingStationDto create(AddingReceivingStationDto dto) {
        if (!stationRepository.existsReceivingStationByName(dto.getName())) {
            ReceivingStation receivingStation = stationRepository.save(buildReceivingStation(dto));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new ReceivingStationValidationException(
            ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS + dto.getName());
    }

    private Position buildPosition(AddingPositionDto dto) {
        return Position.builder()
            .name(dto.getName())
            .build();
    }

    private ReceivingStation buildReceivingStation(AddingReceivingStationDto dto) {
        return ReceivingStation.builder()
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
            .orElseThrow(() -> new PositionNotFoundException(ErrorMessage.POSITION_NOT_FOUND_BY_ID + id));
        if (position.getEmployees() == null || position.getEmployees().isEmpty()) {
            positionRepository.delete(position);
        } else {
            throw new EmployeeIllegalOperationException(ErrorMessage.EMPLOYEES_ASSIGNED_POSITION);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ReceivingStationDto> getAllReceivingStation() {
        return stationRepository.findAll().stream()
            .map(r -> modelMapper.map(r, ReceivingStationDto.class))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteReceivingStation(Long id) {
        ReceivingStation station = stationRepository.findById(id)
            .orElseThrow(() -> new ReceivingStationNotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + id));
        if (station.getEmployees() == null || station.getEmployees().isEmpty()) {
            stationRepository.delete(station);
        } else {
            throw new EmployeeIllegalOperationException(ErrorMessage.EMPLOYEES_ASSIGNED_STATION);
        }
    }

    private void checkValidPositionAndReceivingStation(List<PositionDto> positions,
        List<ReceivingStationDto> stations) {
        if (!existPositions(positions)) {
            throw new PositionNotFoundException(ErrorMessage.POSITION_NOT_FOUND);
        }
        if (!existReceivingStation(stations)) {
            throw new ReceivingStationNotFoundException(ErrorMessage.RECEIVING_STATION_NOT_FOUND);
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
}
