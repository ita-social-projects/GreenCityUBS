package greencity.service.locations;

import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;

@Service
public class LocationApiService {
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
    private RestTemplate restTemplate;

    /**
     * Constructor for the LocationApiService class.
     *
     * @param restTemplate An instance of RestTemplate for making HTTP requests.
     */
    @Autowired
    public LocationApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves all districts in a city by the city's name. There's a special case
     * for Kyiv, the capital of Ukraine. In the system, due to the API having
     * references only to the previous element, the search for districts occurs
     * sequentially: region -> district in region -> local community -> city ->
     * district. Generally, the city corresponds to level 4 in the hierarchical
     * structure. However, Kyiv is unique in that it is at level 1, and its
     * districts are at level 5. Therefore, a separate logic is implemented because
     * the system can't go through all the steps from level 1 to level 4, and has to
     * directly access the districts from level 5 when dealing with Kyiv.
     *
     * @param regionName The name of the region.
     * @param cityName   The name of the city.
     * @return A list of LocationDto that represent districts in the city.
     */
    public List<LocationDto> getAllDistrictsInCityByNames(String regionName, String cityName) {
        checkIfNotNull(regionName, cityName);
        if (cityName.equals(KYIV.getLocationNameMap().get(NAME))
            || cityName.equals(KYIV.getLocationNameMap().get(NAME_EN))) {
            return getAllDistrictsInCityByCityID(KYIV.getId());
        }
        List<LocationDto> cities = getCitiesByName(regionName, cityName);
        LocationDto city = getCityInRegion(regionName, cities);
        String cityId = city.getId();
        List<LocationDto> allDistricts = getAllDistrictsInCityByCityID(cityId);
        if (allDistricts.isEmpty()) {
            allDistricts.add(city);
        }
        return allDistricts;
    }

    /**
     * Retrieves a list of cities by name.
     *
     * @param regionName The name of the region.
     * @param cityName   The name of the city.
     * @return A list of matching city locations.
     */
    private List<LocationDto> getCitiesByName(String regionName, String cityName) {
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
     * Finds a location by its name.
     *
     * @param locations    The list of locations.
     * @param locationName The location name.
     * @return A LocationDto matching the provided name.
     */
    private LocationDto findLocationByName(List<LocationDto> locations, String locationName) {
        return locations.stream()
            .filter(location -> location.getLocationNameMap().containsValue(locationName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.CITY_NOT_FOUND + locationName));
    }

    /**
     * Retrieves a city by name from a specified region.
     *
     * @param regionName The name of the region.
     * @param cityName   The name of the city.
     * @return The LocationDto that represents the city in the specified region.
     * @throws NotFoundException if the city is not found.
     */
    private LocationDto getCityByNameFromRegionSide(String regionName, String cityName) {
        LocationDto region = getRegionByName(regionName);
        List<LocationDto> districts = getAllDistrictInTheRegionsById(region.getId());
        List<LocationDto> localCommunities = districts.stream()
            .flatMap(district -> getAllLocalCommunitiesById(district.getId()).stream())
            .collect(Collectors.toList());
        List<LocationDto> cities = localCommunities.stream()
            .flatMap(community -> getAllCitiesById(community.getId()).stream())
            .collect(Collectors.toList());
        if (cities.isEmpty()) {
            throw new NotFoundException(ErrorMessage.CITY_NOT_FOUND + cityName);
        }
        return findLocationByName(cities, cityName);
    }

    /**
     * Retrieves a region by name.
     *
     * @param regionName The name of the region.
     * @return The region matching the provided name.
     */
    private LocationDto getRegionByName(String regionName) {
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
     * Retrieves the city in a specified region.
     *
     * @param regionName The name of the region.
     * @param cities     A list of LocationDto that represent cities.
     * @return The LocationDto that represents the city in the specified region.
     * @throws NotFoundException if the city is not found in the region.
     */
    private LocationDto getCityInRegion(String regionName, List<LocationDto> cities) {
        LocationDto region = getRegionByName(regionName);
        String regionID = region.getId();
        for (LocationDto city : cities) {
            LocationDto localCommunity =
                (getLocationDataByCode(LocationDivision.LOCAL_COMMUNITY.getLevelId(), city.getParentId()));
            LocationDto districtRegion =
                (getLocationDataByCode(LocationDivision.DISTRICT_IN_REGION.getLevelId(), localCommunity.getParentId()));
            if (districtRegion.getParentId().equals(regionID)) {
                return city;
            }
        }
        throw new NotFoundException(ErrorMessage.CITY_NOT_FOUND_IN_REGION + regionID);
    }

    /**
     * Retrieves all regions.
     *
     * @return A list of LocationDto that represent all regions.
     */

    private List<LocationDto> getAllRegions() {
        return getLocationDataByLevel(LocationDivision.REGION.getLevelId());
    }

    /**
     * Retrieves all cities by their ID.
     *
     * @param upperId The ID of the city.
     * @return A list of LocationDto that represent the cities.
     */

    private List<LocationDto> getAllCitiesById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.CITY.getLevelId(), upperId);
    }

    /**
     * Retrieves all districts in the region by the region's ID.
     *
     * @param upperId The ID of the region.
     * @return A list of LocationDto that represent districts in the region.
     */
    private List<LocationDto> getAllDistrictInTheRegionsById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.DISTRICT_IN_REGION.getLevelId(), upperId);
    }

    /**
     * Retrieves all local communities by their ID.
     *
     * @param upperId The ID of the local community.
     * @return A list of LocationDto that represent local communities.
     */
    private List<LocationDto> getAllLocalCommunitiesById(String upperId) {
        return getLocationDataByUpperId(LocationDivision.LOCAL_COMMUNITY.getLevelId(), upperId);
    }

    /**
     * Retrieves all districts in a city by the city's ID.
     *
     * @param upperId The ID of the city.
     * @return A list of LocationDto that represent districts in the city.
     */
    private List<LocationDto> getAllDistrictsInCityByCityID(String upperId) {
        return getLocationDataByUpperId(LocationDivision.DISTRICT_IN_CITY.getLevelId(), upperId);
    }

    /**
     * Retrieves location data by level and code.
     *
     * @param level The hierarchical level of the location.
     * @param code  The code of the location.
     * @return The LocationDto that matches the specified level and code.
     * @throws NotFoundException if the location is not found.
     */
    private LocationDto getLocationDataByCode(int level, String code) {
        UriComponentsBuilder builder = buildUrl()
            .queryParam(CODE, code)
            .queryParam(LEVEL, level);
        List<LocationDto> resultFromUrl = getResultFromUrl(builder.build().encode().toUri());
        if (CollectionUtils.isEmpty(resultFromUrl)) {
            throw new NotFoundException(
                String.format(ErrorMessage.NOT_FOUND_LOCATION_ON_LEVEL_AND_BY_CODE, level, code));
        }
        return resultFromUrl.get(0);
    }

    /**
     * Fetches a list of location data by level.
     *
     * @param level The level of the location.
     * @return A list of LocationDto for the specified level.
     */
    private List<LocationDto> getLocationDataByLevel(int level) {
        UriComponentsBuilder builder = buildUrl().queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    private boolean checkIfNotNull(String... names) {
        for (String name : names) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException(ErrorMessage.VALUE_CAN_NOT_BE_NULL_OR_EMPTY);
            }
        }
        return true;
    }

    /**
     * Retrieves a list of location data by level and name.
     *
     * @param level The hierarchical level of the location.
     * @param name  The name of the location.
     * @return A list of LocationDto that matches the specified level and name.
     * @throws IllegalArgumentException if the name is null.
     */

    private List<LocationDto> getLocationDataByName(int level, String name) {
        UriComponentsBuilder builder = buildUrl()
            .queryParam(NAME, name)
            .queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * Extracts location data from a URL.
     *
     * @param url The URL to retrieve the data from.
     * @return A list of LocationDto.
     */
    private List<LocationDto> getResultFromUrl(URI url) {
        ParameterizedTypeReference<Map<String, Object>> typeRef =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
        return Optional.ofNullable(response)
            .map(ResponseEntity::getBody)
            .map(body -> (List<Map<String, Object>>) body.get(RESULTS))
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url))
            .stream()
            .map(this::mapToLocationDto)
            .collect(Collectors.toList());
    }

    /**
     * Transforms a map into a LocationDto.
     *
     * @param result The map with location data.
     * @return The transformed LocationDto.
     */
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

    /**
     * Retrieves a value from a map based on a specified key.
     *
     * @param <T> The type of the object being returned.
     * @param map The map from which to retrieve the value.
     * @param key The key associated with the value to retrieve.
     * @return The value associated with the specified key.
     */
    private <T> T getValueFromMap(Map<String, Object> map, String key) {
        return (T) map.get(key);
    }

    /**
     * Retrieves location data by its upper Id.
     *
     * @param level   The hierarchical level of the location.
     * @param upperId The upperId associated with the location.
     * @return A list of LocationDto associated with the specified upperId.
     * @throws IllegalArgumentException if the upperId is null.
     */
    private List<LocationDto> getLocationDataByUpperId(int level, String upperId) {
        UriComponentsBuilder builder = buildUrl().queryParam(LEVEL, level)
            .queryParam(PARENT, upperId);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * Constructs a URL using UriComponentsBuilder.
     *
     * @return A UriComponentsBuilder instance with the API URL, page, and page size
     *         parameters set.
     */
    private UriComponentsBuilder buildUrl() {
        return UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }
}
