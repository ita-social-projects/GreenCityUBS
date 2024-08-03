package greencity.service.ubs;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.GetEmployeeDto;
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
import greencity.exceptions.user.UserNotFoundException;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.EmployeeCriteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static greencity.ModelUtils.getAddEmployeeDto;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getEmployeeDto;
import static greencity.ModelUtils.getEmployeeDtoWithoutPositionsAndTariffsForGetAllMethod;
import static greencity.ModelUtils.getEmployeeFilterViewListForOneEmployeeWithDifferentPositions;
import static greencity.ModelUtils.getEmployeeForUpdateEmailCheck;
import static greencity.ModelUtils.getEmployeeListForGetAllMethod;
import static greencity.ModelUtils.getEmployeeWithTariffsIdDto;
import static greencity.ModelUtils.getPosition;
import static greencity.ModelUtils.getPositionDto;
import static greencity.ModelUtils.getTariffsInfo;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UBSManagementEmployeeServiceImplTest {
    @Mock
    private EmployeeRepository repository;
    @Mock
    private PositionRepository positionRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private FileService fileService;
    @Mock
    private UserRemoteClient userRemoteClient;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private UBSManagementEmployeeServiceImpl employeeService;
    @Mock
    private EmployeeCriteriaRepository employeeCriteriaRepository;

    @Test
    void saveEmployeeTest() {
        Employee employee = getEmployee();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        MockMultipartFile file = new MockMultipartFile("employeeDto",
            "", "application/json", "random Bytes".getBytes());

        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(repository.save(any())).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        employeeService.save(dto, file);

        verify(repository, times(1)).existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
        verify(repository, times(1)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeTestThrowsNotFoundException() {
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();

        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(repository.existsByEmailAndInactiveStatus(getAddEmployeeDto().getEmail())).thenReturn(true);

        Exception thrown = assertThrows(NotFoundException.class,
            () -> employeeService.save(dto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.EMPLOYEE_NOT_FOUND_BY_EMAIL + dto.getEmployeeDto().getEmail());

        verify(repository, times(1))
            .existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
        verify(repository, times(1))
            .existsByEmailAndInactiveStatus(getAddEmployeeDto().getEmail());
    }

    @Test
    void saveEmployeeTestWithInactiveStatus() {
        Employee employee = getEmployee();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        MockMultipartFile file = new MockMultipartFile("employeeDto",
            "", "application/json", "random Bytes".getBytes());

        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(repository.existsByEmailAndInactiveStatus(getAddEmployeeDto().getEmail())).thenReturn(true);
        when(repository.findByEmail(dto.getEmployeeDto().getEmail())).thenReturn(Optional.of(employee));
        when(repository.findById(dto.getEmployeeDto().getId())).thenReturn(Optional.of(employee));
        when(repository.findEmployeesByEmailAndIdNot(dto.getEmployeeDto().getEmail(), dto.getEmployeeDto().getId()))
            .thenReturn(new ArrayList<>());

        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(repository.save(any())).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        employeeService.save(dto, file);

        verify(repository, times(1))
            .existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
        verify(repository, times(1))
            .existsByEmailAndInactiveStatus(getAddEmployeeDto().getEmail());
        verify(modelMapper, times(2)).map(any(), any());
        verify(repository, times(2)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeWithExistingEmailShouldThrowExceptionTest() {
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(true);
        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.save(dto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.ACTIVE_EMPLOYEE_WITH_CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmployeeDto().getEmail());

        verify(repository).existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
    }

    @Test
    void saveEmployeeWithDefaultImagePathTest() {
        Employee employee = getEmployee();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();

        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(repository.save(any())).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        employeeService.save(dto, null);

        verify(repository, times(1))
            .existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
        verify(repository, times(1)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeShouldThrowExceptionTest() {
        Employee employee = getEmployee();
        employee.setId(null);
        EmployeeWithTariffsIdDto employeeWithTariffsIdDto = getEmployeeWithTariffsIdDto();
        employeeWithTariffsIdDto.getEmployeeDto().setEmail("test@gmail.com");

        when(repository.existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail())).thenReturn(true);
        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.save(employeeWithTariffsIdDto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.ACTIVE_EMPLOYEE_WITH_CURRENT_EMAIL_ALREADY_EXISTS
                + employeeWithTariffsIdDto.getEmployeeDto().getEmail());

        verify(repository).existsByEmailAndActiveStatus(getAddEmployeeDto().getEmail());
    }

    @Test
    void findAllTest() {
        var employeePage = new EmployeePage();
        var employeeFilterCriteria = new EmployeeFilterCriteria();
        var employeeId = 1L;
        var tariffsInfoId = 10L;
        var employeeFilterViews =
            getEmployeeFilterViewListForOneEmployeeWithDifferentPositions(employeeId, tariffsInfoId);
        var expectedGetEmployeeDto = getEmployeeDtoWithoutPositionsAndTariffsForGetAllMethod();
        var expectedEmployeesList = getEmployeeListForGetAllMethod();
        var firstElement = employeeFilterViews.getFirst();

        when(employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria))
            .thenReturn(employeeFilterViews);
        when(repository.findAll()).thenReturn(expectedEmployeesList);
        when(modelMapper.map(firstElement, GetEmployeeDto.class))
            .thenReturn(getEmployeeDto());

        var getPageableDtoGetEmployeeDto =
            employeeService.findAll(employeePage, employeeFilterCriteria);
        var actualGetEmployeeDto = getPageableDtoGetEmployeeDto.getPage().getFirst();

        assertEquals(expectedGetEmployeeDto, actualGetEmployeeDto);
        assertEquals(expectedGetEmployeeDto.getId(), actualGetEmployeeDto.getId());
        assertEquals(expectedGetEmployeeDto.getEmail(), actualGetEmployeeDto.getEmail());
        verify(employeeCriteriaRepository).findAll(employeePage, employeeFilterCriteria);
        verify(modelMapper, times(1)).map(employeeFilterViews.getFirst(), GetEmployeeDto.class);
        verify(repository).findAll();
    }

    @Test
    void updateEmployeeTest() {
        Employee employee = getEmployeeForUpdateEmailCheck();
        Employee retrievedEmployee = getEmployeeForUpdateEmailCheck();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        Position position = getPosition();

        MockMultipartFile file = new MockMultipartFile("employeeDto",
            "", "application/json", "random Bytes".getBytes());

        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(repository.save(any())).thenReturn(employee);
        doNothing().when(fileService).delete(retrievedEmployee.getImagePath());
        when(repository.findById(anyLong())).thenReturn(Optional.of(retrievedEmployee));
        employeeService.update(dto, file);

        verify(modelMapper, times(2)).map(any(), any());
        verify(repository).save(any());
        verify(fileService).delete(eq(retrievedEmployee.getImagePath()));
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(position.getId(), position.getName());
        verify(repository, times(2)).findById(anyLong());
    }

    @Test
    void updateEmployeeWhenPositionDoesNotExistsTest() {
        Employee employee = getEmployeeForUpdateEmailCheck();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        Position position = getPosition();

        MockMultipartFile file = new MockMultipartFile("employeeDto",
            "", "application/json", "random Bytes".getBytes());

        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(false);
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, file));
        verify(positionRepository).existsPositionByIdAndName(position.getId(), position.getName());
        verify(repository).findById(anyLong());
    }

    @Test
    void updateEmployeeWithDefaultImagePathTest() {
        Employee employee = getEmployee();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        Position position = getPosition();

        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        employeeService.update(dto, null);

        verify(modelMapper, times(2)).map(any(), any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(position.getId(), position.getName());
        verify(repository, times(2)).findById(anyLong());
    }

    @Test
    void updateEmployeeNotFoundTest() {
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeeEmailAlreadyExistsTest() {
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        Employee employee = getEmployee();

        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(repository.findEmployeesByEmailAndIdNot(anyString(), anyLong())).thenReturn(List.of(employee));
        assertThrows(BadRequestException.class, () -> employeeService.update(dto, null));

        verify(repository).findById(anyLong());
        verify(repository).findEmployeesByEmailAndIdNot(anyString(), anyLong());
    }

    @Test
    void updateEmployeeEmailThrowsExceptionWhenUserWithSuchEmailIsAlreadyExistsTest() {
        Employee employee = getEmployeeForUpdateEmailCheck();
        EmployeeWithTariffsIdDto dto = getEmployeeWithTariffsIdDto();
        Position position = getPosition();

        MockMultipartFile file = new MockMultipartFile("employeeDto",
            "", "application/json", "random Bytes".getBytes());

        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        doThrow(HystrixRuntimeException.class).when(userRemoteClient)
            .updateEmployeeEmail(dto.getEmployeeDto().getEmail(), null);

        assertThrows(BadRequestException.class, () -> employeeService.update(dto, file));
        verify(positionRepository).existsPositionByIdAndName(position.getId(), position.getName());
        verify(repository, times(2)).findById(anyLong());
        verify(userRemoteClient).updateEmployeeEmail(dto.getEmployeeDto().getEmail(), null);
    }

    @Test
    void deactivateEmployeeTest() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deactivateEmployee(1L);
        verify(repository).findById(1L);
        assertEquals(EmployeeStatus.INACTIVE, employee.getEmployeeStatus());
        Exception thrown = assertThrows(NotFoundException.class,
            () -> employeeService.deactivateEmployee(2L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);
    }

    @Test
    void activateEmployeeTestNotFound() {
        Employee employee = getEmployee();
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        employee.setImagePath("Pass");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.activateEmployee(1L);
        verify(repository).findById(1L);
        assertEquals(EmployeeStatus.ACTIVE, employee.getEmployeeStatus());
        Exception thrown = assertThrows(NotFoundException.class,
            () -> employeeService.deactivateEmployee(2L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);
    }

    @Test
    void activateEmployeeActiveTest() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.activateEmployee(employee.getId());
        assertEquals(EmployeeStatus.ACTIVE, employee.getEmployeeStatus());
    }

    @Test
    void activateEmployeeTest() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.activateEmployee(employee.getId());
        assertEquals(EmployeeStatus.ACTIVE, employee.getEmployeeStatus());
    }

    @Test
    void deactivateEmployeeInactiveTest() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        assertEquals(EmployeeStatus.INACTIVE, employee.getEmployeeStatus());
    }

    @Test
    void deactivateEmployeeHystrixRuntimeExceptionTest() {
        Employee employee = getEmployee();
        long employeeId = employee.getId();
        employee.setImagePath("Pass");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        doThrow(HystrixRuntimeException.class).when(userRemoteClient).deactivateEmployee("Test");
        assertThrows(BadRequestException.class, () -> employeeService.deactivateEmployee(employeeId));

        verify(repository).findById(1L);
    }

    @Test
    void activateEmployeeHystrixRuntimeExceptionTest() {
        Employee employee = getEmployee();
        employee.setEmployeeStatus(EmployeeStatus.INACTIVE);
        long employeeId = employee.getId();
        employee.setImagePath("Pass");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        doThrow(HystrixRuntimeException.class).when(userRemoteClient).activateEmployee("Test");
        assertThrows(BadRequestException.class, () -> employeeService.activateEmployee(employeeId));

        verify(repository).findById(1L);
    }

    @Test
    void createPositionTest() {
        AddingPositionDto addingPositionDto = AddingPositionDto.builder().name("Водій").build();
        when(positionRepository.existsPositionByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(Position.class), eq(PositionDto.class))).thenReturn(getPositionDto(1L));
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
    void updatePositionTest() {
        PositionDto dto = getPositionDto(1L);
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
    void getAllPositionTest() {
        when(positionRepository.findAll()).thenReturn(List.of(getPosition()));
        when(modelMapper.map(any(), any())).thenReturn(getPositionDto(1L));

        List<PositionDto> positionWithTranslateDtos = employeeService.getAllPositions();
        assertEquals(1, positionWithTranslateDtos.size());

        verify(positionRepository, times(1)).findAll();
    }

    @Test
    void deletePositionTest() {
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
    void deleteEmployeeImageTest() {
        Employee employee = getEmployee();
        employee.setImagePath("path");
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));

        employeeService.deleteEmployeeImage(anyLong());

        verify(repository, times(1)).findById(anyLong());
        verify(fileService, times(1)).delete("path");
        verify(repository, times(1)).save(employee);
    }

    @Test
    void deleteEmployeeImageShouldThrowExceptionsTest() {
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
    void getTariffsForEmployeeTest() {
        TariffsInfo tariffsInfo = getTariffsInfo();
        GetTariffInfoForEmployeeDto dto = GetTariffInfoForEmployeeDto.builder().build();

        when(modelMapper.map(tariffsInfo, GetTariffInfoForEmployeeDto.class)).thenReturn(dto);
        when(tariffsInfoRepository.findAll()).thenReturn(List.of(getTariffsInfo()));

        List<GetTariffInfoForEmployeeDto> dtos = employeeService.getTariffsForEmployee();
        assertEquals(1, dtos.size());
        verify(modelMapper, times(1)).map(any(), any());
        verify(tariffsInfoRepository).findAll();
    }

    @Test
    void getEmployeesByTariffIdTest() {
        Employee employee = new Employee();
        employee.setEmail("test@example.com");
        EmployeeWithTariffsDto employeeDto = new EmployeeWithTariffsDto();
        Long tariffId = 1L;
        List<Employee> employees = List.of(employee);

        when(repository.selectAllEmployeesByTariffIdAndChatEqualsTrue(tariffId)).thenReturn(employees);
        when(modelMapper.map(employee, EmployeeWithTariffsDto.class)).thenReturn(employeeDto);

        List<EmployeeWithTariffsDto> result = employeeService.getEmployeesByTariffId(tariffId);

        assertEquals(1, result.size());

        verify(repository, times(1)).selectAllEmployeesByTariffIdAndChatEqualsTrue(tariffId);
        verify(modelMapper, times(1)).map(employee, EmployeeWithTariffsDto.class);
    }

    @Test
    void getEmployeeByEmailTest() {
        Employee employee = new Employee();
        employee.setEmail("test@example.com");
        EmployeeWithTariffsDto employeeDto = new EmployeeWithTariffsDto();
        String email = "test@example.com";

        when(repository.findByEmail(email)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeWithTariffsDto.class)).thenReturn(employeeDto);

        EmployeeWithTariffsDto result = employeeService.getEmployeeByEmail(email);

        assertNotNull(result);

        verify(repository, times(1)).findByEmail(email);
        verify(modelMapper, times(1)).map(employee, EmployeeWithTariffsDto.class);
    }

    @Test
    void getEmployeeByEmailNotFoundTest() {
        String email = "nonexistent@example.com";

        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> employeeService.getEmployeeByEmail(email));

        verify(repository, times(1)).findByEmail(email);
        verify(modelMapper, times(0)).map(any(Employee.class), eq(EmployeeWithTariffsDto.class));
    }
}
