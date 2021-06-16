package greencity.service.ubs;

import greencity.dto.AddEmployeeDto;
import greencity.dto.EmployeeDto;
import greencity.dto.PageableAdvancedDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UBSEmployeeService {
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
     * Method deletes employee from database by id.
     *
     * @param id {@link Long}
     * @author Mykola Danylko
     */
    void delete(Long id);
}
