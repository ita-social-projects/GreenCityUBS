package greencity.service.locations;

import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.exceptions.NotFoundException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Retrieves a city by its name.
     *
     * @param regionName The name of the region where the city is located.
     * @param cityName   The name of the city.
     * @return A LocationDto object containing the city's data.
     */
    public LocationDto getCityByName(String regionName, String cityName) {
        List<LocationDto> allCities = getLocationDataByName(DEFAULT_PAGE_SIZE, 4, cityName);
        return findLocationByName(allCities, regionName, cityName, ErrorMessage.CITY_NOT_FOUND);
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
    private LocationDto findLocationByName(List<LocationDto> locations, String regionName, String locationName,
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
        } catch (NotFoundException e) {
            allRegions = getAllRegions();
        }
        if (allRegions.isEmpty()) {
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
        throw new NotFoundException(ErrorMessage.CITY_NOT_FOUND + cities.get(0).getName());
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
     * Retrieves a city in a specific region by its name.
     *
     * @param regionName The name of the region where the city is located.
     * @param cityName   The name of the city.
     * @return A LocationDto object containing the city's data.
     */
    public List<LocationDto> getAllDistrictsInCityByNames(String regionName, String cityName) {
        if (cityName.equals("Київ") || cityName.equals("Kyiv")) {
            return getAllDistrictsInCityByCityID("UA80000000000093317");
        }
        List<LocationDto> allDistricts = new ArrayList<>();
        LocationDto city = getCityByName(regionName, cityName);
        String cityId = city.getId();
        allDistricts = getAllDistrictsInCityByCityID(cityId);
        if (allDistricts.isEmpty()) {
            String cityNameUK = city.getName().get("name");
            city = getCityByName(regionName, cityNameUK);
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
        return getLocationDataByLevel(DEFAULT_PAGE_SIZE, 1);
    }

    /**
     * Retrieves all cities in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all cities in the specified region.
     */
    public List<LocationDto> getAllCitiesById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, 4, upperId);
    }

    /**
     * Retrieves all districts in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all districts in the specified region.
     */
    public List<LocationDto> getAllDistrictInTheRegionsById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, 2, upperId);
    }

    /**
     * Retrieves all local communities in a specific region.
     *
     * @param upperId The id of the upper region.
     * @return A list of all local communities in the specified region.
     */
    public List<LocationDto> getAllLocalCommunitiesById(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, 3, upperId);
    }

    /**
     * Fetches a list of all districts in a city specified by the upper ID.
     *
     * @param upperId The ID of the city.
     * @return A List of LocationDto objects, each representing a district in the
     *         city.
     */
    public List<LocationDto> getAllDistrictsInCityByCityID(String upperId) {
        return getLocationDataByUpperId(DEFAULT_PAGE_SIZE, 5, upperId);
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
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("page", "1")
                .queryParam("page_size", pageSize)
                .queryParam("code", code)
                .queryParam("level", level);
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
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("page", "1")
                .queryParam("page_size", pageSize)
                .queryParam("level", level);
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
    private List<LocationDto> getLocationDataByName(int pageSize, int level, String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name parameter cannot be null");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("page", "1")
            .queryParam("page_size", pageSize)
            .queryParam("name", name)
            .queryParam("level", level);
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
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return Optional.ofNullable(response)
                .map(ResponseEntity::getBody)
                .map(body -> (List<Map<String, Object>>) body.get("results"))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_LOCATION_BY_URL + url))
                .stream()
                .map(this::mapToLocationDto)
                .collect(Collectors.toList());
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to get response from " + url, e);
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
        nameMap.put("name", getValueFromMap(result, "name"));
        nameMap.put("name_en", getValueFromMap(result, "name_en"));
        return LocationDto.builder()
            .id(getValueFromMap(result, "code"))
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("page", "1")
            .queryParam("page_size", pageSize)

            .queryParam("level", level)
            .queryParam("parent", upperId);
        return getResultFromUrl(builder.build().encode().toUri());
    }
}
