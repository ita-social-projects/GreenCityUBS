package greencity.service.ubs;

import greencity.dto.*;
import org.springframework.data.domain.Pageable;
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
    EmployeeDto save(AddEmployeeDto dto, MultipartFile image);

    /**
     * Method finds all employee.
     *
     * @param pageable {@link Pageable}
     * @return pageableDto {@link PageableAdvancedDto} that contains employees from
     *         database.
     * @author Mykola Danylko
     */
    PageableAdvancedDto<EmployeeDto> findAll(Pageable pageable);

    /**
     * Method updates information about employee.
     *
     * @param dto {@link EmployeeDto}
     * @return employeeDto {@link EmployeeDto} that contains employee from database.
     * @author Mykola Danylko
     */
    EmployeeDto update(EmployeeDto dto);

    /**
     * Method updates information about position.
     *
     * @param dto {@link PositionDto}
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    PositionDto update(PositionDto dto);

    /**
     * Method updates information about receiving station.
     *
     * @param dto {@link ReceivingStationDto}
     * @return {@link ReceivingStationDto}
     * @author Mykola Danylko
     */
    ReceivingStationDto update(ReceivingStationDto dto);

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
     * Method creates new receiving station.
     *
     * @param dto {@link AddingReceivingStationDto}
     * @return {@link ReceivingStationDto}
     * @author Mykola Danylko
     */
    ReceivingStationDto create(AddingReceivingStationDto dto);

    /**
     * Method gets all positions.
     *
     * @return {@link PositionDto}
     * @author Mykola Danylko
     */
    List<PositionDto> getAllPositions();

    /**
     * Method gets all receiving stations.
     *
     * @return {@link ReceivingStationDto}
     * @author Mykola Danylko
     */
    List<ReceivingStationDto> getAllReceivingStation();

    /**
     * Method deletes position by id.
     *
     * @param id {@link Long} position's id.
     * @author Mykola Danylko
     */
    void deletePosition(Long id);

    /**
     * Method deletes receiving station by id.
     * 
     * @param id {@link Long} receiving station's id
     * @author Mykola Danylko
     */
    void deleteReceivingStation(Long id);
}
