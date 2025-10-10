package greencity.entity.admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "settings_text")
@Builder
public class SettingsText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section")
    @NotNull
    private String section;

    @Column(name = "field")
    @NotNull
    private String field;

    @Column(name = "value_uk")
    @NotNull
    private String valueUK;

    @Column(name = "value_en")
    @NotNull
    private String valueEN;

    @Column(name = "updated_at")
    @NotNull
    private LocalDateTime updatedAt;
}
