package greencity.service.ubs;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.entity.TariffsInfoRecievingEmployee;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeFilterView;
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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
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
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private String defaultImagePath = AppConstant.DEFAULT_IMAGE;
    private final EmployeeCriteriaRepository employeeCriteriaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeWithTariffsDto save(EmployeeWithTariffsIdDto dto, MultipartFile image) {
        String employeeEmail = dto.getEmployeeDto().getEmail();
        dto.getEmployeeDto()
            .setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getEmployeeDto().getPhoneNumber()));
        if (employeeEmail != null
            && employeeRepository.existsByEmailAndActiveStatus(employeeEmail)) {
            throw new UnprocessableEntityException(
                ErrorMessage.ACTIVE_EMPLOYEE_WITH_CURRENT_EMAIL_ALREADY_EXISTS + employeeEmail);
        }

        if (employeeRepository.existsByEmailAndInactiveStatus(employeeEmail)) {
            Employee employee = employeeRepository.findByEmail(employeeEmail).orElseThrow(
                () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND_BY_EMAIL
                    + dto.getEmployeeDto().getEmail()));
            dto.getEmployeeDto().setId(employee.getId());

            EmployeeWithTariffsDto employeeWithTariffsDto = update(dto, image);
            employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
            employeeRepository.save(employee);
            return employeeWithTariffsDto;
        }
        checkValidPosition(dto.getEmployeeDto().getEmployeePositions());

        Employee employee = modelMapper.map(dto, Employee.class);
        employee.setUuid(UUID.randomUUID().toString());
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
        dto.getTariffId().stream().forEach(tariff -> {
            TariffsInfoRecievingEmployee tariffsInfoReceivingEmployees = new TariffsInfoRecievingEmployee();
            tariffsInfoReceivingEmployees.setEmployee(employee);
            tariffsInfoReceivingEmployees.setHasChat(tariff.getHasChat());
            tariffsInfoReceivingEmployees.setTariffsInfo(tariffsInfoRepository.findById(tariff.getTariffId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND)));
            employee.getTariffsInfoReceivingEmployees().add(tariffsInfoReceivingEmployees);
        });
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
            .name(employee.getFirstName())
            .uuid(employee.getUuid())
            .positions(employee.getEmployeePosition().stream()
                .map(position -> PositionDto.builder()
                    .id(position.getId())
                    .name(position.getName())
                    .nameEn(position.getNameEn())
                    .build())
                .collect(Collectors.toList()))
            .isUbs(true)
            .build();
        try {
            userRemoteClient.signUpEmployee(signUpDto);
        } catch (HystrixRuntimeException e) {
            throw new BadRequestException(
                "Error to create user(): User with this email already exists or not valid data ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<GetEmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria filterCriteria) {
        List<EmployeeFilterView> employeeFilterViews = employeeCriteriaRepository.findAll(employeePage, filterCriteria);
        List<GetEmployeeDto> resultList = mapEmployeeFilterViewsToGetEmployeeDtos(employeeFilterViews);
        Pageable pageable = getPageable(employeePage);
        return getAllTranslationDto(new PageImpl<>(resultList, pageable, employeeFilterViews.size()));
    }

    private List<GetEmployeeDto> mapEmployeeFilterViewsToGetEmployeeDtos(List<EmployeeFilterView> employeeFilterViews) {
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, GetEmployeeDto> getEmployeeDtoMap = new LinkedHashMap<>();
        for (var employeeFilterView : employeeFilterViews) {
            var getEmployeeDto = getEmployeeDtoMap.computeIfAbsent(employeeFilterView.getEmployeeId(),
                id -> modelMapper.map(employeeFilterView, GetEmployeeDto.class));
            initializeGetEmployeeDtoCollections(getEmployeeDto);
            fillGetEmployeeDto(employeeFilterView, getEmployeeDto, employees);
        }
        return new ArrayList<>(getEmployeeDtoMap.values());
    }

    private Pageable getPageable(EmployeePage employeePage) {
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        return PageRequest.of(employeePage.getPageNumber(), employeePage.getPageSize(), sort);
    }

    private void fillGetEmployeeDto(EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto,
        List<Employee> employees) {
        fillGetTariffInfoForEmployeeDto(emplView, getEmployeeDto, employees);
        fillPositionDto(emplView, getEmployeeDto, employees);
    }

    private void fillPositionDto(EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto, List<Employee> employees) {
        List<PositionDto> positionsDtos = employees.stream()
            .filter(employee -> employee.getId().equals(emplView.getEmployeeId()))
            .flatMap(employee -> employee.getEmployeePosition().stream()
                .map(position -> modelMapper.map(position, PositionDto.class)))
            .collect(Collectors.toList());

        getEmployeeDto.getEmployeePositions().addAll(positionsDtos);
    }

    private void fillGetTariffInfoForEmployeeDto(
        EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto, List<Employee> employees) {

    }

    private void initializeGetEmployeeDtoCollections(GetEmployeeDto getEmployeeDto) {
        getEmployeeDto.setEmployeePositions(new ArrayList<>());
        getEmployeeDto.setTariffs(new ArrayList<>());
    }

    private PageableDto<GetEmployeeDto> getAllTranslationDto(Page<GetEmployeeDto> pages) {
        List<GetEmployeeDto> getEmployeeDtos = pages.getContent();
        return new PageableDto<>(
            getEmployeeDtos,
            pages.getTotalElements(),
            pages.getPageable().getPageNumber(),
            pages.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmployeeWithTariffsDto update(EmployeeWithTariffsIdDto dto, MultipartFile image) {
        final Employee upEmployee = employeeRepository.findById(dto.getEmployeeDto().getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getEmployeeDto().getId()));

        if (!employeeRepository.findEmployeesByEmailAndIdNot(
            dto.getEmployeeDto().getEmail(), dto.getEmployeeDto().getId()).isEmpty()) {
            throw new BadRequestException(
                "Email already exist in another employee: " + dto.getEmployeeDto().getEmail());
        }
        checkValidPosition(dto.getEmployeeDto().getEmployeePositions());
        dto.getEmployeeDto()
            .setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getEmployeeDto().getPhoneNumber()));
        updateEmployeeEmail(dto, upEmployee.getUuid());
        updateEmployeeAuthoritiesToRelatedPositions(dto);

        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        updatedEmployee.setUuid(upEmployee.getUuid());
        updatedEmployee.setEmployeeStatus(upEmployee.getEmployeeStatus());
        dto.getTariffId().stream().forEach(tariff -> {
            TariffsInfoRecievingEmployee tariffsInfoReceivingEmployees = new TariffsInfoRecievingEmployee();
            tariffsInfoReceivingEmployees.setEmployee(updatedEmployee);
            tariffsInfoReceivingEmployees.setHasChat(tariff.getHasChat());
            tariffsInfoReceivingEmployees.setTariffsInfo(tariffsInfoRepository.findById(tariff.getTariffId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND)));
            updatedEmployee.getTariffsInfoReceivingEmployees().add(tariffsInfoReceivingEmployees);
        });

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

    private void updateEmployeeAuthoritiesToRelatedPositions(EmployeeWithTariffsIdDto dto) {
        var positions = EmployeePositionsDto.builder()
            .email(dto.getEmployeeDto().getEmail())
            .positions(dto.getEmployeeDto().getEmployeePositions())
            .build();
        userRemoteClient.updateAuthoritiesToRelatedPositions(positions);
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
                throw new BadRequestException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + employee.getUuid());
            }
            employeeRepository.save(employee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + id));
        if (employee.getEmployeeStatus() == EmployeeStatus.INACTIVE) {
            employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
            try {
                userRemoteClient.activateEmployee(employee.getUuid());
            } catch (HystrixRuntimeException e) {
                throw new BadRequestException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND + employee.getUuid());
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
            .nameEn(dto.getNameEn())
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

    private void updateEmployeeEmail(EmployeeWithTariffsIdDto dto, String uuid) {
        Employee employee = employeeRepository.findById(dto.getEmployeeDto().getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getEmployeeDto().getId()));
        String oldEmail = employee.getEmail();
        String newEmail = dto.getEmployeeDto().getEmail();
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
