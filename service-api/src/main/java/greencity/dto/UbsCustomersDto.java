package greencity.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class UbsCustomersDto implements Serializable {
    private String name;
    private String email;
    private String phoneNumber;
}
