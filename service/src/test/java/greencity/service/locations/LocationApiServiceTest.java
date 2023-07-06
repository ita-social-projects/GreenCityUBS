package greencity.service.locations;

import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class LocationApiServiceTest {
    private static final String API_URL = "https://directory.org.ua/api/katottg";
    private static final String PAGE_SIZE_VALUE = "125";
    private static final String LEVEL = "level";
    private static final String NAME = "name";
    private static final String NAME_EN = "name_en";
    private static final String CODE = "code";
    private static final String PAGE_SIZE = "page_size";
    private static final String PARENT = "parent";
    private static final String PARENT_ID = "parent_id";
    private static final String RESULTS = "results";
    @InjectMocks
    LocationApiService locationApiService;
    @Mock
    RestTemplate restTemplate;

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

    private void respond(UriComponentsBuilder builder, List<Map<String, Object>> list) {
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(list));
    }

    ResponseEntity<Map> prepareResponseEntity(List<Map<String, Object>> results) {
        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put(RESULTS, results);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private void assertLocationDto(LocationDto locationDto, String expectedId, String expectedParentId,
        String expectedName, String expectedNameEn) {
        assertEquals(expectedId, locationDto.getId());
        assertEquals(expectedParentId, locationDto.getParentId());
        assertEquals(expectedName, locationDto.getLocationNameMap().get(NAME));
        assertEquals(expectedNameEn, locationDto.getLocationNameMap().get(NAME_EN));
    }

    private void initLviv() {
        Map<String, Object> lvivskaResult =
            getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> lviv2Result =
            getApiResult("UA05020030010063857", "UA05020000000026686", "Львів", "Lviv");
        Map<String, Object> lvivDistrictResult =
            getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
        Map<String, Object> lvivska2Result =
            getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");
        Map<String, Object> lvivResult =
            getApiResult("UA46060250010015970", "UA05020030000031457", "Львів", "Lviv");
        Map<String, Object> halytskyiResult =
            getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        Map<String, Object> zaliznychnyiResult =
            getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");
        UriComponentsBuilder level1Builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder level2BuilderLviv =
            buildCode("UA46060000000042587", LocationDivision.DISTRICT_IN_REGION.getLevelId());
        UriComponentsBuilder level2BuilderLvivParent =
            buildParent(LocationDivision.DISTRICT_IN_REGION.getLevelId(), "UA46000000000026241");
        UriComponentsBuilder level3CityBuilder =
            buildCode("UA05020030000031457", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder level3BuilderParentLviv =
            buildParent(LocationDivision.LOCAL_COMMUNITY.getLevelId(), "UA46060000000042587");
        UriComponentsBuilder level4BuilderParentLvivCity =
            buildParent(LocationDivision.CITY.getLevelId(), "UA05020030000031457");
        UriComponentsBuilder level4BuilderLvivCity = buildName("Львів", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder level4BuilderLviv2City = buildName("Миколаїв", LocationDivision.CITY.getLevelId());

        UriComponentsBuilder level5BuilderVillage =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA46060230040034427");
        UriComponentsBuilder level5BuilderCity =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA46060250010015970");

        respond(level1Builder, Arrays.asList(lvivskaResult));
        respond(level2BuilderLviv, Arrays.asList(lvivDistrictResult));
        respond(level2BuilderLvivParent, Arrays.asList(lvivDistrictResult));
        respond(level3BuilderParentLviv, Arrays.asList(lvivska2Result));
        respond(level3BuilderParentLviv, Arrays.asList(lvivska2Result));
        respond(level3CityBuilder, Arrays.asList(lvivska2Result));
        respond(level4BuilderParentLvivCity, Arrays.asList(lvivResult, lviv2Result));
        respond(level4BuilderLvivCity, Arrays.asList(lvivResult));
        respond(level4BuilderLviv2City, Arrays.asList(lviv2Result));
        respond(level5BuilderVillage, new ArrayList<>());
        respond(level5BuilderCity, Arrays.asList(halytskyiResult, zaliznychnyiResult));

    }

    private void initKyiv() {
        Map<String, Object> kyivResult = getApiResult("UA80000000000093317", null, "Київ", "Kyiv");
        Map<String, Object> holosiivskyiResult =
            getApiResult("UA80000000000126643", "UA80000000000093317", "Голосіївський", "Holosiivskyi");
        Map<String, Object> darnytskyiResult =
            getApiResult("UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

        UriComponentsBuilder kyivBuilder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder kyivDistrictsBuilder =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA80000000000093317");
        respond(kyivBuilder, Arrays.asList(kyivResult));
        respond(kyivDistrictsBuilder, Arrays.asList(holosiivskyiResult, darnytskyiResult));

    }

    @Test
    void testGetDisctrictByName_whenNameEn() {
        initLviv();
        UriComponentsBuilder lvivBuilder = buildName("Lviv", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder lvivskaBuilder = buildName("Lvivska", LocationDivision.REGION.getLevelId());
        respond(lvivBuilder, new ArrayList<>());
        respond(lvivskaBuilder, new ArrayList<>());
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Lvivska", "Lviv");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");
    }

    @Test
    void testGetAllDistrictsInCityByNames_whenKyiv() {
        initKyiv();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Київ");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
            "Holosiivskyi");
        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_whenKyivEn() {
        initKyiv();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Kyiv");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
            "Holosiivskyi");
        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_whenCityNotExist() {
        initLviv();
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Вінницька", "Львів");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames() {
        initLviv();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Львів");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_whenCityNotFound() {
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Львівська", "UNREAL");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames_whenLocalCommunityNotFound() {
        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No Community", "No Community En");
        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        when(restTemplate.getForEntity((API_URL), Map.class)).thenReturn(responseEntity);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
        });

    }

    @Test
    void testGetAllDistrictsInCityByNames_whenNoDistricts() {

        Map<String, Object> lvivskaResult =
            getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> lvivDistrictResult =
            getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
        Map<String, Object> kulykivskaResult =
            getApiResult("UA46060230000093092", "UA46060000000042587", "Куликівська", "Kulykivska");
        Map<String, Object> kulykivResult =
            getApiResult("UA46060230010099970", "UA46060230000093092", "Куликів", "Kulykiv");
        Map<String, Object> vidnivResult =
            getApiResult("UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");
        UriComponentsBuilder level1Builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder level2BuilderLviv =
            buildCode("UA46060000000042587", LocationDivision.DISTRICT_IN_REGION.getLevelId());
        UriComponentsBuilder level2BuilderLvivParent =
            buildParent(LocationDivision.DISTRICT_IN_REGION.getLevelId(), "UA46000000000026241");
        UriComponentsBuilder level3VillageBuilder =
            buildCode("UA46060230000093092", LocationDivision.LOCAL_COMMUNITY.getLevelId());

        UriComponentsBuilder level4BuilderParentLvivCity =
            buildParent(LocationDivision.CITY.getLevelId(), "UA05020030000031457");
        UriComponentsBuilder level4BuilderVillage = buildName("Віднів", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder level5BuilderVillage =
            buildParent(LocationDivision.DISTRICT_IN_CITY.getLevelId(), "UA46060230040034427");

        respond(level1Builder, Arrays.asList(lvivskaResult));
        respond(level2BuilderLviv, Arrays.asList(lvivDistrictResult));
        respond(level2BuilderLvivParent, Arrays.asList(lvivDistrictResult));
        respond(level3VillageBuilder, Arrays.asList(kulykivskaResult));
        respond(level4BuilderVillage, Arrays.asList(kulykivResult, vidnivResult));
        respond(level4BuilderParentLvivCity, Arrays.asList(kulykivResult, vidnivResult));
        respond(level5BuilderVillage, new ArrayList<>());
        List<LocationDto> allDistricts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Віднів");
        assertNotNull(allDistricts);
        assertFalse(allDistricts.isEmpty());
        assertEquals(1, allDistricts.size());
        assertLocationDto(allDistricts.get(0), "UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");
    }

    @Test
    void testGetAllDistrictsInCityByNames_whenNameEn() {
        initLviv();
        List<LocationDto> result = locationApiService.getAllDistrictsInCityByNames("Lvivska", "Львів");
        assertEquals(2, result.size());
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_whenCityValueNulOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Lvivska", null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Lvivska", "");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_whenRegionValueNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames(null, "Lviv");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("", "Lviv");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_whenValuesNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames(null, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("", "");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_whenResultEmpty() {
        initLviv();
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Львівська", "Тест");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_whenUrlEmpty() {
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Тест", "Тест");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_noCities() {
        Map<String, Object> mykolaivskaResult =
            getApiResult("UA48000000000039575", null, "Миколаївська", "Mykolaivska");
        Map<String, Object> chernihivskaResult =
            getApiResult("UA74000000000025378", null, "Чернігівська", "Chernihivska");
        Map<String, Object> nizhynskyiResult =
            getApiResult("UA74040000000028062", "UA74000000000025378", "Ніжинський", "Nizhynskyi");
        Map<String, Object> bobrovytskaDistrictResult =
            getApiResult("UA74040050000013413", "UA74040000000028062", "Бобровицька", "Bobrovytska");
        Map<String, Object> mykolaiv2Result =
            getApiResult("UA74040050200013786", "UA74040050000013413", "Миколаїв", "Mykolaiv");

        UriComponentsBuilder builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder builder4 = buildName("Миколаїв", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder builder3 = buildCode("UA74040050000013413", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder builder2 =
            buildCode("UA74040000000028062", LocationDivision.DISTRICT_IN_REGION.getLevelId());

        respond(builder, Arrays.asList(mykolaivskaResult, chernihivskaResult));
        respond(builder4, Arrays.asList(mykolaiv2Result));
        respond(builder3, Arrays.asList(bobrovytskaDistrictResult));
        respond(builder2, Arrays.asList(nizhynskyiResult));

        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Миколаївська", "Миколаїв");
        });
    }

    @Test
    void testGetGetAllDistrictsInCityByNames_noCitiesEmptyList() {
        Map<String, Object> mykolaivskaResult =
            getApiResult("UA48000000000039575", null, "Миколаївська", "Mykolaivska");
        Map<String, Object> mykolaivskyiResult =
            getApiResult("UA48060000000094390", "UA48000000000039575", "Миколаївський", "Mykolaivskyi");
        Map<String, Object> mykolaivskaDistrictResult =
            getApiResult("UA48060150000071713", "UA48060000000094390", "Миколаївська", "Mykolaivska");
        Map<String, Object> chernihivskaResult =
            getApiResult("UA74000000000025378", null, "Чернігівська", "Chernihivska");
        Map<String, Object> nizhynskyiResult =
            getApiResult("UA74040000000028062", "UA74000000000025378", "Ніжинський", "Nizhynskyi");
        Map<String, Object> bobrovytskaDistrictResult =
            getApiResult("UA74040050000013413", "UA74040000000028062", "Бобровицька", "Bobrovytska");
        UriComponentsBuilder builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder builder4 = buildName("Миколаїв", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder builder3 = buildCode("UA74040050000013413", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder builder2 =
            buildCode("UA74040000000028062", LocationDivision.DISTRICT_IN_REGION.getLevelId());
        UriComponentsBuilder builder2_2 =
            buildParent(LocationDivision.DISTRICT_IN_REGION.getLevelId(), "UA48000000000039575");
        UriComponentsBuilder builder3_2 =
            buildParent(LocationDivision.LOCAL_COMMUNITY.getLevelId(), "UA48060000000094390");
        UriComponentsBuilder builder4_2 = buildParent(LocationDivision.CITY.getLevelId(), "UA48060150000071713");

        respond(builder, Arrays.asList(mykolaivskaResult, chernihivskaResult));
        respond(builder4, new ArrayList<>());
        respond(builder3, Arrays.asList(bobrovytskaDistrictResult));
        respond(builder2, Arrays.asList(nizhynskyiResult));
        respond(builder2_2, Arrays.asList(mykolaivskyiResult));
        respond(builder3_2, Arrays.asList(mykolaivskaDistrictResult));
        respond(builder4_2, new ArrayList<>());

        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Миколаївська", "Миколаїв");
        });
    }

    @Test
    void testGetCityByNameFromRegionSide_ResultFromUrlEmpty() {
        Map<String, Object> mykolaivskaResult =
            getApiResult("UA48000000000039575", null, "Миколаївська", "Mykolaivska");
        Map<String, Object> mykolaivskyiResult =
            getApiResult("UA48060000000094390", "UA48000000000039575", "Миколаївський", "Mykolaivskyi");
        Map<String, Object> mykolaivskaDistrictResult =
            getApiResult("UA48060150000071713", "UA48060000000094390", "Миколаївська", "Mykolaivska");
        Map<String, Object> chernihivskaResult =
            getApiResult("UA74000000000025378", null, "Чернігівська", "Chernihivska");
        Map<String, Object> nizhynskyiResult =
            getApiResult("UA74040000000028062", "UA74000000000025378", "Ніжинський", "Nizhynskyi");
        Map<String, Object> bobrovytskaDistrictResult =
            getApiResult("UA74040050000013413", "UA74040000000028062", "Бобровицька", "Bobrovytska");
        Map<String, Object> mykolaiv2Result =
            getApiResult("UA74040050200013786", "UA74040050000013413", "Миколаїв", "Mykolaiv");

        UriComponentsBuilder builder = build(LocationDivision.REGION.getLevelId());
        UriComponentsBuilder builder4 = buildName("Миколаїв", LocationDivision.CITY.getLevelId());
        UriComponentsBuilder builder3 = buildCode("UA74040050000013413", LocationDivision.LOCAL_COMMUNITY.getLevelId());
        UriComponentsBuilder builder2 =
            buildCode("UA74040000000028062", LocationDivision.DISTRICT_IN_REGION.getLevelId());
        UriComponentsBuilder builder2_2 =
            buildParent(LocationDivision.DISTRICT_IN_REGION.getLevelId(), "UA48000000000039575");
        UriComponentsBuilder builder3_2 =
            buildParent(LocationDivision.LOCAL_COMMUNITY.getLevelId(), "UA48060000000094390");
        UriComponentsBuilder builder4_2 = buildParent(LocationDivision.CITY.getLevelId(), "UA48060150000071713");

        respond(builder, Arrays.asList(mykolaivskaResult, chernihivskaResult));
        respond(builder4, new ArrayList<>());
        respond(builder3, Arrays.asList(bobrovytskaDistrictResult));
        respond(builder2, new ArrayList<>());
        respond(builder2_2, Arrays.asList(mykolaivskyiResult));
        respond(builder3_2, Arrays.asList(mykolaivskaDistrictResult));
        respond(builder4_2, Arrays.asList(mykolaiv2Result));

        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("Миколаївська", "Миколаїв");
        });
    }

}
