package greencity.entity.parameters;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class MultiValue {
    private List<Boolean> booleans;
    private List<Integer> integers;
    private List<Float> floats;
    private List<String> strings;
    private List<LocalDate> dates;
    private List<LocalTime> times;
    private Class clazz;

    /**
     * Constructor.
     **/
    public MultiValue() {
    }
}
