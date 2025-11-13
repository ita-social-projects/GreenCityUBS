package greencity.service.ubs;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import greencity.client.UserRemoteClient;
import greencity.client.config.UserRemoteWebClient;
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
import greencity.dto.tariff.TariffWithChatAccess;
import greencity.entity.TariffsInfoRecievingEmployee;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeFilterView;
import greencity.entity.user.employee.Position;
import greencity.enums.EmployeeStatus;
import greencity.enums.UserStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.dto.filters.EmployeeFilterCriteria;
import greencity.dto.filters.EmployeePage;
import greencity.repository.EmployeeCriteriaRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.UserRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.EmployeeOrderPositionRepository;
import greencity.service.phone.UAPhoneNumberUtil;
import greencity.service.ubs.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Data
@Slf4j
public class UBSManagementEmployeeServiceImpl implements UBSManagementEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final ReceivingStationRepository stationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final UserRemoteClient userRemoteClient;
    private final UserRemoteWebClient userRemoteWebClient;
    private final ModelMapper modelMapper;
    private final EmployeeCriteriaRepository employeeCriteriaRepository;
    private final EmployeeOrderPositionRepository employeeOrderPositionRepository;
    private final UserService userService;
    private String defaultImagePath = AppConstant.DEFAULT_IMAGE;

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
        checkValidPosition(dto.getEmployeeDto().getEmployeePositionIds());

        Employee employee = buildEmployeeFromEmployeeWithTariffsIdDto(dto);
        employee.setUuid(UUID.randomUUID().toString());
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
        if (image != null) {
            try {
                employee.setImagePath(userRemoteWebClient.uploadFile(image));
            } catch (WebClientRequestException | WebClientResponseException e) {
                log.warn(AppConstant.USER_SERVICE_UNAVAILABLE_LOG, e.getMessage());
            }
        } else {
            employee.setImagePath(defaultImagePath);
        }

        if (employee.getTariffsInfoReceivingEmployees() == null) {
            employee.setTariffsInfoReceivingEmployees(new ArrayList<>());
        }

        List<TariffWithChatAccess> tariffs = dto.getTariffs();
        if (tariffs != null) {
            tariffs.forEach(tariff -> {
                TariffsInfoRecievingEmployee tariffsInfoReceivingEmployees = new TariffsInfoRecievingEmployee();
                tariffsInfoReceivingEmployees.setEmployee(employee);
                tariffsInfoReceivingEmployees.setHasChat(tariff.getHasChat());
                tariffsInfoReceivingEmployees.setTariffsInfo(tariffsInfoRepository.findById(tariff.getTariffId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND)));
                employee.getTariffsInfoReceivingEmployees().add(tariffsInfoReceivingEmployees);
            });
        }
        signUpEmployee(employee);
        return modelMapper.map(employeeRepository.save(employee), EmployeeWithTariffsDto.class);
    }

    private Employee buildEmployeeFromEmployeeWithTariffsIdDto(EmployeeWithTariffsIdDto employeeWithTariffsIdDto) {
        return Employee.builder()
            .firstName(employeeWithTariffsIdDto.getEmployeeDto().getFirstName())
            .lastName(employeeWithTariffsIdDto.getEmployeeDto().getLastName())
            .phoneNumber(employeeWithTariffsIdDto.getEmployeeDto().getPhoneNumber())
            .email(employeeWithTariffsIdDto.getEmployeeDto().getEmail())
            .employeePosition(
                positionRepository.findByIdIn((employeeWithTariffsIdDto.getEmployeeDto().getEmployeePositionIds())))
            .build();
    }

    private void signUpEmployee(Employee employee) {
        EmployeeSignUpDto signUpDto = EmployeeSignUpDto.builder()
            .email(employee.getEmail())
            .name(employee.getFirstName())
            .uuid(employee.getUuid())
            .positions(employee.getEmployeePosition().stream()
                .map(position -> PositionDto.builder()
                    .id(position.getId())
                    .nameUk(position.getNameUk())
                    .nameEn(position.getNameEn())
                    .build())
                .toList())
            .isUbs(true)
            .build();
        try {
            userRemoteClient.signUpEmployee(signUpDto, "uk");
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
        List<GetEmployeeDto> resultList = mapEmployeeFilterViewsToGetEmployeeDTOs(employeeFilterViews);
        Pageable pageable = getPageable(employeePage);
        return getAllTranslationDto(new PageImpl<>(resultList, pageable, employeeFilterViews.size()));
    }

    private List<GetEmployeeDto> mapEmployeeFilterViewsToGetEmployeeDTOs(List<EmployeeFilterView> employeeFilterViews) {
        List<Employee> employees = employeeRepository.findAll();
        Map<Long, GetEmployeeDto> getEmployeeDtoMap = new LinkedHashMap<>();
        for (EmployeeFilterView employeeFilterView : employeeFilterViews) {
            GetEmployeeDto getEmployeeDto = getEmployeeDtoMap.computeIfAbsent(employeeFilterView.getEmployeeId(),
                id -> modelMapper.map(employeeFilterView, GetEmployeeDto.class));
            getEmployeeDto.setTariffs(employees.stream()
                .filter(employee -> employee.getId().equals(employeeFilterView.getEmployeeId()))
                .flatMap(employee -> employee.getTariffsInfoReceivingEmployees().stream()
                    .map(tariffsInfoRecievingEmployee -> modelMapper.map(tariffsInfoRecievingEmployee.getTariffsInfo(),
                        GetTariffInfoForEmployeeDto.class)))
                .toList());
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
        List<PositionDto> positionsDTOs = employees.stream()
            .filter(employee -> employee.getId().equals(emplView.getEmployeeId()))
            .flatMap(employee -> employee.getEmployeePosition().stream()
                .map(position -> modelMapper.map(position, PositionDto.class)))
            .toList();

        getEmployeeDto.getEmployeePositions().addAll(positionsDTOs);
    }

    private void fillGetTariffInfoForEmployeeDto(
        EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto, List<Employee> employees) {
        List<GetTariffInfoForEmployeeDto> tariffs = employees.stream()
            .filter(employee -> employee.getId().equals(emplView.getEmployeeId()))
            .flatMap(employee -> employee.getTariffsInfoReceivingEmployees().stream()
                .map(tariffsInfoRecievingEmployee -> {
                    GetTariffInfoForEmployeeDto tariffDto =
                        modelMapper.map(tariffsInfoRecievingEmployee.getTariffsInfo(),
                            GetTariffInfoForEmployeeDto.class);
                    tariffDto.setHasChat(tariffsInfoRecievingEmployee.getHasChat());
                    return tariffDto;
                }))
            .toList();

        getEmployeeDto.setTariffs(tariffs);
    }

    private void initializeGetEmployeeDtoCollections(GetEmployeeDto getEmployeeDto) {
        getEmployeeDto.setEmployeePositions(new ArrayList<>());
        getEmployeeDto.setTariffs(new ArrayList<>());
    }

    private PageableDto<GetEmployeeDto> getAllTranslationDto(Page<GetEmployeeDto> pages) {
        List<GetEmployeeDto> getEmployeeDTOs = pages.getContent();
        return new PageableDto<>(
            getEmployeeDTOs,
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
        checkValidPosition(dto.getEmployeeDto().getEmployeePositionIds());
        dto.getEmployeeDto()
            .setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getEmployeeDto().getPhoneNumber()));
        updateEmployeeEmail(dto, upEmployee.getUuid());
        updateEmployeeAuthoritiesToRelatedPositions(dto);

        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        updatedEmployee.setUuid(upEmployee.getUuid());
        updatedEmployee.setEmployeeStatus(upEmployee.getEmployeeStatus());
        if (updatedEmployee.getTariffsInfoReceivingEmployees() == null) {
            updatedEmployee.setTariffsInfoReceivingEmployees(new ArrayList<>());
        }

        if (dto.getTariffs() != null) {
            dto.getTariffs().forEach(tariff -> {
                TariffsInfoRecievingEmployee tariffsInfoReceivingEmployees = new TariffsInfoRecievingEmployee();
                tariffsInfoReceivingEmployees.setEmployee(updatedEmployee);
                tariffsInfoReceivingEmployees.setHasChat(tariff.getHasChat());
                tariffsInfoReceivingEmployees.setTariffsInfo(tariffsInfoRepository.findById(tariff.getTariffId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND)));
                updatedEmployee.getTariffsInfoReceivingEmployees().add(tariffsInfoReceivingEmployees);
            });
        }

        if (image != null) {
            String imageUrlToDelete = upEmployee.getImagePath();
            try {
                updatedEmployee.setImagePath(userRemoteWebClient.uploadFile(image));
            } catch (WebClientRequestException | WebClientResponseException e) {
                log.warn(AppConstant.USER_SERVICE_UNAVAILABLE_LOG, e.getMessage());
            }
            if (!imageUrlToDelete.equals(defaultImagePath)) {
                try {
                    userRemoteWebClient.deleteFile(imageUrlToDelete);
                } catch (WebClientRequestException | WebClientResponseException e) {
                    log.warn(AppConstant.USER_SERVICE_UNAVAILABLE_LOG, e.getMessage());
                }
            }
        } else {
            updatedEmployee.setImagePath(dto.getEmployeeDto().getImage());
        }

        return modelMapper.map(employeeRepository.save(updatedEmployee), EmployeeWithTariffsDto.class);
    }

    @Override
    @Transactional
    public PositionDto update(PositionDto dto) {
        if (!positionRepository.existsById(dto.getId())) {
            throw new NotFoundException(ErrorMessage.POSITION_NOT_FOUND_BY_ID + dto.getId());
        }
        if (!positionRepository.existsPositionByNameUk(dto.getNameUk())) {
            Position position = modelMapper.map(dto, Position.class);
            return modelMapper.map(positionRepository.save(position), PositionDto.class);
        }
        throw new UnprocessableEntityException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getNameUk());
    }

    private void updateEmployeeAuthoritiesToRelatedPositions(EmployeeWithTariffsIdDto dto) {
        Set<Position> positions = positionRepository.findByIdIn(dto.getEmployeeDto().getEmployeePositionIds());

        EmployeePositionsDto employeePositionsDto = EmployeePositionsDto.builder()
            .email(dto.getEmployeeDto().getEmail())
            .positions(positions.stream().map(position -> modelMapper.map(position, PositionDto.class)).toList())
            .build();
        userRemoteClient.updateAuthoritiesToRelatedPositions(employeePositionsDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void updateEmployeeStatus(String currentUserUuid, Long targetEmployeeId, EmployeeStatus employeeStatus) {
        Employee employee = employeeRepository.findById(targetEmployeeId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + targetEmployeeId));

        if (!employee.getEmployeeStatus().equals(employeeStatus)) {
            User employeeUser = userRepository.findUserByUuid(employee.getUuid())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_UUID + employee.getUuid()));
            UserStatus userStatus = switch (employeeStatus) {
                case ACTIVE -> UserStatus.ACTIVATED;
                case INACTIVE -> UserStatus.DEACTIVATED;
            };

            employee.setEmployeeStatus(employeeStatus);
            employeeRepository.save(employee);
            userService.updateUserStatusById(currentUserUuid, employeeUser.getId(), userStatus);
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
            try {
                userRemoteWebClient.deleteFile(employee.getImagePath());
            } catch (WebClientRequestException | WebClientResponseException e) {
                log.warn(AppConstant.USER_SERVICE_UNAVAILABLE_LOG, e.getMessage());
            }
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
        if (!positionRepository.existsPositionByNameUk(dto.getNameUk())) {
            Position position = positionRepository.save(buildPosition(dto));
            return modelMapper.map(position, PositionDto.class);
        }
        throw new UnprocessableEntityException(ErrorMessage.CURRENT_POSITION_ALREADY_EXISTS + dto.getNameUk());
    }

    private Position buildPosition(AddingPositionDto dto) {
        return Position.builder()
            .nameUk(dto.getNameUk())
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
            .toList();
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

    private void checkValidPosition(Set<Long> positionIds) {
        if (!existPositions(positionIds)) {
            throw new NotFoundException(ErrorMessage.POSITION_NOT_FOUND);
        }
    }

    private boolean existPositions(Set<Long> positionIds) {
        return positionIds.stream()
            .allMatch(positionRepository::existsById);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GetTariffInfoForEmployeeDto> getTariffsForEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND_BY_EMAIL + email));

        List<TariffsInfo> tariffs = tariffsInfoRepository.findByEmployeeId(employee.getId());
        return tariffs
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffInfoForEmployeeDto.class))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EmployeeWithTariffsDto> getEmployeesByTariffId(Long tariffId) {
        List<Employee> employeeWithEnabledChat =
            employeeRepository.selectAllEmployeesByTariffIdAndChatEqualsTrue(tariffId);

        if (employeeWithEnabledChat.isEmpty()) {
            throw new NotFoundException(ErrorMessage.EMPLOYEE_WITH_ENABLED_CHAT_NOT_FOUND_BY_TARIFF_ID + tariffId);
        }

        return employeeWithEnabledChat
            .stream()
            .map(employee -> modelMapper.map(employee, EmployeeWithTariffsDto.class))
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmployeeWithTariffsDto getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow(
            () -> new UserNotFoundException(String.format("Employee not found with email %s", email)));

        return modelMapper.map(employee, EmployeeWithTariffsDto.class);
    }
}
