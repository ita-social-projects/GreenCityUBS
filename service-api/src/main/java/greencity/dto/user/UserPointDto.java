package greencity.dto.user;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPointDto implements Serializable {
    private int points;
}