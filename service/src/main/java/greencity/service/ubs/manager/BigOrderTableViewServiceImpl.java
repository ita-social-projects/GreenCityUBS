package greencity.service.ubs.manager;

import greencity.dto.BigOrderTableDTO;
import greencity.dto.CustomTableViewDto;
import greencity.entity.parameters.CustomTableView;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import greencity.repository.BigOrderTableRepository;
import greencity.repository.CustomTableViewRepo;
import greencity.service.ubs.maneger.BigOrderTableServiceView;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@AllArgsConstructor
public class BigOrderTableViewServiceImpl implements BigOrderTableServiceView {
    private final BigOrderTableRepository bigOrderTableRepository;
    private final CustomTableViewRepo customTableViewRepo;
    private final ModelMapper modelMapper;

    @Override
    public Page<BigOrderTableDTO> getOrders(OrderPage orderPage, OrderSearchCriteria searchCriteria, String uuid) {
        var orders = bigOrderTableRepository.findAll(orderPage, searchCriteria);
        var orderList = new ArrayList<BigOrderTableDTO>();
        orders.forEach(o -> orderList.add(modelMapper.map(o, BigOrderTableDTO.class)));
        return new PageImpl<>(orderList, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    public void changeOrderTableView(String uuid, String titles) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            customTableViewRepo.update(uuid, titles);
        } else {
            CustomTableView customTableView = CustomTableView.builder()
                .uuid(uuid)
                .titles(titles)
                .build();
            customTableViewRepo.save(customTableView);
        }
    }

    @Override
    public CustomTableViewDto getCustomTableParameters(String uuid) {
        if (Boolean.TRUE.equals(customTableViewRepo.existsByUuid(uuid))) {
            return castTableViewToDto(customTableViewRepo.findByUuid(uuid).getTitles());
        } else {
            return CustomTableViewDto.builder()
                .titles(" ")
                .build();
        }
    }

    private CustomTableViewDto castTableViewToDto(String titles) {
        return CustomTableViewDto.builder()
            .titles(titles)
            .build();
    }
}
