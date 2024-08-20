package greencity.service.cache;

import greencity.dto.CityAndDistrictDto;
import greencity.dto.OptionForColumnDTO;
import java.util.List;

public interface CacheService {
	List<OptionForColumnDTO> findAllCities();

	List<OptionForColumnDTO> findAllDistricts();

	List<CityAndDistrictDto> findAllCitiesAndDistricts();
}
