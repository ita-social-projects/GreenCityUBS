package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.exceptions.*;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.FileService;
import greencity.service.ubs.UBSManagementEmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UBSManagementEmployeeServiceImplTest {
    @Mock
    private EmployeeRepository repository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private ReceivingStationRepository stationRepository;
    @Mock
    private FileService fileService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PhoneNumberFormatterService phoneFormatter;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UBSManagementEmployeeServiceImpl employeeService;

    @Test
    void saveEmployee() {
        Employee employee = getEmployee();
        employee.setId(null);
        when(phoneFormatter.getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(getAddEmployeeDto().getPhoneNumber());
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber())).thenReturn(false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(modelMapper.map(any(), any())).thenReturn(employee, getEmployeeDto());
        when(repository.save(any())).thenReturn(getEmployee());
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(true);
        AddEmployeeDto addEmployeeDto = getAddEmployeeDto();
        addEmployeeDto.setUserId(UserDto.builder().id(1L).build());
        when(userRepository.existsById(1L)).thenReturn(true);
        EmployeeDto result = employeeService.save(addEmployeeDto, null);
        assertEquals(1L, result.getId());
        verify(phoneFormatter, times(1))
            .getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber());
        verify(fileService, never()).upload(null);
        verify(repository, times(1)).existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber());
        verify(repository, times(1)).existsByEmail(getAddEmployeeDto().getEmail());
        verify(modelMapper, times(2)).map(any(), any());
        verify(repository, times(1)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
        verify(stationRepository, atLeastOnce()).existsReceivingStationByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeAlreadyExistException() {
        Employee employee = getEmployee();
        employee.setId(null);
        when(phoneFormatter.getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(getAddEmployeeDto().getPhoneNumber());
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber())).thenReturn(false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(modelMapper.map(any(), any())).thenReturn(employee, getEmployeeDto());
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(true);
        AddEmployeeDto addEmployeeDto = getAddEmployeeDto();
        addEmployeeDto.setUserId(UserDto.builder().id(1L).build());
        when(userRepository.existsById(1L)).thenReturn(false);
        Exception thrown = assertThrows(EmployeeAlreadyExist.class,
            () -> employeeService.save(addEmployeeDto, null));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_ALREADY_EXIST);

        when(phoneFormatter.getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(getAddEmployeeDto().getPhoneNumber());
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber())).thenReturn(false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(modelMapper.map(any(), any())).thenReturn(employee, getEmployeeDto());
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(repository.existsByUserId(1L)).thenReturn(true);
        Exception thrown1 = assertThrows(EmployeeAlreadyExist.class,
            () -> employeeService.save(addEmployeeDto, null));
        assertEquals(thrown1.getMessage(), ErrorMessage.EMPLOYEE_ALREADY_EXIST);
    }

    @Test
    void saveEmployeeShouldThrowException() {
        Employee employee = getEmployee();
        employee.setId(null);
        AddEmployeeDto employeeDto = getAddEmployeeDto();
        employeeDto.setEmail("test@gmail.com");
        when(phoneFormatter.getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(getAddEmployeeDto().getPhoneNumber());
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(true, false, false, false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(true, false, false);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(false, true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(false);

        Exception thrown = assertThrows(EmployeeValidationException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + employeeDto.getPhoneNumber());
        Exception thrown1 = assertThrows(EmployeeValidationException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown1.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + employeeDto.getEmail());
        Exception thrown3 = assertThrows(PositionNotFoundException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown3.getMessage(), ErrorMessage.POSITION_NOT_FOUND);
        Exception thrown4 = assertThrows(ReceivingStationNotFoundException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown4.getMessage(), ErrorMessage.RECEIVING_STATION_NOT_FOUND);
    }

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(1, 2);

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1L));

        employeeService.findAll(pageable);

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void updateEmployee() {
        when(phoneFormatter.getE164PhoneNumberFormat(anyString())).thenReturn(getEmployeeDto().getPhoneNumber());
        when(modelMapper.map(any(), any())).thenReturn(getEmployee(), getEmployeeDto());
        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong()))
            .thenReturn(null);
        when(repository.checkIfEmailUnique(anyString(), anyLong()))
            .thenReturn(null);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(getEmployee()));

        employeeService.update(getEmployeeDto(), null);

        verify(repository, times(1)).save(any());
        verify(repository, times(1)).checkIfEmailUnique(anyString(), anyLong());
        verify(repository, times(1)).checkIfPhoneNumberUnique(anyString(), anyLong());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
        verify(stationRepository, atLeastOnce()).existsReceivingStationByIdAndName(any(), any());
    }

    @Test
    void updateEmployeeShouldThrowExceptions() {
        when(phoneFormatter.getE164PhoneNumberFormat(anyString())).thenReturn(getEmployeeDto().getPhoneNumber());
        when(repository.existsById(any())).thenReturn(false, true, true, true, true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong()))
            .thenReturn(getEmployee(), null, null, null);
        when(repository.checkIfEmailUnique(anyString(), anyLong()))
            .thenReturn(getEmployee(), null, null);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(false, true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(false);

        Exception thrown2 = assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.update(getEmployeeDto(), null));
        assertEquals(thrown2.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + getEmployeeDto().getId());
        Exception thrown = assertThrows(EmployeeValidationException.class,
            () -> employeeService.update(getEmployeeDto(), null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + getEmployeeDto().getPhoneNumber());
        Exception thrown1 = assertThrows(EmployeeValidationException.class,
            () -> employeeService.update(getEmployeeDto(), null));
        assertEquals(thrown1.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + getEmployeeDto().getEmail());
        Exception thrown3 = assertThrows(PositionNotFoundException.class,
            () -> employeeService.update(getEmployeeDto(), null));
        assertEquals(thrown3.getMessage(), ErrorMessage.POSITION_NOT_FOUND);
        Exception thrown4 = assertThrows(ReceivingStationNotFoundException.class,
            () -> employeeService.update(getEmployeeDto(), null));
        assertEquals(thrown4.getMessage(), ErrorMessage.RECEIVING_STATION_NOT_FOUND);
    }

    @Test
    void delete() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        when(repository.findById(1L)).thenReturn(Optional.of(employee), Optional.empty());
        doNothing().when(repository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(repository, times(1)).deleteById(1L);
        verify(fileService, times(1)).delete(employee.getImagePath());

        Exception thrown = assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.deleteEmployee(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 1L);
    }

    @Test
    void createPosition() {
        AddingPositionDto addingPositionDto = AddingPositionDto.builder().name("Водій").build();
        when(positionRepository.existsPositionByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(Position.class), eq(PositionDto.class))).thenReturn(getPositionDto());
        when(positionRepository.save(any())).thenReturn(getPosition(), getPosition());

        employeeService.create(addingPositionDto);

        verify(positionRepository, times(1)).existsPositionByName(any());
        verify(positionRepository, times(1)).save(any());
        verify(modelMapper, times(1)).map(any(Position.class), eq(PositionDto.class));

        Exception thrown = assertThrows(PositionValidationException.class,
            () -> employeeService.create(addingPositionDto));
        assertEquals(thrown.getMessage(), ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS
            + addingPositionDto.getName());
    }

    @Test
    void updatePosition() {
        PositionDto dto = getPositionDto();
        when(positionRepository.existsById(dto.getId())).thenReturn(true, true, false);
        when(positionRepository.existsPositionByName(dto.getName())).thenReturn(false, true);
        when(modelMapper.map(any(), any())).thenReturn(getPosition(), dto);

        employeeService.update(dto);

        verify(positionRepository, times(1)).existsById(dto.getId());
        verify(positionRepository, times(1)).existsPositionByName(dto.getName());
        verify(modelMapper, times(2)).map(any(), any());

        Exception thrown = assertThrows(PositionValidationException.class,
            () -> employeeService.update(dto));
        Exception thrown1 = assertThrows(PositionNotFoundException.class,
            () -> employeeService.update(dto));

        assertEquals(thrown1.getMessage(), ErrorMessage.POSITION_NOT_FOUND_BY_ID + dto.getId());
        assertEquals(thrown.getMessage(), ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS
            + dto.getName());
    }

    @Test
    void getAllPosition() {
        when(positionRepository.findAll()).thenReturn(List.of(getPosition()));
        when(modelMapper.map(any(), any())).thenReturn(getPositionDto());

        List<PositionDto> positionDtos = employeeService.getAllPositions();

        assertEquals(1, positionDtos.size());

        verify(positionRepository, times(1)).findAll();
    }

    @Test
    void deletePosition() {
        Position position = getPosition();
        when(positionRepository.findById(1L)).thenReturn(Optional.of(position));

        employeeService.deletePosition(1L);

        verify(positionRepository, times(1)).findById(1L);
        verify(positionRepository, times(1)).delete(position);

        position.setEmployees(Set.of(getEmployee()));
        Exception thrown = assertThrows(EmployeeIllegalOperationException.class,
            () -> employeeService.deletePosition(1L));

        when(positionRepository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(PositionNotFoundException.class,
            () -> employeeService.deletePosition(2L));

        assertEquals(thrown1.getMessage(), ErrorMessage.POSITION_NOT_FOUND_BY_ID + 2L);
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEES_ASSIGNED_POSITION);
    }

    @Test
    void CreateReceivingStation() {
        AddingReceivingStationDto stationDto = AddingReceivingStationDto.builder().name("Петрівка").build();
        when(stationRepository.existsReceivingStationByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(ReceivingStation.class), eq(ReceivingStationDto.class)))
            .thenReturn(getReceivingStationDto());
        when(stationRepository.save(any())).thenReturn(getReceivingStation(), getReceivingStation());

        employeeService.create(stationDto);

        verify(stationRepository, times(1)).existsReceivingStationByName(any());
        verify(stationRepository, times(1)).save(any());
        verify(modelMapper, times(1))
            .map(any(ReceivingStation.class), eq(ReceivingStationDto.class));

        Exception thrown = assertThrows(ReceivingStationValidationException.class,
            () -> employeeService.create(stationDto));
        assertEquals(thrown.getMessage(), ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS
            + stationDto.getName());
    }

    @Test
    void updateReceivingStation() {
        ReceivingStationDto stationDto = getReceivingStationDto();
        when(stationRepository.existsById(stationDto.getId())).thenReturn(true, true, false);
        when(stationRepository.existsReceivingStationByName(stationDto.getName()))
            .thenReturn(false, true);
        when(modelMapper.map(any(), any())).thenReturn(getReceivingStation(), stationDto);

        employeeService.update(stationDto);

        verify(stationRepository, times(1)).existsById(stationDto.getId());
        verify(stationRepository, times(1)).existsReceivingStationByName(stationDto.getName());
        verify(modelMapper, times(2)).map(any(), any());

        Exception thrown = assertThrows(ReceivingStationValidationException.class,
            () -> employeeService.update(stationDto));
        Exception thrown1 = assertThrows(ReceivingStationNotFoundException.class,
            () -> employeeService.update(stationDto));

        assertEquals(thrown1.getMessage(), ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + stationDto.getId());
        assertEquals(thrown.getMessage(), ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS
            + stationDto.getName());
    }

    @Test
    void getAllReceivingStation() {
        when(stationRepository.findAll()).thenReturn(List.of(getReceivingStation()));
        when(modelMapper.map(any(), any())).thenReturn(getReceivingStationDto());

        List<ReceivingStationDto> stationDtos = employeeService.getAllReceivingStation();

        assertEquals(1, stationDtos.size());

        verify(stationRepository, times(1)).findAll();
    }

    @Test
    void deleteReceivingStation() {
        ReceivingStation station = getReceivingStation();
        when(stationRepository.findById(1L)).thenReturn(Optional.of(station));

        employeeService.deleteReceivingStation(1L);

        verify(stationRepository, times(1)).findById(1L);
        verify(stationRepository, times(1)).delete(station);

        station.setEmployees(Set.of(getEmployee()));
        Exception thrown = assertThrows(EmployeeIllegalOperationException.class,
            () -> employeeService.deleteReceivingStation(1L));

        when(stationRepository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(ReceivingStationNotFoundException.class,
            () -> employeeService.deleteReceivingStation(2L));

        assertEquals(thrown1.getMessage(), ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + 2L);
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEES_ASSIGNED_STATION);
    }

    @Test
    void deleteEmployeeImage() {
        Employee employee = getEmployee();
        employee.setImagePath("path");
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));

        employeeService.deleteEmployeeImage(anyLong());

        verify(repository, times(1)).findById(anyLong());
        verify(fileService, times(1)).delete("path");
        verify(repository, times(1)).save(employee);
    }

    @Test
    void deleteEmployeeImageShouldThrowExceptions() {
        Employee employee = getEmployee();
        employee.setImagePath(AppConstant.DEFAULT_IMAGE);
        when(repository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.deleteEmployeeImage(2L));
        assertEquals(thrown1.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);

        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        Exception thrown2 = assertThrows(EmployeeIllegalOperationException.class,
            () -> employeeService.deleteEmployeeImage(1L));
        assertEquals(thrown2.getMessage(), ErrorMessage.CANNOT_DELETE_DEFAULT_IMAGE);
    }
}