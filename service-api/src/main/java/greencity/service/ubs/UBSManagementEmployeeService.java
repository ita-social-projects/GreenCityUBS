package greencity.service.ubs;

import greencity.dto.employee.EmployeeWithTariffsDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UBSManagementEmployeeService {
    /**
     * Method saves new employee.
     *
     * @param dto   {@link EmployeeWithTariffsIdDto} that contains new employee.
     * @param image {@link MultipartFile} that contains employee's image.
     * @return employeeWithTariffsDto {@link EmployeeWithTariffsDto} that contains
     *         employee from database.
     * @author Mykola Danylko
     */
    EmployeeWithTariffsDto save(EmployeeWithTariffsIdDto dto, MultipartFile image);

    /**
     * {@inheritDoc}
     */
    PageableDto<GetEmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria);

    /**
     * Method updates information about employee.
     *
     * @param dto   {@link EmployeeWithTariffsIdDto}
     * @param image {@link MultipartFile} that contains employee's image.
     * @return employeeWithTariffsDto {@link EmployeeWithTariffsDto} that contains
     *         employee from database.
     * @author Mykola Danylko
     */
    EmployeeWithTariffsDto update(EmployeeWithTariffsIdDto dto, MultipartFile image);

    /**
     * Method updates information about position.
     *
     * @param dto {@link PositionDto}
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    PositionDto update(PositionDto dto);

    /**
     * Method deletes employee by id.
     *
     * @param id {@link Long}
     * @author Mykola Danylko
     */
    void deactivateEmployee(Long id);

    /**
     * Method activate employee by id.
     *
     * @param id {@link Long}
     * @author Oksana Spodaryk
     */
    void activateEmployee(Long id);

    /**
     * Method creates new employee position.
     *
     * @param dto {@link AddingPositionDto}
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    PositionDto create(AddingPositionDto dto);

    /**
     * Method gets all positions.
     *
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    List<PositionDto> getAllPositions();

    /**
     * Method deletes position by id.
     *
     * @param id {@link Long} position's id.
     * @author Mykola Danylko
     */
    void deletePosition(Long id);

    /**
     * Method deletes employee image.
     *
     * @param id (@link Long) employee id.
     * @author Mykola Danylko
     */
    void deleteEmployeeImage(Long id);

    /**
     * Method that return list of GetTariffInfoForEmployeeDto.
     *
     * @return list of GetTariffInfoForEmployeeDto.
     * @author Nikita Korzh.
     */
    List<GetTariffInfoForEmployeeDto> getTariffsForEmployee();

    /**
     * Method to get a list of employees associated with a specific tariff.
     *
     * @param tariffId The ID of the tariff for which to retrieve associated employees.
     * @return A list of GetEmployeeDto objects representing the employees associated with the given tariff.
     */
    List<EmployeeWithTariffsDto> getEmployeesByTariffId(Long tariffId);
}
