package greencity.service.locations;

import greencity.constant.ErrorMessage;
import greencity.dto.location.api.LocationDto;
import greencity.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
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

    Map<String, Object> getApiResult(String code, String parent_id, String name, String nameEn) {
        Map<String, Object> apiResult = new HashMap<>();
        apiResult.put("code", code);
        apiResult.put("parent_id", parent_id);
        apiResult.put("name", name);
        apiResult.put("name_en", nameEn);
        return apiResult;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach
    void initialise() {
        Map<String, Object> apiResult1 =
            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult22 =
//            getApiResult("UA46020000000075920", "UA46000000000026241", "Дрогобицький", "Drohobytskyi");
        Map<String, Object> apiResult21 =
                getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
        Map<String, Object> apiResult31 =
            getApiResult("UA46060230000093092", "UA46060000000042587", "Куликівська", "Kulykivska");
        Map<String, Object> apiResult41 =
            getApiResult("UA46060230010099970", "UA46060230000093092", "Куликів", "Kulykiv");
        Map<String, Object> apiResult42 =
            getApiResult("UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "1");

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "2")
            .queryParam("code", "UA46060000000042587")
            .queryParam("level", "2");
        UriComponentsBuilder builder2_2 = UriComponentsBuilder
                .fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "2")
                .queryParam("parent", "UA46000000000026241");

        UriComponentsBuilder builder3_village = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "2")
            .queryParam("code", "UA46060230000093092")
            .queryParam("level", "3");

        UriComponentsBuilder builder3_city = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "2")
            .queryParam("code", "UA05020030000031457")
            .queryParam("level", "3");

        UriComponentsBuilder builder3_3 = UriComponentsBuilder
                .fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "3")
                .queryParam("parent", "UA46060000000042587");

        UriComponentsBuilder builder4_2 = UriComponentsBuilder
                .fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "4")
                .queryParam("parent", "UA05020030000031457");

        UriComponentsBuilder builder4_city = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("name", "Львів")
            .queryParam("level", "4");

        UriComponentsBuilder builder4_village = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("name", "Віднів")
            .queryParam("level", "4");
//        UriComponentsBuilder builder4_villageEn = UriComponentsBuilder
//                .fromHttpUrl("https://directory.org.ua/api/katottg")
//                .queryParam("page", "1")
//                .queryParam("page_size", "5000")
//                .queryParam("name", "Vidniv")
//                .queryParam("level", "4");
        UriComponentsBuilder builder5_village = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "5")
            .queryParam("parent", "UA46060230040034427");

        UriComponentsBuilder builder5_city =
            UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "5")
                .queryParam("parent", "UA46060250010015970");
        UriComponentsBuilder builder_regions = UriComponentsBuilder
                .fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "1");

        Map<String, Object> apiResult32 =
            getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");
        Map<String, Object> apiResult43 = getApiResult("UA46060250010015970", "UA05020030000031457", "Львів", "Lviv");

        Map<String, Object> apiResult51 =
            getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        Map<String, Object> apiResult52 =
            getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");

        RestTemplate restTemplate = mock(RestTemplate.class);

        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
        when(restTemplate.exchange(eq(builder2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
        when(restTemplate.exchange(eq(builder2_2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
        when(restTemplate.exchange(eq(builder3_village.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult31)));
        when(restTemplate.exchange(eq(builder3_3.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
        when(restTemplate.exchange(eq(builder4_village.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
        when(restTemplate.exchange(eq(builder4_2.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42,apiResult43)));
//        when(restTemplate.exchange(eq(builder4_villageEn.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
//                any(ParameterizedTypeReference.class)))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
        when(restTemplate.exchange(eq(builder3_city.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
        when(restTemplate.exchange(eq(builder4_city.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult43)));
        when(restTemplate.exchange(eq(builder5_village.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(new ArrayList<>()));
        when(restTemplate.exchange(eq(builder5_city.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
            any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));
        when(restTemplate.exchange(eq(builder_regions.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));

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
        assertEquals(expectedName, locationDto.getName().get("name"));
        assertEquals(expectedNameEn, locationDto.getName().get("name_en"));
    }

    @Test
    void testGetAllDistrictsInCityByCityID() {
        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByCityID("UA46060250010015970");
        assertEquals(2, districts.size());
        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
            "Zaliznychnyi");
    }

    @Test
    void testGetRegionByName() {
        LocationDto region = locationApiService.getRegionByName("Вінницька");
        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }
    @Test
    void testGetRegionByName_ListEmpty() {
        LocationDto region = locationApiService.getRegionByName("Avtonomna Respublika Krym");
        assertLocationDto(region, "UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
    }
    @Test
    void testGetRegionByNameEn() {
        LocationDto region = locationApiService.getRegionByName("Vinnytska");
        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }
    @Test
    void testGetCitiesByName() {
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getCitiesByName("Львівська","Нема");
        });
    }
    @Test
    void testGetUnrealCityInRegion() {
        assertThrows(NotFoundException.class, () -> {
            locationApiService.getCityInRegion("Львівська",new ArrayList<>());
        });
    }
    @Test
    void testgetResultFromUrl_UnrealUrl() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        URI testUrl = URI.create("http://testurl.com");

        // Assuming the existence of a LocationApiService constructor that accepts a RestTemplate
        LocationApiService locationApiService = new LocationApiService(restTemplate);

        ParameterizedTypeReference<Map<String, Object>> typeRef =
                new ParameterizedTypeReference<Map<String, Object>>() {
                };

        when(restTemplate.exchange(
                eq(testUrl),
                eq(HttpMethod.GET),
                eq(null),
                eq(typeRef)
        )).thenThrow(new RestClientException("Test exception"));

        assertThrows(
                NotFoundException.class,
                () -> locationApiService.getResultFromUrl(testUrl),
                ErrorMessage.NOT_FOUND_LOCATION_BY_URL + testUrl
        );
    }
    @Test
    void testGetCityByNameAndRegionName() {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", 1)
            .queryParam("page_size", 5000)
            .queryParam("level", "1");

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "2")
            .queryParam("code", "UA05020000000026686")
            .queryParam("level", "2");

        UriComponentsBuilder builder3 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", 1)
            .queryParam("page_size", 2)
            .queryParam("code", "UA05020030000031457")
            .queryParam("level", 3);

        UriComponentsBuilder builder4 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page_size", 5000)
            .queryParam("parent", "UA05020030000031457")
            .queryParam("level", 4);
        UriComponentsBuilder builder_regions = UriComponentsBuilder
                .fromHttpUrl("https://directory.org.ua/api/katottg")
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "1");

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
            .name(Collections.singletonMap("name", "Вінниця")).build();
        LocationDto city2 = LocationDto.builder().id("UA05020030020063505").parentId("UA05020030000031457")
            .name(Collections.singletonMap("name", "Десна")).build();
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
        assertEquals("Вінниця", result.getName().get("name"));
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
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "1");

        UriComponentsBuilder builder5 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "5")
            .queryParam("parent", "UA80000000000093317");
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
    void testGetAllDistrictsInCityByNames() {

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

        List<LocationDto> allDistricts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Віднів");

        assertNotNull(allDistricts);
        assertFalse(allDistricts.isEmpty());
        assertEquals(1, allDistricts.size());
        assertLocationDto(allDistricts.get(0), "UA46060230040034427", "UA46060230000093092", "Віднів", "Vidniv");
    }

    @Test
    void testGetRegion() {
  List<LocationDto> regions = locationApiService.getAllRegions();
        assertEquals(3, regions.size());
        assertLocationDto(regions.get(0), "UA01000000000013043", null, "Автономна Республіка Крим",
            "Avtonomna Respublika Krym");
        assertLocationDto(regions.get(1), "UA05000000000010236", null, "Вінницька", "Vinnytska");
    }

    @Test
    void testGetUnrealRegion() {
        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=1";
        LocationDto city1 = LocationDto.builder()
            .id("UA05020030010063857")
            .parentId(null)
            .name(Collections.singletonMap("name", "НЕМАЄ"))
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
    void testGetCityByName() {LocationDto result = locationApiService.getCitiesByName("Львівська", "Львів").get(0);
        assertNotNull(result);
        assertEquals("UA46060250010015970", result.getId());
        assertEquals("Львів", result.getName().get("name"));
    }

    @Test
    void getRegionByName_whenAllCitiesIsEmpty() {
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
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("name", "Вінницька")
            .queryParam("level", "1");

        UriComponentsBuilder builder2 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "2")
            .queryParam("parent", "UA05000000000010236");

        UriComponentsBuilder builder3 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "3")
            .queryParam("parent", "UA05020000000026686");

        UriComponentsBuilder builder4 = UriComponentsBuilder
            .fromHttpUrl("https://directory.org.ua/api/katottg")
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "4")
            .queryParam("parent", "UA05020030000031457");

        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2,apiResult3)));
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
        assertEquals("Вінниця", result.getName().get("name"));
    }

    @Test
    void testGetCityByNameAndRegionName_NotFoundException() {
        LocationDto city1 = LocationDto.builder()
            .id("UA05020030010063857")
            .parentId("UA05020030000031457")
            .name(Collections.singletonMap("name", "Вінниця"))
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
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "2")
            .queryParam("parent", "UA05000000000010236");

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
                .queryParam("page", "1")
                .queryParam("page_size", "5000")
                .queryParam("level", "3")
                .queryParam("parent", "UA05020000000026686");
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
            .queryParam("page", "1")
            .queryParam("page_size", "5000")
            .queryParam("level", "4")
            .queryParam("parent", "UA46060250000025047");
        when(restTemplate.exchange(eq(builder.build().encode().toUri()), eq(HttpMethod.GET), eq(null),
                any(ParameterizedTypeReference.class)))
                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2,apiResult3)));
        List<LocationDto> cities = locationApiService.getAllCitiesById("UA46060250000025047");
        assertEquals(3, cities.size());
        assertLocationDto(cities.get(0), "UA46060250010015970", "UA46060250000025047", "Львів", "Lviv");
        assertLocationDto(cities.get(1), "UA46060250020038547", "UA46060250000025047", "Винники", "Vynnyky");
        assertLocationDto(cities.get(2), "UA46060250030012851", "UA46060250000025047", "Дубляни", "Dubliany");
    }


    @Test
    void testFindLocationByName_WhenLocationsEmpty_ThenCallGetCityByNameFromRegionSide() {

        LocationDto result = locationApiService.findLocationByName(new ArrayList<>(), "Львівська", "Львів", "Location not found: ");
        assertNotNull(result);
        assertEquals("Львів", result.getName().get("name"));
    }
}