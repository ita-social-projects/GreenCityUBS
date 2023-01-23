package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.SaveEmployeeDto;
import greencity.dto.employee.UpdateEmployeeDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import greencity.enums.SortingOrder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import greencity.repository.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

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
        SaveEmployeeDto dto = getSaveEmployeeDto();
        MockMultipartFile file = new MockMultipartFile("employeeDto",
                "", "application/json", "random Bytes".getBytes());

        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(false);
        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(repository.save(any())).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(any(), any())).thenReturn(true);
        employeeService.save(dto, file);

        verify(repository, times(1)).existsByEmail(getAddEmployeeDto().getEmail());
        verify(modelMapper, times(2)).map(any(), any());
        verify(repository, times(1)).save(any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(any(), any());
    }

    @Test
    void saveEmployeeShouldThrowExceptionTest() {
        Employee employee = getEmployee();
        employee.setId(null);
        SaveEmployeeDto employeeDto = getSaveEmployeeDto();
        employeeDto.setEmail("test@gmail.com");

        when(repository.existsByEmail(getAddEmployeeDto().getEmail())).thenReturn(true, false, false);
        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> employeeService.save(employeeDto, null));
        assertEquals(thrown.getMessage(),
            ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + employeeDto.getEmail());

        verify(repository).existsByEmail(getAddEmployeeDto().getEmail());
    }

    @Test
    void findAllTest() {
        EmployeePage employeePage = new EmployeePage();
        EmployeeFilterCriteria employeeFilterCriteria = new EmployeeFilterCriteria();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
            Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));

        when(employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria))
            .thenReturn(new PageImpl<>(List.of(getEmployee()), pageable, 1L));
        employeeService.findAll(employeePage, employeeFilterCriteria);

        verify(employeeCriteriaRepository, times(1))
            .findAll(employeePage, employeeFilterCriteria);
    }

    @Test
    void updateEmployeeTest() {
        Employee employee = getEmployee();
        UpdateEmployeeDto dto = getUpdateEmployeeDto();
        Position position = ModelUtils.getPosition();

        MockMultipartFile file = new MockMultipartFile("employeeDto",
                "", "application/json", "random Bytes".getBytes());

        when(modelMapper.map(dto, Employee.class)).thenReturn(employee);
        when(positionRepository.existsPositionByIdAndName(position.getId(), position.getName())).thenReturn(true);
        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        employeeService.update(dto, file);

        verify(modelMapper, times(2)).map(any(), any());
        verify(positionRepository, atLeastOnce()).existsPositionByIdAndName(position.getId(), position.getName());
        verify(repository, times(2)).findById(anyLong());
    }

    @Test
    void updateEmployeeNotFoundTest() {
        UpdateEmployeeDto dto = ModelUtils.getUpdateEmployeeDto();
        assertThrows(NotFoundException.class, () -> employeeService.update(dto, null));
    }

    @Test
    void updateEmployeeEmailAlreadyExistsTest() {
        UpdateEmployeeDto dto = ModelUtils.getUpdateEmployeeDto();
        Employee employee = getEmployee();

        when(repository.findById(anyLong())).thenReturn(Optional.of(employee));
        when(repository.findEmployeeWithEmailAndNotId(anyString(), anyLong())).thenReturn(List.of(employee));
        assertThrows(BadRequestException.class, () -> employeeService.update(dto, null));

        verify(repository).findById(anyLong());
        verify(repository).findEmployeeWithEmailAndNotId(anyString(), anyLong());
    }

    @Test
    void deleteTest() {
        Employee employee = getEmployee();
        employee.setImagePath("Pass");
        System.out.println(employee.getEmployeeStatus());
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        employeeService.deleteEmployee(1L);
        verify(repository).findById(1L);
        System.out.println(employee.getEmployeeStatus());
        assertEquals(EmployeeStatus.INACTIVE, employee.getEmployeeStatus());
        Exception thrown = assertThrows(NotFoundException.class,
            () -> employeeService.deleteEmployee(2L));
        assertEquals(thrown.getMessage(), ErrorMessage.EMPLOYEE_NOT_FOUND + 2L);
    }

    @Test
    void createPositionTest() {
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
    void updatePositionTest() {
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
    void getAllPositionTest() {
        when(positionRepository.findAll()).thenReturn(List.of(getPosition()));
        when(modelMapper.map(any(), any())).thenReturn(getPositionDto());

        List<PositionDto> positionDtos = employeeService.getAllPositions();

        assertEquals(1, positionDtos.size());

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
    void findAllActiveEmployeesTest() {
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
    void test() {
        Employee employee = getEmployee();
        UpdateEmployeeDto dto = getUpdateEmployeeDto();
        employee.setEmail("test@gmail.com");
        dto.setEmail("test@gmail.com");
        verify(userRemoteClient).updateEmployeeEmail(employee.getEmail(), dto.getEmail());
        assertEquals(employee.getEmail(), dto.getEmail());
    }
}
