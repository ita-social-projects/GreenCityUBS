package greencity.dto.viber.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReceiverDto {
    private String id;
    private String name;
    private String email;
}
