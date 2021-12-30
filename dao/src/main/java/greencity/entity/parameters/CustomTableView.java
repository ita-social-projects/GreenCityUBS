package greencity.entity.parameters;

import lombok.*;

import javax.persistence.*;
import javax.persistence.Column;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class CustomTableView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    String uuid;

    @Column(columnDefinition = "text", length = 551)
    String titles;
}
