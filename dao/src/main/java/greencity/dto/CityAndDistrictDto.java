package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CityAndDistrictDto {
	private Long id;
	private String city;
	private String cityEn;
	private String district;
	private String districtEn;
}
