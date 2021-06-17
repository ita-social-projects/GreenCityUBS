package greencity.service;

import static greencity.ModelUtils.*;

import greencity.constant.ErrorMessage;
import greencity.dto.AddEmployeeDto;
import greencity.dto.AddingPositionDto;
import greencity.dto.EmployeeDto;
import greencity.dto.PositionDto;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.exceptions.EmployeeNotFoundException;
import greencity.exceptions.EmployeeValidationException;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.service.ubs.FileService;
import greencity.service.ubs.UBSManagementEmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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
    @InjectMocks
    private UBSManagementEmployeeServiceImpl employeeService;

    @Test
    void save() {
        Employee employee = getEmployee();
        employee.setId(null);
        when(phoneFormatter.getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(getAddEmployeeDto().getPhoneNumber(), getAddEmployeeDto().getPhoneNumber());
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber())).thenReturn(false, true, false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false, true);
        when(modelMapper.map(any(), any())).thenReturn(employee, getEmployeeDto());
        when(repository.save(any())).thenReturn(getEmployee());

        EmployeeDto result = employeeService.save(getAddEmployeeDto(), null);

        assertEquals(1L, result.getId());

        verify(phoneFormatter, times(1))
            .getE164PhoneNumberFormat(getAddEmployeeDto().getPhoneNumber());
        verify(fileService, never()).upload(null);
        verify(repository, times(1)).existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber());
        verify(repository, times(1)).existsByEmail(getAddEmployeeDto().getEmail());
        verify(modelMapper, times(2)).map(any(), any());
        verify(repository, times(1)).save(any());

        AddEmployeeDto employeeDto = getAddEmployeeDto();
        employeeDto.setEmail("test@gmail.com");

        Exception thrown = assertThrows(EmployeeValidationException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + employeeDto.getPhoneNumber());
        Exception thrown1 = assertThrows(EmployeeValidationException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown1.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + employeeDto.getEmail());

    }

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(1, 2);

        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1L));

        employeeService.findAll(pageable);

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void update() {
        when(phoneFormatter.getE164PhoneNumberFormat(anyString())).thenReturn(getEmployeeDto().getPhoneNumber());
        when(modelMapper.map(any(), any())).thenReturn(getEmployee(), getEmployeeDto());
        when(repository.existsById(any())).thenReturn(true, true, true, false);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong()))
            .thenReturn(null, getEmployee(), null);
        when(repository.checkIfEmailUnique(anyString(), anyLong()))
            .thenReturn(null, getEmployee());

        employeeService.update(getEmployeeDto());

        verify(repository, times(1)).save(any());
        verify(repository, times(1)).checkIfEmailUnique(anyString(), anyLong());
        verify(repository, times(1)).checkIfPhoneNumberUnique(anyString(), anyLong());

        Exception thrown = assertThrows(EmployeeValidationException.class,
            () -> employeeService.update(getEmployeeDto()));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + getEmployeeDto().getPhoneNumber());
        Exception thrown1 = assertThrows(EmployeeValidationException.class,
            () -> employeeService.update(getEmployeeDto()));
        assertEquals(thrown1.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + getEmployeeDto().getEmail());
        Exception thrown2 = assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.update(getEmployeeDto()));
        assertEquals(thrown2.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + getEmployeeDto().getId());
    }

    @Test
    void delete() {
        when(repository.existsById(anyLong())).thenReturn(true, false);
        doNothing().when(repository).deleteById(1L);

        employeeService.deleteEmployee(1L);

        verify(repository, times(1)).deleteById(1L);

        Exception thrown = assertThrows(EmployeeNotFoundException.class,
            () -> employeeService.deleteEmployee(1L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 1L);
    }
    @Test
    void createPosition() {
        when(positionRepository.existsPositionByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(Position.class), eq(PositionDto.class))).thenReturn(getPositionDto());
        when(positionRepository.save(any())).thenReturn(getPosition());

        employeeService.create(AddingPositionDto.builder().name("Петрівка").build());

        verify(positionRepository, times(1)).existsPositionByName(any());
        verify(positionRepository, times(1)).save(any());
        verify(modelMapper, times(1)).map(any(Position.class), eq(PositionDto.class));

    }
}