package greencity.entity.parameters;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
