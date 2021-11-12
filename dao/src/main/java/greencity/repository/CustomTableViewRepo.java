package greencity.repository;

import greencity.entity.parameters.CustomTableView;
import greencity.entity.user.employee.EmployeeOrderPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CustomTableViewRepo extends JpaRepository<CustomTableView, Integer> {
    /**
     * Method find CustomTableView by Uuid.
     *
     * @return CustomTableView entity
     * @author Sikhovskiy Rostyslav
     */
    CustomTableView findByUuid(String uuid);

    /**
     * Method saves CustomTableView entity in Data Base.
     *
     * @return CustomTableView entity
     * @author Sikhovskiy Rostyslav
     */
    CustomTableView save(CustomTableView customTableView);

    /**
     * Method to check if exists CustomTableView with current Uuid.
     *
     * @author Sikhovskiy Rostyslav
     */
    Boolean existsByUuid(String uuid);

    /**
     * Method update titles in CustomTableView table.
     *
     * @author Sikhovskiy Rostyslav
     */
    @Transactional
    @Modifying
    @Query("update CustomTableView e set e.titles=:titles where e.uuid=:uuid")
    void update(String uuid, String titles);
}
