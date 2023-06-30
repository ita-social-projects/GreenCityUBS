//package greencity.service.locations;
//
//import greencity.dto.location.api.LocationDto;
//import greencity.exceptions.NotFoundException;
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.net.URI;
//import java.util.Map;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Arrays;
//import java.util.ArrayList;
//import java.util.Collections;
//
//import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//public class LocationApiServiceTest {
//    private Map<String, Object> getApiResult(String code, String parent_id, String name, String nameEn) {
//        Map<String, Object> apiResult = new HashMap<>();
//        apiResult.put("code", code);
//        apiResult.put("parent_id", parent_id);
//        apiResult.put("name", name);
//        apiResult.put("name_en", nameEn);
//        return apiResult;
//    }
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    private ResponseEntity<Map> prepareResponseEntity(List<Map<String, Object>> results) {
//        Map<String, Object> apiResponse = new HashMap<>();
//        apiResponse.put("results", results);
//        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
//    }
//
//    private void assertLocationDto(LocationDto locationDto, String expectedId, String expectedParentId,
//        String expectedName, String expectedNameEn) {
//        assertEquals(expectedId, locationDto.getId());
//        assertEquals(expectedParentId, locationDto.getParentId());
//        assertEquals(expectedName, locationDto.getName().get("name"));
//        assertEquals(expectedNameEn, locationDto.getName().get("name_en"));
//    }
//    @Test
//    public void testGetAllDistrictsInCityByCityID() {
//        UriComponentsBuilder builder =
//                UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
//                        .queryParam("page","1")
//                        .queryParam("page_size", "5000")
//                        .queryParam("level", "5")
//                        .queryParam("parent", "UA46060250010015970")
//                ;
//        URI apiUrl = builder.build().encode().toUri();
//
//        Map<String, Object> apiResult1 =
//                getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
//        Map<String, Object> apiResult2 =
//                getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByCityID("UA46060250010015970");
//        assertEquals(2, districts.size());
//        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
//        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
//                "Zaliznychnyi");
//    }
//
//    @Test
//    public void testGetRegionByName() {
//        UriComponentsBuilder builder =
//                UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
//                        .queryParam("page","1")
//                        .queryParam("page_size", "5000")
//                        .queryParam("name", "Вінницька")
//                        .queryParam("level", "1");
//        URI apiUrl = builder.build().encode().toUri();
//
//        Map<String, Object> apiResult1 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        LocationDto region = locationApiService.getRegionByName("Вінницька");
//        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
//    }
//
//    @Test
//    public void testGetRegionByNameEn() {
//        UriComponentsBuilder builder =
//                UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
//                        .queryParam("page","1")
//                        .queryParam("page_size", "5000")
//                        .queryParam("name", "Vinnytska")
//                        .queryParam("level", "1");
//        URI apiUrl = builder.build().encode().toUri();
//
//        Map<String, Object> apiResult1 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        LocationDto region = locationApiService.getRegionByName("Vinnytska");
//        assertLocationDto(region, "UA05000000000010236", null, "Вінницька", "Vinnytska");
//    }
//
//
//
//
//
//
//
//    @Test
//    public void testGetCityByNameAndRegionName() {
//        UriComponentsBuilder builder = UriComponentsBuilder
//                .fromHttpUrl("https://directory.org.ua/api/katottg")
//                .queryParam("page_size", 5000)
//                .queryParam("name", "Вінницька");
//
//        UriComponentsBuilder builder2 = UriComponentsBuilder
//                .fromHttpUrl("https://directory.org.ua/api/katottg")
//                .queryParam("page_size", 2)
//                .queryParam("code", "UA05020000000026686")
//                .queryParam("level", 2);
//
//        UriComponentsBuilder builder3 = UriComponentsBuilder
//                .fromHttpUrl("https://directory.org.ua/api/katottg")
//                .queryParam("page", 1)
//                .queryParam("page_size", 2)
//                .queryParam("code", "UA05020030000031457")
//                .queryParam("level", 3);
//
//        UriComponentsBuilder builder4 = UriComponentsBuilder
//                .fromHttpUrl("https://directory.org.ua/api/katottg")
//                .queryParam("page_size", 5000)
//                .queryParam("parent", "UA05020030000031457")
//                .queryParam("level", 4);
//
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult41 =
//            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
//        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
//        LocationDto city1 = LocationDto.builder().id("UA05020030010063857").parentId("UA05020030000031457")
//            .name(Collections.singletonMap("name", "Вінниця")).build();
//        LocationDto city2 = LocationDto.builder().id("UA05020030020063505").parentId("UA05020030000031457")
//            .name(Collections.singletonMap("name", "Десна")).build();
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        // Mocking RestTemplate responses
//        when(restTemplate.getForEntity(eq(builder.build().encode().toUri()), eq(Map.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//        when(restTemplate.getForEntity(eq(builder2.build().encode().toUri()), eq(Map.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//        when(restTemplate.getForEntity(eq(builder3.build().encode().toUri()), eq(Map.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//        when(restTemplate.getForEntity(eq(builder4.build().encode().toUri()), eq(Map.class))).thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
//
//        LocationDto result = locationApiService.getCityInRegion("Вінницька", Arrays.asList(city1, city2));
//        assertNotNull(result);
//        assertEquals("UA05020030010063857", result.getId());
//        assertEquals("Вінниця", result.getName().get("name"));
//    }
//
//    @Test
//    public void testNullParentParameter() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(IllegalArgumentException.class, () -> {
//            locationApiService.getAllDistrictsInCityByCityID(null);
//        });
//    }
//
//
//    @Test
//    public void testEmptyParentParameter() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getAllDistrictsInCityByCityID("");
//        });
//    }
//
//    @Test
//    public void testNullNameParameterInGetRegionByName() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(IllegalArgumentException.class, () -> {
//            locationApiService.getRegionByName(null);
//        });
//    }
//
//    @Test
//    public void testEmptyNameParameterInGetRegionByName() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getRegionByName("");
//        });
//    }
//
//    @Test
//    public void testNonExistentParentParameter() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getAllDistrictsInCityByCityID("NonExistentParent");
//        });
//    }
//
//    @Test
//    public void testNonExistentNameParameterInGetRegionByName() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getRegionByName("NonExistentName");
//        });
//    }
//
//    @Test
//    public void testAPIErrors404() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getRegionByName("NonExistentName");
//        });
//    }
//
//    @Test
//    public void testAPIErrors500() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5";
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(null);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getRegionByName("NonExistentName");
//        });
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames_CityWithoutDistricts() {
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult41 =
//            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
//        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
//
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&level=1", Map.class))
//            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020000000026686&level=3", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Вінниця&level=4",
//            Map.class)).thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
//
//        assertThrows(NotFoundException.class, () -> {
//            List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Вінницька", "Вінниця");
//        });
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames_Kyiv() {
//        Map<String, Object> apiResult1 = getApiResult("UA80000000000093317", null, "Київ", "Kyiv");
//        Map<String, Object> apiResult51 =
//            getApiResult("UA80000000000126643", "UA80000000000093317", "Голосіївський", "Holosiivskyi");
//        Map<String, Object> apiResult52 =
//            getApiResult("UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");
//
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&level=1", Map.class))
//            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA80000000000093317&level=5", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));
//        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Київська", "Київ");
//
//        assertEquals(2, districts.size());
//        assertLocationDto(districts.get(0), "UA80000000000126643", "UA80000000000093317", "Голосіївський",
//            "Holosiivskyi");
//        assertLocationDto(districts.get(1), "UA80000000000210193", "UA80000000000093317", "Дарницький", "Darnytskyi");
//
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames() {
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA46060000000042587", "UA46000000000026241", "Львівський", "Lvivskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA46060000000042587", "Львівська", "Lvivska");
//        Map<String, Object> apiResult41 = getApiResult("UA46060250010015970", "UA05020030000031457", "Львів", "Lviv");
//        Map<String, Object> apiResult51 =
//            getApiResult("UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
//        Map<String, Object> apiResult52 =
//            getApiResult("UA46060250010259421", "UA46060250010015970", "Залізничний", "Zaliznychnyi");
//
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&level=1", Map.class))
//            .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46000000000026241&level=2", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060000000042587&level=3", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Львів&level=4",
//            Map.class)).thenReturn(prepareResponseEntity(Arrays.asList(apiResult41)));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250010015970&level=5", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult51, apiResult52)));
//        List<LocationDto> districts = locationApiService.getAllDistrictsInCityByNames("Львівська", "Львів");
//
//        assertEquals(2, districts.size());
//        assertLocationDto(districts.get(0), "UA46060250010121390", "UA46060250010015970", "Галицький", "Halytskyi");
//        assertLocationDto(districts.get(1), "UA46060250010259421", "UA46060250010015970", "Залізничний",
//            "Zaliznychnyi");
//
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames_CityNotFound() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=4";
//        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No City", "No City En");
//        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
//        });
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames_LocalCommunityNotFound() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=3";
//        Map<String, Object> apiResult1 = getApiResult("UA000000001", "UA000000000", "No Community", "No Community En");
//        List<Map<String, Object>> results = Collections.singletonList(apiResult1);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getAllDistrictsInCityByNames("RegionName", "CityName");
//        });
//
//    }
//
//    @Test
//    public void testGetAllDistrictsInCityByNames_NoDistricts() {
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        String regionName = "Вінницька";
//        String cityName = "Вінниця";
//        Map<String, String> name = new HashMap<>();
//        name.put("name", "Вінниця");
//        name.put("name_en", "Vinnytsia");
//        LocationDto city =
//            LocationDto.builder().id("UA05020030010063857").parentId("UA05020030000031457").name(name).build();
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&name=" + cityName + "&level=4", Map.class))
//                .thenReturn(prepareResponseEntity(
//                    Collections.singletonList(getApiResult(city.getId(), city.getParentId(), cityName, cityName))));
//
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=" + city.getId() + "&level=5", Map.class))
//                .thenReturn(prepareResponseEntity(new ArrayList<>()));
//
//        List<LocationDto> allDistricts = locationApiService.getAllDistrictsInCityByNames(regionName, cityName);
//
//        assertNotNull(allDistricts);
//        assertFalse(allDistricts.isEmpty());
//        assertEquals(1, allDistricts.size());
//        assertLocationDto(allDistricts.get(0), "UA05020030010063857", "UA05020030000031457", "Вінниця", "Вінниця");
//    }
//
//    @Test
//    public void testGetRegion() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=1";
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        List<LocationDto> regions = locationApiService.getAllRegions();
//        assertEquals(2, regions.size());
//        assertLocationDto(regions.get(0), "UA01000000000013043", null, "Автономна Республіка Крим",
//            "Avtonomna Respublika Krym");
//        assertLocationDto(regions.get(1), "UA05000000000010236", null, "Вінницька", "Vinnytska");
//    }
//
//    @Test
//    public void testGetUnrealRegion() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&level=1";
//        LocationDto city1 = LocationDto.builder()
//            .id("UA05020030010063857")
//            .parentId(null)
//            .name(Collections.singletonMap("name", "НЕМАЄ"))
//            .build();
//        Map<String, Object> apiResult1 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//
//        assertThrows(NotFoundException.class, () -> {
//            locationApiService.getRegionByName("НЕМАЄ");
//        });
//    }
//
//    @Test
//    public void testGetCityByName() {
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult41 =
//            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
//        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Вінницька&level=1",
//            Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020000000026686&level=3", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&name=Вінниця&level=4", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
//        LocationDto result = locationApiService.getCityByName("Вінницька", "Вінниця");
//        assertNotNull(result);
//        assertEquals("UA05020030010063857", result.getId());
//        assertEquals("Вінниця", result.getName().get("name"));
//    }
//
//    @Test
//    public void testGetCityByNameEn() {
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult41 =
//            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
//        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Вінницька&level=1",
//            Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020000000026686&level=3", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&name=Vinnytsia&level=4", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
//        LocationDto result = locationApiService.getCityByName("Вінницька", "Vinnytsia");
//        assertNotNull(result);
//        assertEquals("UA05020030010063857", result.getId());
//        assertEquals("Вінниця", result.getName().get("name"));
//    }
//
//    @Test
//    public void testGetCityByNameFromRegionSide() {
//        Map<String, Object> apiResult1 =
//            getApiResult("UA01000000000013043", null, "Автономна Республіка Крим", "Avtonomna Respublika Krym");
//        Map<String, Object> apiResult2 = getApiResult("UA05000000000010236", null, "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 = getApiResult("UA46000000000026241", null, "Львівська", "Lvivska");
//        Map<String, Object> apiResult21 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult32 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult41 =
//            getApiResult("UA05020030010063857", "UA05020030000031457", "Вінниця", "Vinnytsia");
//        Map<String, Object> apiResult42 = getApiResult("UA05020030020063505", "UA05020030000031457", "Десна", "Desna");
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Вінницька&level=1",
//            Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult1, apiResult2, apiResult3)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult21)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020000000026686&level=3", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult32)));
//        when(restTemplate.getForEntity(
//            "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05020030000031457&level=4", Map.class))
//                .thenReturn(prepareResponseEntity(Arrays.asList(apiResult41, apiResult42)));
//        LocationDto result = locationApiService.getCityByNameFromRegionSide("Вінницька", "Вінниця");
//        assertNotNull(result);
//        assertEquals("UA05020030010063857", result.getId());
//        assertEquals("Вінниця", result.getName().get("name"));
//    }
//
//    @Test
//    public void testGetCityByNameAndRegionName_NotFoundException() {
//        LocationDto city1 = LocationDto.builder().id("UA05020030010063857").parentId("UA05020030000031457")
//            .name(Collections.singletonMap("name", "Вінниця")).build();
//
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        when(restTemplate.getForEntity("https://directory.org.ua/api/katottg?page_size=5000&name=Неіснуюча&level=1",
//            Map.class)).thenReturn(null);
//
//        Exception exception = assertThrows(NotFoundException.class,
//            () -> locationApiService.getCityInRegion("Неіснуюча", Arrays.asList(city1)));
////        assertTrue(exception.getMessage().contains(ErrorMessage.CITY_NOT_FOUND));
//    }
//
//    @Test
//    public void testGetDistrictInTheRegion() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA05000000000010236&level=2";
//        Map<String, Object> apiResult1 =
//            getApiResult("UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        Map<String, Object> apiResult2 =
//            getApiResult("UA05040000000050292", "UA05000000000010236", "Гайсинський", "Haisynskyi");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        List<LocationDto> regions = locationApiService.getAllDistrictInTheRegionsById("UA05000000000010236");
//        assertEquals(2, regions.size());
//        assertLocationDto(regions.get(0), "UA05020000000026686", "UA05000000000010236", "Вінницький", "Vinnytskyi");
//        assertLocationDto(regions.get(1), "UA05040000000050292", "UA05000000000010236", "Гайсинський", "Haisynskyi");
//
//    }
//
//    @Test
//    public void testGetLocalCommunity() {
//        UriComponentsBuilder builder =
//                UriComponentsBuilder.fromHttpUrl("https://directory.org.ua/api/katottg")
//                        .queryParam("page","1")
//                        .queryParam("page_size", "5000")
//                        .queryParam("level", "3")
//                        .queryParam("parent", "UA05020000000026686");
//        URI apiUrl = builder.build().encode().toUri();
//        Map<String, Object> apiResult1 =
//            getApiResult("UA05020010000053508", "UA05020000000026686", "Агрономічна", "Ahronomichna");
//        Map<String, Object> apiResult2 =
//            getApiResult("UA05020030000031457", "UA05020000000026686", "Вінницька", "Vinnytska");
//        Map<String, Object> apiResult3 =
//            getApiResult("UA05020050000066991", "UA05020000000026686", "Вороновицька", "Voronovytska");
//        Map<String, Object> apiResult4 =
//            getApiResult("UA05020070000010139", "UA05020000000026686", "Гніванська", "Hnivanska");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2, apiResult3, apiResult4);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        List<LocationDto> localCommunities = locationApiService.getAllLocalCommunitiesById("UA05020000000026686");
//        assertEquals(4, localCommunities.size());
//        assertLocationDto(localCommunities.get(0), "UA05020010000053508", "UA05020000000026686", "Агрономічна",
//            "Ahronomichna");
//        assertLocationDto(localCommunities.get(1), "UA05020030000031457", "UA05020000000026686", "Вінницька",
//            "Vinnytska");
//        assertLocationDto(localCommunities.get(2), "UA05020050000066991", "UA05020000000026686", "Вороновицька",
//            "Voronovytska");
//        assertLocationDto(localCommunities.get(3), "UA05020070000010139", "UA05020000000026686", "Гніванська",
//            "Hnivanska");
//    }
//
//    @Test
//    public void testGetCity() {
//        String apiUrl = "https://directory.org.ua/api/katottg?page_size=5000&parent=UA46060250000025047&level=4";
//        Map<String, Object> apiResult1 = getApiResult("UA46060250010015970", "UA46060250000025047", "Львів", "Lviv");
//        Map<String, Object> apiResult2 =
//            getApiResult("UA46060250020038547", "UA46060250000025047", "Винники", "Vynnyky");
//        Map<String, Object> apiResult3 =
//            getApiResult("UA46060250030012851", "UA46060250000025047", "Дубляни", "Dubliany");
//        List<Map<String, Object>> results = Arrays.asList(apiResult1, apiResult2, apiResult3);
//        ResponseEntity<Map> responseEntity = prepareResponseEntity(results);
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        when(restTemplate.getForEntity(eq(apiUrl), eq(Map.class))).thenReturn(responseEntity);
//        LocationApiService locationApiService = new LocationApiService(restTemplate);
//        List<LocationDto> cities = locationApiService.getAllCitiesById("UA46060250000025047");
//        assertEquals(3, cities.size());
//        assertLocationDto(cities.get(0), "UA46060250010015970", "UA46060250000025047", "Львів", "Lviv");
//        assertLocationDto(cities.get(1), "UA46060250020038547", "UA46060250000025047", "Винники", "Vynnyky");
//        assertLocationDto(cities.get(2), "UA46060250030012851", "UA46060250000025047", "Дубляни", "Dubliany");
//    }
//
//
//
//
//
//
//
//
//
//
//
//}