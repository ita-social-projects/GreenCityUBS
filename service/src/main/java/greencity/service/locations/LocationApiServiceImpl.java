package greencity.service.locations;

import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;

@Service
@EnableCaching
public class LocationApiServiceImpl implements LocationApiService {
    private static final String API_URL = "https://directory.org.ua/api/katottg";
    private static final int DEFAULT_PAGE_SIZE = 125;
    private static final String LEVEL = "level";
    private static final String NAME = "name";
    private static final String NAME_EN = "name_en";
    private static final String CODE = "code";
    private static final String PAGE_SIZE = "page_size";
    private static final String PARENT = "parent";
    private static final String PARENT_ID = "parent_id";
    private static final String RESULTS = "results";
    private static final String NAME_KYIV_UA = "Київ";
    private static final String NAME_KYIV_EN = "Kyiv";
    private static final String KYIV_ID = "UA80000000000093317";
    private static final LocationDto KYIV = LocationDto.builder()
        .id(KYIV_ID)
        .locationNameMap(Map.of(NAME, NAME_KYIV_UA, NAME_EN, NAME_KYIV_EN))
        .build();
    private final RestTemplate restTemplate;

    /**
     * Constructor for the LocationApiService class.
     *
     * @param restTemplate An instance of RestTemplate for making HTTP requests.
     */
    @Autowired
    public LocationApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "districtList", key = "#regionName + '_' + #cityName")
    public List<LocationDto> getAllDistrictsInCityByNames(String regionName, String cityName) {
        checkIfNotNull(regionName, cityName);
        regionName = removeWordRegion(regionName);
        cityName = removeWordCity(cityName);
        if (cityName.equals(KYIV.getLocationNameMap().get(NAME))
            || cityName.equals(KYIV.getLocationNameMap().get(NAME_EN))) {
            return getAllDistrictsInCityByCityID(KYIV.getId());
        }

        List<LocationDto> cities = getCitiesByName(regionName, cityName);
        LocationDto city = getCityInRegion(regionName, cities);
        String cityId = city.getId();
        List<LocationDto> allDistricts = getAllDistrictsInCityByCityID(cityId);
        if (allDistricts.isEmpty()) {
            return List.of(city);
        }
        return allDistricts;
    }

    static String replaceAllQuotes(String input) {
        Pattern pattern = Pattern.compile("[`'‘’“”‛‟ʼ«»\"]");
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "’");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "cityList", key = "#regionName + '_' + #cityName ")
    public List<LocationDto> getCitiesByName(String regionName, String cityName) {
        List<LocationDto> allCities = getLocationDataByName(LocationDivision.CITY.getLevelId(), cityName);
        if (allCities.isEmpty()) {
            allCities.add(getCityByNameFromRegionSide(regionName, cityName));
            return allCities;
        }
        return allCities.stream()
            .filter(location -> location.getLocationNameMap().containsValue(cityName))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "locationByName", key = "#locations.size() + '_' + #locationName")
    public LocationDto findLocationByName(List<LocationDto> locations, String locationName) {
        return locations.stream()
            .filter(location -> location.getLocationNameMap().containsValue(locationName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CITY_NOT_FOUND + locationName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "cityByNameFromRegionSide", key = "#regionName + '_' + #cityName ")
    public LocationDto getCityByNameFromRegionSide(String regionName, String cityName) {
        LocationDto region = getRegionByName(regionName);
        List<LocationDto> districts = getAllDistrictInTheRegionsById(region.getId());
        List<LocationDto> localCommunities = districts.stream()
            .flatMap(district -> getAllLocalCommunitiesById(district.getId()).stream())
            .toList();
        List<LocationDto> cities = localCommunities.stream()
            .flatMap(community -> getAllCitiesById(community.getId()).stream())
            .collect(Collectors.toList());
        if (cities.isEmpty()) {
            throw new NotFoundException(ErrorMessage.CITY_NOT_FOUND + cityName);
        }
        return findLocationByName(cities, cityName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "regionByName", key = "#regionName")
    public LocationDto getRegionByName(String regionName) {
        List<LocationDto> allRegions;
        try {
            allRegions = getLocationDataByName(LocationDivision.REGION.getLevelId(), regionName);
            if (allRegions.isEmpty()) {
                allRegions = getAllRegions();
            }
        } catch (NotFoundException e) {
            allRegions = getAllRegions();
        }

        return allRegions.stream()
            .filter(region -> region.getLocationNameMap().containsValue(regionName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.REGION_NOT_FOUND + regionName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "cityInRegion", key = "#regionName +'_'+#cities.size()")
    public LocationDto getCityInRegion(String regionName, List<LocationDto> cities) {
        LocationDto region = getRegionByName(regionName);
        String regionID = region.getId();
        return cities.stream()
            .filter(city -> {
                if (checkIfRegionIdEqualsUpperId(region.getId(), city.getParentId())) {
                    return true;
                }
                LocationDto localCommunity =
                    getLocationDataByCode(LocationDivision.LOCAL_COMMUNITY.getLevelId(), city.getParentId());
                LocationDto districtRegion = getLocationDataByCode(LocationDivision.DISTRICT_IN_REGION.getLevelId(),
                    localCommunity.getParentId());
                return districtRegion.getParentId().equals(regionID);
            })
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CITY_NOT_FOUND_IN_REGION + regionID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "allRegions", key = "allRegions")
    public List<LocationDto> getAllRegions() {
        return getLocationDataByLevel(LocationDivision.REGION.getLevelId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "allCitiesById", key = "#upperId")
    public List<LocationDto> getAllCitiesById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.CITY.getLevelId(), upperId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "allDistrictInTheRegionsById", key = "#upperId")
    public List<LocationDto> getAllDistrictInTheRegionsById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.DISTRICT_IN_REGION.getLevelId(), upperId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "allLocalCommunitiesById", key = "#upperId")
    public List<LocationDto> getAllLocalCommunitiesById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.LOCAL_COMMUNITY.getLevelId(), upperId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "allDistrictsInCityByCityID", key = "#upperId")
    public List<LocationDto> getAllDistrictsInCityByCityID(String upperId) {
        return getLocationDataByUpperId(LocationDivision.DISTRICT_IN_CITY.getLevelId(), upperId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "locationDataByCode", key = "#level+'_'+#code")
    public LocationDto getLocationDataByCode(int level, String code) {
        UriComponentsBuilder builder = buildUrl()
            .queryParam(CODE, code)
            .queryParam(LEVEL, level);
        List<LocationDto> resultFromUrl = getResultFromUrl(builder.build().encode().toUri());
        if (CollectionUtils.isEmpty(resultFromUrl)) {
            throw new NotFoundException(
                String.format(ErrorMessage.NOT_FOUND_LOCATION_ON_LEVEL_AND_BY_CODE, level, code));
        }
        return resultFromUrl.getFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "locationDataByLevel", key = "#level")
    public List<LocationDto> getLocationDataByLevel(int level) {
        UriComponentsBuilder builder = buildUrl().queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    private static boolean checkIfNotNull(String... names) {
        if (Arrays.stream(names).anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException(ErrorMessage.VALUE_CAN_NOT_BE_NULL_OR_EMPTY);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "locationDataByName", key = "#level+'_'+#name")
    public List<LocationDto> getLocationDataByName(int level, String name) {
        UriComponentsBuilder builder = buildUrl()
            .queryParam(NAME, name)
            .queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "resultFromUrl", key = "#url")
    public List<LocationDto> getResultFromUrl(URI url) {
        ParameterizedTypeReference<Map<String, Object>> typeRef =
            new ParameterizedTypeReference<>() {
            };
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);

        if (response == null || response.getBody() == null) {
            throw new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url);
        }

        return Optional.of(response)
            .map(ResponseEntity::getBody)
            .map(body -> (List<Map<String, Object>>) body.get(RESULTS))
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url))
            .stream()
            .map(this::mapToLocationDto)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "locationDataByUpperId", key = "#level+'_'+#upperId")
    public List<LocationDto> getLocationDataByUpperId(int level, String upperId) {
        UriComponentsBuilder builder = buildUrl().queryParam(LEVEL, level)
            .queryParam(PARENT, upperId);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    private UriComponentsBuilder buildUrl() {
        return UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }

    private LocationDto mapToLocationDto(Map<String, Object> result) {
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(NAME, getValueFromMap(result, NAME));
        nameMap.put(NAME_EN, getValueFromMap(result, NAME_EN));
        return LocationDto.builder()
            .id(getValueFromMap(result, CODE))
            .parentId(getValueFromMap(result, PARENT_ID))
            .locationNameMap(nameMap)
            .build();
    }

    private <T> T getValueFromMap(Map<String, Object> map, String key) {
        return (T) map.get(key);
    }

    private boolean checkIfRegionIdEqualsUpperId(String regionId, String cityUpperId) {
        return regionId.equals(cityUpperId);
    }

    private static String removeWordRegion(String sentence) {
        String withoutSpaces = sentence.replace(" ", "");
        String withoutRegion = withoutSpaces.replaceAll("(?iu)region", "");
        return replaceAllQuotes(withoutRegion.replaceAll("(?iu)область", ""));
    }

    private static String removeWordCity(String sentence) {
        String withoutSpaces = sentence.trim();
        String withoutRegion = withoutSpaces.replaceAll("(?iu)city", "");
        return replaceAllQuotes(withoutRegion.replaceAll("(?iu)місто", ""));
    }
}
