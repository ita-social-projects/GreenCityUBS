package greencity.service.locations;

import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class LocationApiService {
    private static final String API_URL = "https://directory.org.ua/api/katottg";
    private static final int DEFAULT_PAGE_SIZE = 5000;
    private static final String LEVEL = "level";
    private static final String PAGE = "page";
    private static final String NAME = "name";
    private static final String NAME_EN = "name_en";
    private static final String CODE = "code";
    private static final String PAGE_SIZE = "page_size";
    private static final String UPPER_ID = "parent";
    private LocationDto kyiv;
    private RestTemplate restTemplate;

    /**
     * Constructor for the LocationApiService class.
     *
     * @param restTemplate An instance of RestTemplate for making HTTP requests.
     */
    @Autowired
    public LocationApiService(RestTemplate restTemplate) {
        Map<String, String> name = new HashMap<>();
        name.put(NAME, "Київ");
        name.put(NAME_EN, "Kyiv");
        kyiv = LocationDto.builder()
            .id("UA80000000000093317")
            .parentId(null)
            .name(name)
            .build();
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves a city by its name.
     *
     * @param regionName The name of the region where the city is located.
     * @param cityName   The name of the city.
     * @return A LocationDto object containing the city's data.
     */
    public List<LocationDto> getCitiesByName(String regionName, String cityName) {
        List<LocationDto> allCities = getLocationDataByName(DEFAULT_PAGE_SIZE, 4, cityName);
        if (allCities.isEmpty()) {
            allCities.add(getCityByNameFromRegionSide(regionName, cityName));
            return allCities;
        }
        return allCities.stream()
            .filter(location -> location.getName().containsKey(cityName)
                || location.getName().containsValue(cityName))
            .collect(Collectors.toList());
    }

    /**
     * Finds a location by its name in a list of locations.
     *
     * @param locations    The list of locations to search.
     * @param regionName   The name of the region where the location is located.
     * @param locationName The name of the location.
     * @param errorMessage The error message to use if the location is not found.
     * @return A LocationDto object containing the location's data.
     */
    LocationDto findLocationByName(List<LocationDto> locations, String regionName, String locationName,
        String errorMessage) {
        if (locations.isEmpty()) {
            return getCityByNameFromRegionSide(regionName, locationName);
        }
        return locations.stream()
            .filter(location -> location.getName().containsKey(locationName)
                || location.getName().containsValue(locationName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(errorMessage + locationName));
    }

    /**
     * Retrieves a region by its name.
     *
     * @param regionName The name of the region.
     * @return A LocationDto object containing the region's data.
     */
    public LocationDto getRegionByName(String regionName) {
        List<LocationDto> allRegions = new ArrayList<>();
        try {
            allRegions = getLocationDataByName(DEFAULT_PAGE_SIZE, 1, regionName);
            if (allRegions.isEmpty()) {
                allRegions = getAllRegions();
            }
        } catch (NotFoundException e) {
            allRegions = getAllRegions();
        }

        return allRegions.stream()
            .filter(region -> region.getName().containsKey(regionName) || region.getName()
                .containsValue(regionName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.REGION_NOT_FOUND + regionName));
    }

    /**
     * Retrieves a city in a specific region.
     *
     * @param regionName The name of the region.
     * @param cities     A list of LocationDto objects representing cities.
     * @return A LocationDto object containing the city's data.
     */
    public LocationDto getCityInRegion(String regionName, List<LocationDto> cities) {
        LocationDto region = getRegionByName(regionName);
        String regionID = region.getId();
        for (LocationDto city : cities) {
            LocationDto localCommunity = (getLocationDataByCode(2, 3, city.getParentId()));
            LocationDto districtRegion = (getLocationDataByCode(2, 2, localCommunity.getParentId()));
            if (districtRegion.getParentId().equals(regionID)) {
                return city;
            }
        }
        throw new NotFoundException(ErrorMessage.CITY_NOT_FOUND_IN_REGION + regionID);
    }

    /**
     * Retrieves a city by its name from the regional side.
     *
     * @param regionName The name of the region where the city is located.
     * @param cityName   The name of the city.
     * @return A LocationDto object containing the city's data.
     */
    public LocationDto getCityByNameFromRegionSide(String regionName, String cityName) {
        LocationDto region = getRegionByName(regionName);
        List<LocationDto> districts = getAllDistrictInTheRegionsById(region.getId());
        List<LocationDto> localCommunities = districts.stream()
            .flatMap(district -> getAllLocalCommunitiesById(district.getId()).stream())
            .collect(Collectors.toList());
        List<LocationDto> cities = localCommunities.stream()
            .flatMap(community -> getAllCitiesById(community.getId()).stream())
            .collect(Collectors.toList());
        return findLocationByName(cities, regionName, cityName, ErrorMessage.CITY_NOT_FOUND);
    }

    /**
     * Fetches districts in a city. Directly retrieves for Kyiv, otherwise searches
     * by city name in a given region. If no districts are found, attempts to search
     * again using the Ukrainian name. If still unsuccessful, returns city itself.
     *
     * @param regionName The region name.
     * @param cityName   The city name.
     * @return List of districts, or the city itself if no districts are found.
     */
    public List<LocationDto> getAllDistrictsInCityByNames(String regionName, String cityName) {
        if (cityName.equals(kyiv.getName().get(NAME)) || cityName.equals(kyiv.getName().get(NAME_EN))) {
            return getAllDistrictsInCityByCityID(kyiv.getId());
        }
        List<LocationDto> cities = getCitiesByName(regionName, cityName);
        LocationDto city = getCityInRegion(regionName, cities);
        String cityId = city.getId();
        List<LocationDto> allDistricts = getAllDistrictsInCityByCityID(cityId);
        if (allDistricts.isEmpty()) {
            String cityNameUK = city.getName().get(NAME);
            city = getCitiesByName(regionName, cityNameUK).get(0);
            allDistricts = getAllDistrictsInCityByCityID(city.getId());
        }
        if (allDistricts.isEmpty()) {
            allDistricts.add(city);
        }
        return allDistricts;
    }

    /**
     * Retrieves all regions.
     *
     * @return A list of all regions.
     */
    public List<LocationDto> getAllRegions() {
        return getLocationDataByLevel(DEFAULT_PAGE_SIZE, LocationDivision.REGION.getLevelId());
    }

    /**
     * Retrieves all cities in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all cities in the specified region.
     */
    public List<LocationDto> getAllCitiesById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, LocationDivision.CITY.getLevelId(), upperId);
    }

    /**
     * Retrieves all districts in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all districts in the specified region.
     */
    public List<LocationDto> getAllDistrictInTheRegionsById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, LocationDivision.DISTRICT_IN_REGION.getLevelId(), upperId);
    }

    /**
     * Retrieves all local communities in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all local communities in the specified region.
     */
    public List<LocationDto> getAllLocalCommunitiesById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, LocationDivision.LOCAL_COMMUNITY.getLevelId(), upperId);
    }

    /**
     * Fetches a list of all districts in a city specified by the upper ID.
     *
     * @param upperId The ID of the city.
     * @return A List of LocationDto objects, each representing a district in the
     *         city.
     */
    public List<LocationDto> getAllDistrictsInCityByCityID(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, LocationDivision.DISTRICT_IN_CITY.getLevelId(), upperId);
    }

    /**
     * Fetches location data by the provided code.
     *
     * @param pageSize The maximum number of results to return.
     * @param level    The hierarchical level of the location (e.g., city, region).
     * @param code     The code representing the specific location to fetch.
     * @return A LocationDto object representing the location matching the provided
     *         code.
     */
    private LocationDto getLocationDataByCode(int pageSize, int level, String code) {
        UriComponentsBuilder builder = builderUrl(pageSize).queryParam(CODE, code)
            .queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri()).get(0);
    }

    /**
     * Fetches all location data at the provided level.
     *
     * @param pageSize The maximum number of results to return.
     * @param level    The hierarchical level of the locations to fetch (e.g., city,
     *                 region).
     * @return A List of LocationDto objects representing all locations at the
     *         provided level.
     */
    private List<LocationDto> getLocationDataByLevel(int pageSize, int level) {
        UriComponentsBuilder builder = builderUrl(pageSize).queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * Fetches location data by the provided name.
     *
     * @param pageSize The maximum number of results to return.
     * @param level    The hierarchical level of the location to fetch (e.g., city,
     *                 region).
     * @param name     The name of the location to fetch.
     * @return A List of LocationDto objects representing the locations matching the
     *         provided name.
     * @throws NotFoundException if no location matching the provided name and level
     *                           can be found.
     */
    public List<LocationDto> getLocationDataByName(int pageSize, int level, String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name parameter cannot be null");
        }
        UriComponentsBuilder builder = builderUrl(pageSize)
            .queryParam(NAME, name)
            .queryParam(LEVEL, level);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * Fetches the result from a given URL and maps it into a list of LocationDto
     * objects.
     *
     * @param url the URL to fetch results from.
     * @return a List of LocationDto objects.
     * @throws RuntimeException if the restTemplate fails to get a response from the
     *                          provided URL.
     */
    public List<LocationDto> getResultFromUrl(URI url) {
        try {
            ParameterizedTypeReference<Map<String, Object>> typeRef =
                new ParameterizedTypeReference<Map<String, Object>>() {
                };
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, typeRef);
            return Optional.ofNullable(response)
                .map(ResponseEntity::getBody)
                .map(body -> (List<Map<String, Object>>) body.get("results"))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url))
                .stream()
                .map(this::mapToLocationDto)
                .collect(Collectors.toList());
        } catch (RestClientException e) {
            throw new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url);
        }
    }

    /**
     * Maps a result map into a LocationDto object.
     *
     * @param result a map containing the result data.
     * @return a LocationDto object built from the result map.
     */
    private LocationDto mapToLocationDto(Map<String, Object> result) {
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put(NAME, getValueFromMap(result, NAME));
        nameMap.put(NAME_EN, getValueFromMap(result, NAME_EN));
        return LocationDto.builder()
            .id(getValueFromMap(result, CODE))
            .parentId(getValueFromMap(result, "parent_id"))
            .name(nameMap)
            .build();
    }

    /**
     * Gets a value from a map using the provided key.
     *
     * @param map the map from which to get the value.
     * @param key the key of the value to get.
     * @return the value associated with the provided key.
     */
    private <T> T getValueFromMap(Map<String, Object> map, String key) {
        return (T) map.get(key);
    }

    /**
     * Fetches a list of location data specified by upper ID and level.
     *
     * @param pageSize The number of locations to fetch.
     * @param level    The level of location to fetch.
     * @param upperId  The upper ID of the location.
     * @return A List of LocationDto objects, each representing a location matching
     *         the provided parameters.
     */
    private List<LocationDto> getLocationDataByUpperId(int pageSize, int level, String upperId) {
        if (upperId == null) {
            throw new IllegalArgumentException("The upperId parameter cannot be null");
        }
        UriComponentsBuilder builder = builderUrl(pageSize).queryParam(LEVEL, level)
            .queryParam(UPPER_ID, upperId);
        return getResultFromUrl(builder.build().encode().toUri());
    }

    /**
     * Builds the URL for an API endpoint using the provided page size. This method
     * uses UriComponentsBuilder to construct a URL with two query parameters: -
     * PAGE set to "1" - PAGE_SIZE set to the provided page size.
     *
     * @param pageSize The number of results to display per page.
     * @return A UriComponentsBuilder instance with the built URL.
     */
    public UriComponentsBuilder builderUrl(int pageSize) {
        return UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam(PAGE, "1")
            .queryParam(PAGE_SIZE, pageSize);
    }
}
