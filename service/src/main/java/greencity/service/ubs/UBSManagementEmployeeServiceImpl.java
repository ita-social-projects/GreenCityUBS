package greencity.service.ubs;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.LocationsDtos;
import greencity.dto.courier.GetReceivingStationDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.employee.EmployeeSignUpDto;
import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.EmployeePositionsDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
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
import greencity.repository.EmployeeCriteriaRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
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
import java.util.HashMap;
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
    public EmployeeWithTariffsDto save(EmployeeWithTariffsIdDto dto, MultipartFile image) {
        dto.getEmployeeDto()
            .setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getEmployeeDto().getPhoneNumber()));
        if (dto.getEmployeeDto().getEmail() != null
            && employeeRepository.existsByEmail(dto.getEmployeeDto().getEmail())) {
            throw new UnprocessableEntityException(
                ErrorMessage.CURRENT_EMAIL_ALREADY_EXISTS + dto.getEmployeeDto().getEmail());
        }
        checkValidPosition(dto.getEmployeeDto().getEmployeePositions());

        Employee employee = modelMapper.map(dto, Employee.class);
        employee.setUuid(UUID.randomUUID().toString());
        employee.setTariffInfos(tariffsInfoRepository.findTariffsInfosByIdIsIn(dto.getTariffId()));
        employee.setEmployeeStatus(EmployeeStatus.ACTIVE);
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
            .name(employee.getFirstName() + employee.getLastName())
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
            throw new BadRequestException("Error to create user(): User with this email already exists or not valid data ");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GetEmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria) {
        List<EmployeeFilterView> employeeFilterViews =
            employeeCriteriaRepository.findAll(employeePage, employeeFilterCriteria);

        Map<Long, GetEmployeeDto> getEmployeeDtoMap = new HashMap<>();
        for (var employeeFilterView : employeeFilterViews) {
            var getEmployeeDto = getEmployeeDtoMap.computeIfAbsent(
                employeeFilterView.getEmployeeId(), id -> modelMapper.map(employeeFilterView, GetEmployeeDto.class));

            initializeGetEmployeeDtoCollectionsIfNeeded(getEmployeeDto);

            fillGetEmployeeDto(employeeFilterView, getEmployeeDto);
        }

        var resultList = new ArrayList<>(getEmployeeDtoMap.values());
        Sort sort = Sort.by(employeePage.getSortDirection(), employeePage.getSortBy());
        Pageable pageable = PageRequest.of(employeePage.getPageNumber(),
            employeePage.getPageSize(), sort);
        return new PageImpl<>(resultList, pageable, employeeFilterViews.size());
    }

    private void fillGetEmployeeDto(EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto) {
        fillGetTariffInfoForEmployeeDto(emplView, getEmployeeDto);
        fillPositionDto(emplView, getEmployeeDto);
    }

    private void fillPositionDto(EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto) {
        if (isPositionIdAbsent(emplView.getPositionId(), getEmployeeDto)) {
            var positionDto = modelMapper.map(emplView, PositionDto.class);
            getEmployeeDto.getEmployeePositions().add(positionDto);
        }
    }

    private void fillGetTariffInfoForEmployeeDto(EmployeeFilterView emplView, GetEmployeeDto getEmployeeDto) {
        GetTariffInfoForEmployeeDto tariffInfoDto;
        if (isMainInfoAboutCurrentTariffAbsent(emplView.getTariffsInfoId(), getEmployeeDto)) {
            tariffInfoDto = modelMapper.map(emplView, GetTariffInfoForEmployeeDto.class);
            initializeTariffInfoDtoCollections(tariffInfoDto);
            fillGetTariffInfoForEmployeeDtoCollections(emplView, tariffInfoDto);
            getEmployeeDto.getTariffs().add(tariffInfoDto);
        } else {
            tariffInfoDto = extractTariffInfoForEmployeeDto(emplView, getEmployeeDto);
            fillGetTariffInfoForEmployeeDtoCollections(emplView, tariffInfoDto);
        }
    }

    private void fillGetTariffInfoForEmployeeDtoCollections(EmployeeFilterView emplView,
        GetTariffInfoForEmployeeDto tariffInfoDto) {
        if (isLocationIdAbsent(emplView.getLocationId(), tariffInfoDto)) {
            tariffInfoDto.getLocationsDtos().add(modelMapper.map(emplView, LocationsDtos.class));
        }
        if (isReceivingStationIdAbsent(emplView.getReceivingStationId(), tariffInfoDto)) {
            tariffInfoDto.getReceivingStationDtos().add(modelMapper.map(emplView, GetReceivingStationDto.class));
        }
    }

    private boolean isMainInfoAboutCurrentTariffAbsent(Long tariffInfoId, GetEmployeeDto getEmployeeDto) {
        return getEmployeeDto.getTariffs()
            .stream()
            .noneMatch(tariffInfoDto -> tariffInfoDto.getId().equals(tariffInfoId));
    }

    private static boolean isReceivingStationIdAbsent(Long receivingStationId,
        GetTariffInfoForEmployeeDto tariffInfoDto) {
        return tariffInfoDto.getReceivingStationDtos()
            .stream()
            .noneMatch(dto -> dto.getStationId().equals(receivingStationId));
    }

    private boolean isLocationIdAbsent(Long locationId, GetTariffInfoForEmployeeDto tariffInfoDto) {
        return tariffInfoDto.getLocationsDtos()
            .stream()
            .noneMatch(locationDto -> locationDto.getLocationId().equals(locationId));
    }

    private GetTariffInfoForEmployeeDto extractTariffInfoForEmployeeDto(EmployeeFilterView emplView,
        GetEmployeeDto getEmployeeDto) {
        return getEmployeeDto.getTariffs()
            .stream()
            .filter(tariffInfoDto -> tariffInfoDto.getId().equals(emplView.getTariffsInfoId()))
            .findAny()
            .orElseThrow();
    }

    private void initializeTariffInfoDtoCollections(GetTariffInfoForEmployeeDto tariffInfoDto) {
        if (tariffInfoDto.getLocationsDtos() == null || tariffInfoDto.getReceivingStationDtos() == null) {
            tariffInfoDto.setLocationsDtos(new ArrayList<>());
            tariffInfoDto.setReceivingStationDtos(new ArrayList<>());
        }
    }

    private boolean isPositionIdAbsent(Long positionId, GetEmployeeDto getEmployeeDto) {
        return getEmployeeDto.getEmployeePositions()
            .stream()
            .noneMatch(dto -> dto.getId().equals(positionId));
    }

    private void initializeGetEmployeeDtoCollectionsIfNeeded(GetEmployeeDto getEmployeeDto) {
        if (getEmployeeDto.getEmployeePositions() == null || getEmployeeDto.getTariffs() == null) {
            getEmployeeDto.setEmployeePositions(new ArrayList<>());
            getEmployeeDto.setTariffs(new ArrayList<>());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public EmployeeWithTariffsDto update(EmployeeWithTariffsIdDto dto, MultipartFile image) {
        final Employee upEmployee = employeeRepository.findById(dto.getEmployeeDto().getId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getEmployeeDto().getId()));

        if (!employeeRepository
            .findEmployeesByEmailAndIdNot(dto.getEmployeeDto().getEmail(), dto.getEmployeeDto().getId()).isEmpty()) {
            throw new BadRequestException(
                "Email already exist in another employee: " + dto.getEmployeeDto().getEmail());
        }
        checkValidPosition(dto.getEmployeeDto().getEmployeePositions());
        dto.getEmployeeDto()
            .setPhoneNumber(UAPhoneNumberUtil.getE164PhoneNumberFormat(dto.getEmployeeDto().getPhoneNumber()));
        updateEmployeeEmail(dto, upEmployee.getUuid());
        updateEmployeeAuthoritiesToRelatedPositions(dto);

        Employee updatedEmployee = modelMapper.map(dto, Employee.class);
        updatedEmployee.setTariffInfos(tariffsInfoRepository.findTariffsInfosByIdIsIn(dto.getTariffId()));
        updatedEmployee.setUuid(upEmployee.getUuid());
        updatedEmployee.setEmployeeStatus(upEmployee.getEmployeeStatus());

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
                throw new BadRequestException("Employee with current uuid doesn't exist: " + employee.getUuid());
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
        Employee employee = employeeRepository.findById(dto.getEmployeeDto().getId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.EMPLOYEE_NOT_FOUND + dto.getEmployeeDto().getId()));
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
