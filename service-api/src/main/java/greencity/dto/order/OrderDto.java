package greencity.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class OrderDto {
    private String firstName;
    private String lastName;
    private String address;
    private String addressComment;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
}
