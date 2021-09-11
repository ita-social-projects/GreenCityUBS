package greencity.entity.parameters;

import greencity.entity.enums.EditType;
import greencity.entity.language.Title;
import lombok.*;

import javax.persistence.*;

//@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@ToString
@Builder
public class ColumnState {
    //@Id
    private long id;
    //@Column(nullable = false)
    private String name;
    //@Column(nullable = false)
    private int weight;
    //@Column(nullable = false)
    private boolean sticky;
    //@Column(nullable = false)
    private boolean visible;
    //@Column(nullable = false)
    private int index;
    //@Column(nullable = false)
    private EditType editType;

    //@OneToOne
    //@Column(nullable = false)
    private Title title;

    @ManyToOne
    @JoinColumn(name = "table_parameters_id")
    private TableParameters tableParameters;
}
