package greencity.dto;

import lombok.*;

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
