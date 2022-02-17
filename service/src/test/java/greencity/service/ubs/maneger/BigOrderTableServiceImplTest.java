package greencity.service.ubs.maneger;

import greencity.ModelUtils;
import greencity.dto.BigOrderTableDTO;
import greencity.entity.parameters.CustomTableView;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.BigOrderTableRepository;
import greencity.repository.CustomTableViewRepo;
import greencity.service.ubs.manager.BigOrderTableViewServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class BigOrderTableServiceImplTest {

    @InjectMocks
    private BigOrderTableViewServiceImpl bigOrderTableService;

    @Mock
    private BigOrderTableRepository bigOrderTableRepository;

    @Mock(lenient = true)
    CustomTableViewRepo customTableViewRepo;

    @Mock
    private ModelMapper modelMapper;

    @Test
    void getOrders() {
        var orderPage = getOrderPage();
        var orderSearchCriteria = getOrderSearchCriteria();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(Page.empty());

        bigOrderTableService.getOrders(orderPage, orderSearchCriteria, "uuid");

        verify(bigOrderTableRepository).findAll(orderPage, orderSearchCriteria);
    }

    @Test
    void getOrdersCorrectCalculateWhenAllValueNotNull() {
        var pageView = ModelUtils.getBigOrderedTableViewPage();
        var bigOrderTableDTOPage = ModelUtils.getBigOrderTableDTOPage();
        var bigOrderTable = ModelUtils.getBigOrderTableDto();
        var orderPage = getOrderPage();
        var orderSearchCriteria = getOrderSearchCriteria();

        when(bigOrderTableRepository.findAll(orderPage, orderSearchCriteria)).thenReturn(pageView);
        when(modelMapper.map(pageView.getContent().get(0), BigOrderTableDTO.class)).thenReturn(bigOrderTable);

        assertEquals(bigOrderTableService.getOrders(orderPage, orderSearchCriteria, "uuid").getContent(),
            bigOrderTableDTOPage.getContent());
    }

    @Test
    void changeOrderTableView() {
        String uuid = "uuid1";
        CustomTableView customTableView = CustomTableView.builder()
            .titles("titles1,titles2")
            .uuid(uuid)
            .build();

        bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
        verify(customTableViewRepo).save(customTableView);
    }

    @Test
    void changeOrderTableView2() {
        String uuid = "uuid1";

        when(customTableViewRepo.existsByUuid(uuid)).thenReturn(Boolean.TRUE);
        bigOrderTableService.changeOrderTableView(uuid, "titles1,titles2");

        verify(customTableViewRepo).existsByUuid(uuid);
    }

    @Test
    void getCustomTableParametersForExistUuid() {
        CustomTableView customTableView = ModelUtils.getCustomTableView();
        when(customTableViewRepo.findByUuid("uuid1")).thenReturn(customTableView);
        when(customTableViewRepo.existsByUuid("uuid1")).thenReturn(Boolean.TRUE);
        bigOrderTableService.getCustomTableParameters(customTableView.getUuid());

        verify(customTableViewRepo).existsByUuid(customTableView.getUuid());
        Assertions.assertNotNull(customTableView);
    }

    @Test
    void getCustomTableParametersForNon_ExistUuid() {
        CustomTableView customTableView = ModelUtils.getCustomTableView();
        when(customTableViewRepo.findByUuid("uuid1")).thenReturn(customTableView);
        when(customTableViewRepo.existsByUuid("uuid1")).thenReturn(Boolean.FALSE);
        bigOrderTableService.getCustomTableParameters(customTableView.getUuid());

        verify(customTableViewRepo).existsByUuid(customTableView.getUuid());
        Assertions.assertNotNull(customTableView);
    }

    private OrderPage getOrderPage() {
        return new OrderPage().setPageNumber(1);
    }

    private OrderSearchCriteria getOrderSearchCriteria() {
        return new OrderSearchCriteria().setOrderDate(new DateFilter().setFrom("2022-11-05"));
    }
}
