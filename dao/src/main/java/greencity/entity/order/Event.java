package greencity.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "events")
@EqualsAndHashCode(exclude = {"order"})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDateTime eventDate;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "author", nullable = false)
    private String authorName;

    @Column(name = "event_name_eng", nullable = false)
    private String eventNameEng;

    @Column(name = "author_eng", nullable = false)
    private String authorNameEng;

    @ManyToOne
    private Order order;
}
