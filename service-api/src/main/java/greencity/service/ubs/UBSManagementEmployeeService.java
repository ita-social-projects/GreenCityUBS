package greencity.service.ubs;

import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.GetEmployeeDto;
import greencity.dto.position.AddingPositionDto;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.GetTariffInfoForEmployeeDto;
import greencity.filters.EmployeeFilterCriteria;
import greencity.filters.EmployeePage;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UBSManagementEmployeeService {
    /**
     * Method saves new employee.
     *
     * @param dto   {@link EmployeeDto} that contains new employee.
     * @param image {@link MultipartFile} that contains employee's image.
     * @return employeeDto {@link EmployeeDto} that contains employee from database.
     * @author Mykola Danylko
     */
    EmployeeDto save(EmployeeDto dto, MultipartFile image);

    /**
     * {@inheritDoc}
     */
    Page<EmployeeDto> findAll(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria);

    /**
     * {@inheritDoc}
     */
    Page<GetEmployeeDto> findAllActiveEmployees(EmployeePage employeePage, EmployeeFilterCriteria employeeFilterCriteria);

    /**
     * Method updates information about employee.
     *
     * @param dto   {@link EmployeeDto}
     * @param image {@link MultipartFile} that contains employee's image.
     * @return employeeDto {@link EmployeeDto} that contains employee from database.
     * @author Mykola Danylko
     */
    EmployeeDto update(EmployeeDto dto, MultipartFile image);

    /**
     * Method updates information about position.
     *
     * @param dto {@link PositionDto}
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    PositionDto update(PositionDto dto);

    /**
     * Method deletes employee from database by id.
     *
     * @param id {@link Long}
     * @author Mykola Danylko
     */
    void deleteEmployee(Long id);

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


    List<GetTariffInfoForEmployeeDto> getTariffsForEmployee();

}
