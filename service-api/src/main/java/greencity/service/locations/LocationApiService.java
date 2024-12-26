package greencity.service.locations;

import greencity.dto.location.api.LocationDto;
import greencity.exceptions.NotFoundException;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.util.List;

@Service
@EnableCaching
public interface LocationApiService {
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
    List<LocationDto> getAllDistrictsInCityByNames(String regionName, String cityName);

    /**
     * Retrieves a list of cities by name.
     *
     * @param regionName The name of the region.
     * @param cityName   The name of the city.
     * @return A list of matching city locations.
     */
    List<LocationDto> getCitiesByName(String regionName, String cityName);

    /**
     * Finds a location by its name.
     *
     * @param locations    The list of locations.
     * @param locationName The location name.
     * @return A LocationDto matching the provided name.
     */
    LocationDto findLocationByName(List<LocationDto> locations, String locationName);

    /**
     * Retrieves a city by name from a specified region.
     *
     * @param regionName The name of the region.
     * @param cityName   The name of the city.
     * @return The LocationDto that represents the city in the specified region.
     * @throws NotFoundException if the city is not found.
     */
    LocationDto getCityByNameFromRegionSide(String regionName, String cityName);

    /**
     * Retrieves a region by name.
     *
     * @param regionName The name of the region.
     * @return The region matching the provided name.
     */
    LocationDto getRegionByName(String regionName);

    /**
     * Retrieves the city in a specified region.
     *
     * @param regionName The name of the region.
     * @param cities     A list of LocationDto that represent cities.
     * @return The LocationDto that represents the city in the specified region.
     * @throws NotFoundException if the city is not found in the region.
     */
    LocationDto getCityInRegion(String regionName, List<LocationDto> cities);

    /**
     * Retrieves all regions.
     *
     * @return A list of LocationDto that represent all regions.
     */
    List<LocationDto> getAllRegions();

    /**
     * Retrieves all cities by their ID.
     *
     * @param upperId The ID of the city.
     * @return A list of LocationDto that represent the cities.
     */
    List<LocationDto> getAllCitiesById(String upperId);

    /**
     * Retrieves all districts in the region by the region's ID.
     *
     * @param upperId The ID of the region.
     * @return A list of LocationDto that represent districts in the region.
     */
    List<LocationDto> getAllDistrictInTheRegionsById(String upperId);

    /**
     * Retrieves all local communities by their ID.
     *
     * @param upperId The ID of the local community.
     * @return A list of LocationDto that represent local communities.
     */
    List<LocationDto> getAllLocalCommunitiesById(String upperId);

    /**
     * Retrieves all districts in a city by the city's ID.
     *
     * @param upperId The ID of the city.
     * @return A list of LocationDto that represent districts in the city.
     */
    List<LocationDto> getAllDistrictsInCityByCityID(String upperId);

    /**
     * Retrieves location data by level and code.
     *
     * @param level The hierarchical level of the location.
     * @param code  The code of the location.
     * @return The LocationDto that matches the specified level and code.
     * @throws NotFoundException if the location is not found.
     */
    LocationDto getLocationDataByCode(int level, String code);

    /**
     * Fetches a list of location data by level.
     *
     * @param level The level of the location.
     * @return A list of LocationDto for the specified level.
     */
    List<LocationDto> getLocationDataByLevel(int level);

    /**
     * Retrieves a list of location data by level and name.
     *
     * @param level The hierarchical level of the location.
     * @param name  The name of the location.
     * @return A list of LocationDto that matches the specified level and name.
     * @throws IllegalArgumentException if the name is null.
     */
    List<LocationDto> getLocationDataByName(int level, String name);

    /**
     * Extracts location data from a URL.
     *
     * @param url The URL to retrieve the data from.
     * @return A list of LocationDto.
     */
    List<LocationDto> getResultFromUrl(URI url);

    /**
     * Retrieves location data by its upper Id.
     *
     * @param level   The hierarchical level of the location.
     * @param upperId The upperId associated with the location.
     * @return A list of LocationDto associated with the specified upperId.
     * @throws IllegalArgumentException if the upperId is null.
     */
    List<LocationDto> getLocationDataByUpperId(int level, String upperId);
}
