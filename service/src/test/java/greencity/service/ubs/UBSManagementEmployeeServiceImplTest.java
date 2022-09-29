package greencity.service.ubs;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import greencity.ModelUtils;
import greencity.entity.user.employee.ReceivingStation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.AddEmployeeDto;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.enums.EmployeeStatus;
import greencity.enums.SortingOrder;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.repository.EmployeeCriteriaRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static greencity.ModelUtils.getAddEmployeeDto;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getEmployeeDto;
import static greencity.ModelUtils.getPosition;
import static greencity.ModelUtils.getPositionDto;

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
    @InjectMocks
    private UBSManagementEmployeeServiceImpl employeeService;
    @Mock
    private EmployeeCriteriaRepository employeeCriteriaRepository;

    @Test
    void saveEmployee() {
        Employee employee = getEmployee();
        employee.setId(null);
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber())).thenReturn(false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(modelMapper.map(any(), any())).thenReturn(employee, getEmployeeDto());
        when(repository.save(any())).thenReturn(getEmployee());
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(true);
        AddEmployeeDto addEmployeeDto = getAddEmployeeDto();
        EmployeeDto result = employeeService.save(addEmployeeDto, null);
        assertEquals(1L, result.getId());
        verify(fileService, never()).upload(null);
        verify(repository, times(1)).existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber());
        verify(repository, times(1)).existsByEmail(getAddEmployeeDto().getEmail());
        verify(modelMapper, times(2)).map(any(), any());
        verify(repository, times(1)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
        verify(stationRepository, atLeastOnce()).existsReceivingStationByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeShouldThrowException() {
        Employee employee = getEmployee();
        employee.setId(null);
        AddEmployeeDto employeeDto = getAddEmployeeDto();
        employeeDto.setEmail("test@gmail.com");
        when(repository.existsByPhoneNumber(getAddEmployeeDto().getPhoneNumber()))
            .thenReturn(true, false, false, false);
        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(true, false, false);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(false, true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(false);

        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_PHONE_NUMBER_ALREADY_EXISTS + employeeDto.getPhoneNumber());
        Exception thrown1 = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown1.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + employeeDto.getEmail());
        Exception thrown3 = assertThrows(NotFoundException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(ErrorMessage.POSITION_NOT_FOUND, thrown3.getMessage());
        Exception thrown4 = assertThrows(NotFoundException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(ErrorMessage.RECEIVING_STATION_NOT_FOUND, thrown4.getMessage());
    }

    @Test
    void findAll() {
        EmployeePage employeePage = new EmployeePage();
        EmployeeFilterCriteria employeeFilterCriteria = new EmployeeFilterCriteria();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
            Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));
        when(employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria))
            .thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1l));
        employeeService.findAll(employeePage, employeeFilterCriteria);
        verify(employeeCriteriaRepository, times(1))
            .findAll(employeePage, employeeFilterCriteria);
    }

    @Test
    void updateEmployeeTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        ReceivingStation station = ModelUtils.getReceivingStation();
        Position position = ModelUtils.getPosition();

        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong())).thenReturn(null);
        when(repository.checkIfEmailUnique(anyString(), anyLong())).thenReturn(null);
        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(anyLong(), anyString()))
            .thenReturn(true);

        employeeService.update(dto, null);

        verify(repository, times(1)).save(any());
        verify(repository, times(1)).checkIfEmailUnique(anyString(), anyLong());
        verify(repository, times(1)).checkIfPhoneNumberUnique(anyString(), anyLong());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(position.getId(), position.getName());
        verify(stationRepository, atLeastOnce()).existsReceivingStationByIdAndName(station.getId(),
            station.getName());
    }

    @Test
    void updateEmployeeNotFoundTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        when(repository.existsById(any())).thenReturn(false, true, true, true, true);
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeePhoneAlreadyExistsTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong())).thenReturn(getEmployee(), null, null);
        assertThrows(UnprocessableEntityException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeeEmailAlreadyExistsTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong())).thenReturn(null);
        when(repository.checkIfEmailUnique(anyString(), anyLong())).thenReturn(getEmployee(), null, null);
        assertThrows(UnprocessableEntityException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeePositionNotFoundTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong())).thenReturn(null);
        when(repository.checkIfEmailUnique(anyString(), anyLong())).thenReturn(null);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(false, true);
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeeStationNotFoundTest() {
        EmployeeDto dto = ModelUtils.getEmployeeDtoWithReceivingStations();
        Position position = ModelUtils.getPosition();
        when(repository.existsById(any())).thenReturn(true);
        when(repository.checkIfPhoneNumberUnique(anyString(), anyLong())).thenReturn(null);
        when(repository.checkIfEmailUnique(anyString(), anyLong())).thenReturn(null);
        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(stationRepository.existsReceivingStationByIdAndName(any(), any())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void delete() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        System.out.println(employee.getEmployeeStatus());
        when(repository.findById(1L)).thenReturn(Optional.ofNullable(employee));
        employeeService.deleteEmployee(1L);
        verify(repository).findById(1L);
        System.out.println(employee.getEmployeeStatus());
        assertEquals(EmployeeStatus.INACTIVE, employee.getEmployeeStatus());
        Exception thrown = assertThrows(NotFoundException.class,
            () -> employeeService.deleteEmployee(2L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);
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

        Exception thrown = assertThrows(UnprocessableEntityException.class,
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

        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.update(dto));
        Exception thrown1 = assertThrows(NotFoundException.class,
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
        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.deletePosition(1L));

        when(positionRepository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(NotFoundException.class,
            () -> employeeService.deletePosition(2L));

        assertEquals(ErrorMessage.POSITION_NOT_FOUND_BY_ID + 2L, thrown1.getMessage());
        assertEquals(ErrorMessage.EMPLOYEES_ASSIGNED_POSITION, thrown.getMessage());
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

        Exception thrown1 = assertThrows(NotFoundException.class,
            () -> employeeService.deleteEmployeeImage(2L));
        assertEquals(thrown1.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);

        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        Exception thrown2 = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.deleteEmployeeImage(1L));
        assertEquals(ErrorMessage.CANNOT_DELETE_DEFAULT_IMAGE, thrown2.getMessage());
    }

    @Test
    void findAllActiveEmployees() {
        EmployeePage employeePage = new EmployeePage();
        EmployeeFilterCriteria employeeFilterCriteria = new EmployeeFilterCriteria();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
            Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));
        when(employeeCriteriaRepository.findAllActiveEmployees(employeePage, employeeFilterCriteria))
            .thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1L));
        employeeService.findAllActiveEmployees(employeePage, employeeFilterCriteria);
        verify(employeeCriteriaRepository, times(1))
            .findAllActiveEmployees(employeePage, employeeFilterCriteria);
    }
}
