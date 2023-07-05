package greencity.service.locations;

import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationApiServiceTest {
    private static final String API_URL = "https://directory.org.ua/api/katottg";
    private LocationApiService locationApiService;
    RestTemplate restTemplate;
    private static final String PAGE_VALUE = "1";
    private static final String PAGE_SIZE_VALUE = "100";
    private static final String LEVEL = "level";
    private static final String PAGE = "page";
    private static final String NAME = "name";
    private static final String NAME_EN = "name_en";
    private static final String CODE = "code";
    private static final String PAGE_SIZE = "page_size";
    private static final String PARENT = "parent";
    private static final String PARENT_ID = "parent_id";
    private static final Map<String, Object> autonomousRepublicResult =
        getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
    private static final Map<String, Object> lvivskaResult =
        getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
    private static final Map<String, Object> lvivDistrictResult =
        getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
    private static final Map<String, Object> kulykivskaResult =
        getApiResult("UA46060230000093092", "UA46060000000042587", "Куликівська", "Kulykivska");
    private static final Map<String, Object> kulykivResult =
        getApiResult("UA46060230010099970", "UA46060230000093092", "Куликів", "Kulykiv");
    private static final Map<String, Object> vidnivResult =
        getApiResult("UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");
    private static final Map<String, Object> lvivska2Result =
        getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");

    private static final Map<String, Object> lvivResult =
        getApiResult("UA46060250010015970", "UA05020030000031457", "Львів", "Lviv");

    private static final Map<String, Object> halytskyiResult =
        getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
    private static final Map<String, Object> zaliznychnyiResult =
        getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");
    private static final Map<String, Object> kyivResult = getApiResult("UA80000000000093317", null, "Київ", "Kyiv");
    private static final Map<String, Object> holosiivskyiResult =
        getApiResult("UA80000000000126643", "UA80000000000093317", "Голосіївський", "Holosiivskyi");
    private static final Map<String, Object> darnytskyiResult =
        getApiResult("UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    static Map<String, Object> getApiResult(String code, String parent_id, String name, String nameEn) {
        Map<String, Object> apiResult = new HashMap<>();
        apiResult.put(CODE, code);
        apiResult.put(PARENT_ID, parent_id);
        apiResult.put(NAME, name);
        apiResult.put(NAME_EN, nameEn);
        return apiResult;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private UriComponentsBuilder buildCode(String code, int level) {
        return UriComponentsBuilder
            .fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, code)
            .queryParam(LEVEL, level);
    }

    private UriComponentsBuilder buildParent(int level, String parent) {
        return UriComponentsBuilder
            .fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, level)
            .queryParam(PARENT, parent);
    }

    private UriComponentsBuilder build(int level) {
        return UriComponentsBuilder
            .fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, level);
    }

    private UriComponentsBuilder buildName(String name, int level) {
        return UriComponentsBuilder
            .fromHttpUrl(API_URL)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, name)
            .queryParam(LEVEL, level);
    }

    private void response(UriComponentsBuilder builder, List<Map<String, Object>> list) {
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(list));
    }

    void initialise() {
        UriComponentsBuilder level1Builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder level2BuilderLviv =
            buildCode("UA46060000000042587", LocationDivision.DISTRICT_IN_REGION.getLevelId());
        UriComponentsBuilder level2BuilderLvivParent =
            buildParent(LocationDivision.DISTRICT_IN_REGION.getLevelId(), "UA46000000000026241");
        UriComponentsBuilder level3VillageBuilder =
            buildCode("UA46060230000093092", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder level3CityBuilder =
            buildCode("UA05020030000031457", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder level3BuilderParentLviv =
            buildParent(LocationDivision.LOCAL_COMMUNITY.getLevelId(), "UA46060000000042587");
        UriComponentsBuilder level4BuilderParentLvivCity =
            buildParent(LocationDivision.CITY.getLevelId(), "UA05020030000031457");
        UriComponentsBuilder level4BuilderLvivCity = buildName("Львів", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder level4BuilderVillage = buildName("Віднів", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder level5BuilderVillage =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA46060230040034427");
        UriComponentsBuilder level5BuilderCity =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA46060250010015970");

        restTemplate = mock(RestTemplate.class);
        response(level1Builder, Arrays.asList(autonomousRepublicResult, lvivskaResult));
        response(level2BuilderLviv, Arrays.asList(lvivDistrictResult));
        response(level2BuilderLvivParent, Arrays.asList(lvivDistrictResult));
        response(level3VillageBuilder, Arrays.asList(kulykivskaResult));
        response(level3BuilderParentLviv, Arrays.asList(lvivska2Result));
        response(level3BuilderParentLviv, Arrays.asList(lvivska2Result));
        response(level3CityBuilder, Arrays.asList(lvivska2Result));
        response(level4BuilderVillage, Arrays.asList(kulykivResult, vidnivResult));
        response(level4BuilderParentLvivCity, Arrays.asList(kulykivResult, vidnivResult, lvivResult));
        response(level4BuilderLvivCity, Arrays.asList(lvivResult));
        response(level5BuilderVillage, new ArrayList<>());
        response(level5BuilderCity, Arrays.asList(halytskyiResult, zaliznychnyiResult));
        locationApiService = new LocationApiService(restTemplate);
    }

    ResponseEntity<Map> prepareResponseEntity(List<Map<String, Object>> results) {
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("results", results);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    void assertLocationDto(LocationDto locationDto, String expectedId, String expectedParentId,
        String expectedName, String expectedNameEn) {
        assertEquals(expectedId, locationDto.getId());
        assertEquals(expectedParentId, locationDto.getParentId());
        assertEquals(expectedName, locationDto.getName().get(NAME));
        assertEquals(expectedNameEn, locationDto.getName().get(NAME_EN));
    }

    @Test
    void testGetDisctrictByNameEn() {
        initialise();

        UriComponentsBuilder lviv = UriComponentsBuilder
            .fromHttpUrl(API_URL)

            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Lviv")
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId());
        UriComponentsBuilder lvivska = UriComponentsBuilder
            .fromHttpUrl(API_URL)

            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Lvivska")
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());
        when(restTemplate.exchange(eq(lviv.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(new ArrayList<>()));
        when(restTemplate.exchange(eq(lvivska.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(
                    prepareResponseEntity(new ArrayList<>()));

        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Lvivska", "Lviv");

        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");
    }

    @Test
    void testGetAllDistrictsInCityByNames_Kyiv() {

        initKyiv();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Київ");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
            "Holosiivskyi");
        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    }

    public void initKyiv() {
        restTemplate = mock(RestTemplate.class);
        locationApiService = new LocationApiService(restTemplate);

        UriComponentsBuilder builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder builder5 =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA80000000000093317");
        response(builder, Arrays.asList(kyivResult));
        response(builder5, Arrays.asList(holosiivskyiResult, darnytskyiResult));

    }

    @Test
    void testGetAllDistrictsInCityByNames_KyivEn() {
        initKyiv();

        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Kyiv");

        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
            "Holosiivskyi");
        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_Error() {
        initialise();
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Вінницька", "Львів");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames() {
        initialise();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Львів");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_CityNotFound() {
        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No City", "No City En");
        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((API_URL), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames_LocalCommunityNotFound() {
        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No Community", "No Community En");
        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((API_URL), Map.class)).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
        });

    }

    @Test
    void testGetAllDistrictsInCityByNames_NoDistricts() {
        initialise();
        List<LocationDto> allDistricts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Віднів");
        assertNotNull(allDistricts);
        assertFalse(allDistricts.isEmpty());
        assertEquals(1, allDistricts.size());
        assertLocationDto(allDistricts.get(0), "UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");
    }

    @Test
    void getRegionByName_whenAllCitiesIsEmpty() {
        initialise();
        List<LocationDto> result = locationApiService.getAllDistrictsInCityByNames("Lvivska", "Львів");
        assertEquals(2, result.size());
    }

}
