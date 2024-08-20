package greencity.service.cache;

import greencity.dto.CityAndDistrictDto;
import greencity.dto.OptionForColumnDTO;
import greencity.repository.AddressRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {
	private final AddressRepository addressRepository;

	@Override
	@Cacheable("cities")
	public List<OptionForColumnDTO> findAllCities() {
		log.warn("In method cities!!!");
		return findAllCitiesAndDistricts().stream()
			.collect(Collectors.toMap(
				CityAndDistrictDto::getCity,
				district -> district,
				(existing, replacement) -> existing
			)).values()
			.stream()
			.map(source -> OptionForColumnDTO.builder()
				.key(source.getId().toString())
				.en(source.getCityEn())
				.ua(source.getCity())
				.build())
			.toList();
	}

	@Override
	@Cacheable("districts")
	public List<OptionForColumnDTO> findAllDistricts() {
		log.warn("In method districts!!!");
		return findAllCitiesAndDistricts().stream()
			.collect(Collectors.toMap(
				CityAndDistrictDto::getDistrict,
				district -> district,
				(existing, replacement) -> existing
			)).values()
			.stream()
			.map(source -> OptionForColumnDTO.builder()
				.key(source.getId().toString())
				.en(source.getDistrictEn())
				.ua(source.getDistrict())
				.build())
			.toList();
	}

	@Override
	@Cacheable("allData")
	public List<CityAndDistrictDto> findAllCitiesAndDistricts() {
		log.warn("In method allData!!!");
		return addressRepository.findAllCitiesAndDistricts();
	}
}
