package greencity.dto.order;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@JsonSerialize
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsOrderInfoDto {
    @JsonProperty("name")
    private String service;
    @JsonProperty("capacity")
    private String capacity;
    @JsonProperty("price")
    private String cost;
    @JsonProperty("amount")
    private String bagAmount;
    @JsonProperty("summ")
    private String sum;
}
