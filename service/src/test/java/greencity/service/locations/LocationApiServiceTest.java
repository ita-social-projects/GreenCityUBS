package greencity.service.locations;

import com.azure.core.http.rest.Page;
import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.enums.LocationDivision;
import greencity.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocationApiServiceTest {
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

    Map<String, Object> getApiResult(String code, String parent_id, String name, String nameEn) {
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

    void initialise() {
        Map<String, Object> autonomousRepublicResult =
            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
        Map<String, Object> vinnytskaResult = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> lvivskaResult = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> dniptopetrovskaResult =
            getApiResult("UA12000000000090473", null, "Дніпропетровська", "Dnipropetrovska");
        Map<String, Object> lvivDistrictResult =
            getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
        Map<String, Object> kulykivskaResult =
            getApiResult("UA46060230000093092", "UA46060000000042587", "Куликівська", "Kulykivska");
        Map<String, Object> kulykivResult =
            getApiResult("UA46060230010099970", "UA46060230000093092", "Куликів", "Kulykiv");
        Map<String, Object> vidnivResult =
            getApiResult("UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");

        UriComponentsBuilder level1Builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder level2BuilderLviv = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA46060000000042587")
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId());

        UriComponentsBuilder level2BuilderLvivParent = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId())
            .queryParam(PARENT, "UA46000000000026241");
        UriComponentsBuilder level3VillageBuilder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA46060230000093092")
            .queryParam(LEVEL, LocationDivision.LOCAL_COMMUNITY.getLevelId());

        UriComponentsBuilder level3CityBuilder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05020030000031457")
            .queryParam(LEVEL, LocationDivision.LOCAL_COMMUNITY.getLevelId());

        UriComponentsBuilder level3BuilderParentLviv = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.LOCAL_COMMUNITY.getLevelId())
            .queryParam(PARENT, "UA46060000000042587");

        UriComponentsBuilder level4BuilderParentLvivCity = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId())
            .queryParam(PARENT, "UA05020030000031457");

        UriComponentsBuilder level4BuilderLvivCity = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Львів")
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId());

        UriComponentsBuilder level4BuilderVillage = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Віднів")
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId());

        UriComponentsBuilder level5BuilderVillage = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_CITY.getLevelId())
            .queryParam(PARENT, "UA46060230040034427");

        UriComponentsBuilder level5BuilderCity =
            UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam(PAGE, PAGE_VALUE)
                .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
                .queryParam(LEVEL, LocationDivision.DISTRICT_IN_CITY.getLevelId())
                .queryParam(PARENT, "UA46060250010015970");

        UriComponentsBuilder regionsBuilder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        Map<String, Object> lvivska2Result =
            getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");

        Map<String, Object> lvivResult = getApiResult("UA46060250010015970", "UA05020030000031457", "Львів", "Lviv");

        Map<String, Object> halytskyiResult =
            getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");

        Map<String, Object> zaliznychnyiResult =
            getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");

        restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(eq(level1Builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(
                    prepareResponseEntity(Arrays.asList(autonomousRepublicResult, vinnytskaResult, lvivskaResult,
                        dniptopetrovskaResult)));

        when(restTemplate.exchange(eq(level2BuilderLviv.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(lvivDistrictResult)));

        when(restTemplate.exchange(eq(level2BuilderLvivParent.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(lvivDistrictResult)));
        when(restTemplate.exchange(eq(level3VillageBuilder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(kulykivskaResult)));
        when(restTemplate.exchange(eq(level3BuilderParentLviv.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(lvivska2Result)));
        when(restTemplate.exchange(eq(level4BuilderVillage.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(kulykivResult, vidnivResult)));
        when(restTemplate.exchange(eq(level4BuilderParentLvivCity.build().encode().toUri()), eq(HttpMethod.GET),
            eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(kulykivResult, vidnivResult, lvivResult)));
        when(restTemplate.exchange(eq(level3CityBuilder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(lvivska2Result)));
        when(restTemplate.exchange(eq(level4BuilderLvivCity.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(lvivResult)));
        when(restTemplate.exchange(eq(level5BuilderVillage.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(new ArrayList<>()));
        when(restTemplate.exchange(eq(level5BuilderCity.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(halytskyiResult, zaliznychnyiResult)));
        when(restTemplate.exchange(eq(regionsBuilder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(
                    prepareResponseEntity(Arrays.asList(autonomousRepublicResult, lvivskaResult, vinnytskaResult)));

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
    void testGetAllDistrictsInCityByCityID() {
        initialise();
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByCityID("UA46060250010015970");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");
    }

    @Test
    void testGetRegionByName() {
        initialise();
        LocationDto region = locationApiService.getRegionByName("Вінницька");
        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }

    @Test
    void testGetRegionByName_ListEmpty() {
        initialise();
        LocationDto region = locationApiService.getRegionByName("Avtonomna Respublika Krym");
        assertLocationDto(region, "UA01000000000013043", null, "Автономна Республіка Крим",
            "Avtonomna Respublika Krym");
    }

    @Test
    void testGetRegionByNameEn() {
        initialise();
        LocationDto region = locationApiService.getRegionByName("Vinnytska");
        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }

    @Test
    void testGetLocationDataByCode() {
        restTemplate = mock(RestTemplate.class);

        Map<String, Object> lvivskaResult = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");

        UriComponentsBuilder lvivska = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05000000000010236")
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        when(restTemplate.exchange(eq(lvivska.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(
                    prepareResponseEntity(Arrays.asList(lvivskaResult, lvivskaResult)));
        locationApiService = new LocationApiService(restTemplate);

        assertThrows(NotFoundException.class, () -> {
            locationApiService.getLocationDataByCode(LocationDivision.REGION.getLevelId(), "UA05000000000010236");
        });
    }

    @Test
    void testGetDisctrictByNameEn() {
        initialise();

        UriComponentsBuilder lviv = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Lviv")
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId());
        UriComponentsBuilder lvivska = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
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
    void testGetCitiesByName() {
        initialise();
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getCitiesByName("Львівська", "Нема");
        });
    }

    @Test
    void testGetUnrealCityInRegion() {
        List<LocationDto> emptylist = new ArrayList<>();
        assertThrows(RuntimeException.class, () -> {
            locationApiService.getCityInRegion("Львівська", emptylist);
        });
    }

    @Test
    void testGetCityInRegion() {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, 1)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05020000000026686")
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId());

        UriComponentsBuilder builder3 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, 1)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05020030000031457")
            .queryParam(LEVEL, 3);

        UriComponentsBuilder builder4 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(PARENT, "UA05020030000031457")
            .queryParam(LEVEL, 4);
        UriComponentsBuilder builder_regions = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> apiResult21 =
            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        Map<String, Object> apiResult22 =
            getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
        Map<String, Object> apiResult31 =
            getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");

        Map<String, Object> apiResult32 =
            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
        Map<String, Object> apiResult41 =
            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
        Map<String, Object> apiResult42 =
            getApiResult("UA05020030010063857", "UA05020030000031457", "Львів", "Lviv");

        LocationDto city1 = LocationDto.builder().id("UA05020030010063857").parentId("UA05020030000031457")
            .name(Collections.singletonMap(NAME, "Львів")).build();
        LocationDto city2 = LocationDto.builder().id("UA05020030020063505").parentId("UA05020030000031457")
            .name(Collections.singletonMap(NAME, "Львів")).build();
        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        when(restTemplate.exchange(eq(builder_regions.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult22)));
        when(restTemplate.exchange(eq(builder3.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
        when(restTemplate.exchange(eq(builder4.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));

        assertLocationDto(locationApiService.getCityInRegion("Львівська", Arrays.asList(city1, city2)),
            "UA05020030010063857", "UA05020030000031457", "Львів", null);
    }

    @Test
    void testgetResultFromUrl_UnrealUrl() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        URI testUrl = URI.create("http://testurl.com");

        LocationApiService locationApiService = new LocationApiService(restTemplate);

        ParameterizedTypeReference<Map<String, Object>> typeRef =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };

        when(restTemplate.exchange(
            (testUrl),
            (HttpMethod.GET),
            (null),
            (typeRef))).thenThrow(new RestClientException("Test exception"));

        assertThrows(
            NotFoundException.class,
            () -> locationApiService.getResultFromUrl(testUrl),
            ErrorMessage.NOT_FOUND_LOCATION_BY_URL + testUrl);
    }

    @Test
    void testGetCityByNameAndRegionName() {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, 1)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05020000000026686")
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId());

        UriComponentsBuilder builder3 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, 1)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(CODE, "UA05020030000031457")
            .queryParam(LEVEL, 3);

        UriComponentsBuilder builder4 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(PARENT, "UA05020030000031457")
            .queryParam(LEVEL, 4);
        UriComponentsBuilder builder_regions = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        Map<String, Object> apiResult1 =
            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> apiResult21 =
            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        Map<String, Object> apiResult32 =
            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
        Map<String, Object> apiResult41 =
            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
        LocationDto city1 = LocationDto.builder().id("UA05020030010063857").parentId("UA05020030000031457")
            .name(Collections.singletonMap(NAME, "Вінниця")).build();
        LocationDto city2 = LocationDto.builder().id("UA05020030020063505").parentId("UA05020030000031457")
            .name(Collections.singletonMap(NAME, "Десна")).build();
        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        when(restTemplate.exchange(eq(builder_regions.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
        when(restTemplate.exchange(eq(builder3.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
        when(restTemplate.exchange(eq(builder4.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));

        LocationDto result = locationApiService.getCityInRegion("Вінницька", Arrays.asList(city1, city2));
        assertNotNull(result);
        assertEquals("UA05020030010063857", result.getId());
        assertEquals("Вінниця", result.getName().get(NAME));
    }

    @Test
    void testNullParentParameter() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getAllDistrictsInCityByCityID(null);
        });
    }

    @Test
    void testEmptyParentParameter() {
        initialise();
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByCityID("");
        });
    }

    @Test
    void testNullNameParameterInGetRegionByName() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(IllegalArgumentException.class, () -> {
            locationApiService.getRegionByName(null);
        });
    }

    @Test
    void testEmptyNameParameterInGetRegionByName() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getRegionByName("");
        });
    }

    @Test
    void testNonExistentParentParameter() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByCityID("NonExistentParent");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "NonExistentName",
        "APIErrors404",
        "APIErrors500"
    })
    void testNonExistentNameParameterInGetRegionByName(String testName) {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), Map.class)).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getRegionByName(testName);
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames_CityWithoutDistricts() {
        Map<String, Object> apiResult1 =
            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> apiResult21 =
            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        Map<String, Object> apiResult32 =
            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
        Map<String, Object> apiResult41 =
            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");

        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);

        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&level=1", Map.class))
            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));

        when(restTemplate.getForEntity(
            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2", Map.class))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));

        when(restTemplate.getForEntity(
            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020000000026686&level=3", Map.class))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));

        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Вінниця&level=4",
            Map.class)).thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));

        assertThrows(NotFoundException.class, () -> {
            List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Вінницька", "Вінниця");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames_Kyiv() {
        Map<String, Object> apiResult1 = getApiResult("UA80000000000093317", null, "Київ", "Kyiv");
        Map<String, Object> apiResult51 =
            getApiResult("UA80000000000126643", "UA80000000000093317", "Голосіївський", "Holosiivskyi");
        Map<String, Object> apiResult52 =
            getApiResult("UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder builder5 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_CITY.getLevelId())
            .queryParam(PARENT, "UA80000000000093317");
        when(restTemplate.getForEntity((builder.build().encode().toUri()), Map.class))
            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1)));
        when(restTemplate.getForEntity((builder5.build().encode().toUri()), Map.class))
            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));

        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1)));
        when(restTemplate.exchange(eq(builder5.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));

        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Київ");

        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
            "Holosiivskyi");
        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

    }

    @Test
    void testGetAllDistrictsInCityByNames_KyivEn() {
        Map<String, Object> apiResult1 = getApiResult("UA80000000000093317", null, "Київ", "Kyiv");
        Map<String, Object> apiResult51 =
            getApiResult("UA80000000000126643", "UA80000000000093317", "Голосіївський", "Holosiivskyi");
        Map<String, Object> apiResult52 =
            getApiResult("UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");

        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder builder5 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_CITY.getLevelId())
            .queryParam(PARENT, "UA80000000000093317");
        when(restTemplate.getForEntity((builder.build().encode().toUri()), Map.class))
            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1)));
        when(restTemplate.getForEntity((builder5.build().encode().toUri()), Map.class))
            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));

        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1)));
        when(restTemplate.exchange(eq(builder5.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));

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
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=4";
        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No City", "No City En");
        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), (Map.class))).thenReturn(responseEntity);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
        });
    }

    @Test
    void testGetAllDistrictsInCityByNames_LocalCommunityNotFound() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=3";
        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No Community", "No Community En");
        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), Map.class)).thenReturn(responseEntity);
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
    void testGetRegion() {
        initialise();
        List<LocationDto> regions = locationApiService.getAllRegions();
        assertEquals(3, regions.size());
        assertLocationDto(regions.get(0), "UA01000000000013043", null, "Автономна Республіка Крим",
            "Avtonomna Respublika Krym");
        assertLocationDto(regions.get(2), "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }

    @Test
    void testGetUnrealRegion() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=1";
        LocationDto city1 = LocationDto.builder()
            .id("UA05020030010063857")
            .parentId(null)
            .name(Collections.singletonMap(NAME, "НЕМАЄ"))
            .build();
        Map<String, Object> apiResult1 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        List<Map<String, Object>> results = Arrays.asList(apiResult1);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.getForEntity((apiUrl), Map.class)).thenReturn(responseEntity);

        LocationApiService locationApiService = new LocationApiService(restTemplate);

        assertThrows(NotFoundException.class, () -> {
            locationApiService.getRegionByName("НЕМАЄ");
        });
    }

    @Test
    void testGetCityByName() {
        initialise();
        LocationDto result = locationApiService.getCitiesByName("Львівська", "Львів").get(0);
        assertNotNull(result);
        assertEquals("UA46060250010015970", result.getId());
        assertEquals("Львів", result.getName().get(NAME));
    }

    @Test
    void getRegionByName_whenAllCitiesIsEmpty() {
        initialise();
        List<LocationDto> result = locationApiService.getAllDistrictsInCityByNames("Lvivska", "Львів");
        assertEquals(2, result.size());
    }

    @Test
    void testGetCityByNameFromRegionSide() {
        Map<String, Object> apiResult1 =
            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
        Map<String, Object> apiResult21 =
            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        Map<String, Object> apiResult32 =
            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
        Map<String, Object> apiResult41 =
            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(NAME, "Вінницька")
            .queryParam(LEVEL, LocationDivision.REGION.getLevelId());

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId())
            .queryParam(PARENT, "UA05000000000010236");

        UriComponentsBuilder builder3 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.LOCAL_COMMUNITY.getLevelId())
            .queryParam(PARENT, "UA05020000000026686");

        UriComponentsBuilder builder4 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId())
            .queryParam(PARENT, "UA05020030000031457");

        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));

        when(restTemplate.exchange(eq(builder3.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
        when(restTemplate.exchange(eq(builder4.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));

        LocationDto result = locationApiService.getCityByNameFromRegionSide("Вінницька", "Вінниця");
        assertNotNull(result);
        assertEquals("UA05020030010063857", result.getId());
        assertEquals("Вінниця", result.getName().get(NAME));
    }

    @Test
    void testGetCityByNameAndRegionName_NotFoundException() {
        LocationDto city1 = LocationDto.builder()
            .id("UA05020030010063857")
            .parentId("UA05020030000031457")
            .name(Collections.singletonMap(NAME, "Вінниця"))
            .build();

        RestTemplate restTemplate = mock(RestTemplate.class);

        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Неіснуюча&level=1",
            Map.class)).thenReturn(null);

        LocationApiService locationApiService = new LocationApiService(restTemplate);

        String cityName = "Неіснуюча";
        List<LocationDto> cityList = Arrays.asList(city1);

        Exception exception =
            assertThrows(NotFoundException.class, () -> locationApiService.getCityInRegion(cityName, cityList));
    }

    @Test
    void testGetDistrictInTheRegion() {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.DISTRICT_IN_REGION.getLevelId())
            .queryParam(PARENT, "UA05000000000010236");

        Map<String, Object> apiResult1 =
            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        Map<String, Object> apiResult2 =
            getApiResult("UA05040000000050292", "UA05000000000010236", "Гайсинський", "Haisynskyi");
        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2)));

        LocationApiService locationApiService = new LocationApiService(restTemplate);
        List<LocationDto> regions = locationApiService.getAllDistrictInTheRegionsById("UA05000000000010236");
        assertEquals(2, regions.size());
        assertLocationDto(regions.get(0), "UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
        assertLocationDto(regions.get(1), "UA05040000000050292", "UA05000000000010236", "Гайсинський", "Haisynskyi");

    }

    @Test
    void testGetLocalCommunity() {
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam(PAGE, PAGE_VALUE)
                .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
                .queryParam(LEVEL, LocationDivision.LOCAL_COMMUNITY.getLevelId())
                .queryParam(PARENT, "UA05020000000026686");
        URI apiUrl = builder.build().encode().toUri();

        Map<String, Object> apiResult1 =
            getApiResult("UA05020010000053508", "UA05020000000026686", "Агрономічна", "Ahronomichna");
        Map<String, Object> apiResult2 =
            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 =
            getApiResult("UA05020050000066991", "UA05020000000026686", "Вороновицька", "Voronovytska");
        Map<String, Object> apiResult4 =
            getApiResult("UA05020070000010139", "UA05020000000026686", "Гніванська", "Hnivanska");

        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2, apiResult3, apiResult4);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("results", results);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(responseEntity);

        LocationApiService locationApiService = new LocationApiService(restTemplate);

        List<LocationDto> localCommunities = locationApiService.getAllLocalCommunitiesById("UA05020000000026686");

        assertEquals(4, localCommunities.size());
        assertLocationDto(localCommunities.get(0), "UA05020010000053508", "UA05020000000026686", "Агрономічна",
            "Ahronomichna");
        assertLocationDto(localCommunities.get(1), "UA05020030000031457", "UA05020000000026686", "Вінницька",
            "Vinnytska");
        assertLocationDto(localCommunities.get(2), "UA05020050000066991", "UA05020000000026686", "Вороновицька",
            "Voronovytska");
        assertLocationDto(localCommunities.get(3), "UA05020070000010139", "UA05020000000026686", "Гніванська",
            "Hnivanska");
    }

    @Test
    void testGetCity() {
        Map<String, Object> apiResult1 = getApiResult("UA46060250010015970", "UA46060250000025047", "Львів", "Lviv");
        Map<String, Object> apiResult2 =
            getApiResult("UA46060250020038547", "UA46060250000025047", "Винники", "Vynnyky");
        Map<String, Object> apiResult3 =
            getApiResult("UA46060250030012851", "UA46060250000025047", "Дубляни", "Dubliany");
        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2, apiResult3);
        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
        RestTemplate restTemplate = mock(RestTemplate.class);
        LocationApiService locationApiService = new LocationApiService(restTemplate);
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam(PAGE, PAGE_VALUE)
            .queryParam(PAGE_SIZE, PAGE_SIZE_VALUE)
            .queryParam(LEVEL, LocationDivision.CITY.getLevelId())
            .queryParam(PARENT, "UA46060250000025047");
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
        List<LocationDto> cities = locationApiService.getAllCitiesById("UA46060250000025047");
        assertEquals(3, cities.size());
        assertLocationDto(cities.get(0), "UA46060250010015970", "UA46060250000025047", "Львів", "Lviv");
        assertLocationDto(cities.get(1), "UA46060250020038547", "UA46060250000025047", "Винники", "Vynnyky");
        assertLocationDto(cities.get(2), "UA46060250030012851", "UA46060250000025047", "Дубляни", "Dubliany");
    }

    @Test
    void testFindLocationByName_WhenLocationsEmpty_ThenCallGetCityByNameFromRegionSide() {
        initialise();
        LocationDto result =
            locationApiService.findLocationByName(new ArrayList<>(), "Львівська", "Львів", "Location not found: ");
        assertNotNull(result);
        assertEquals("Львів", result.getName().get(NAME));
    }

}
