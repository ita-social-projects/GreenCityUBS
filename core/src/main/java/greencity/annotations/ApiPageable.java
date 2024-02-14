package greencity.annotations;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = "page", schema = @Schema(type = "int", minimum = "0", defaultValue = "0"),
    in = ParameterIn.QUERY, description = "Page index you want to retrieve [0..N]. "
        + "If page index is less than 0 or not specified then default value is used!")
@Parameter(name = "size", schema = @Schema(type = "int", minimum = "1", maximum = "100", defaultValue = "5"),
    in = ParameterIn.QUERY, description = "Number of records per page [1..100]. "
        + "If size is less than 1 or not specified then default value is used!"
        + "If size is bigger than 100, size becomes 100.")
public @interface ApiPageable {
}
