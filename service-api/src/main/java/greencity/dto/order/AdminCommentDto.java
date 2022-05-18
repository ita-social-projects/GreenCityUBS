package greencity.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class AdminCommentDto {
    private Long orderId;
    private String adminComment;
}
