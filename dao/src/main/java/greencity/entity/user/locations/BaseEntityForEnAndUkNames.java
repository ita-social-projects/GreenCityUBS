package greencity.entity.user.locations;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntityForEnAndUkNames {
    @Column(name = "name_uk", nullable = false)
    private String nameUk;
    @Column(name = "name_en", nullable = false)
    private String nameEn;
}
