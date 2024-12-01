package greencity.annotations;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
    @Parameter(name = "lang", description = "Code of the needed language.", in = ParameterIn.QUERY,
        schema = @Schema(type = "string")),
    @Parameter(name = "page", in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"),
        description = "Page index you want to retrieve [0..N]. "
            + "If page index is less than 0 or not specified then default description is used!"),
    @Parameter(name = "size", in = ParameterIn.QUERY,
        schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "5"),
        description = "Number of records per page [1..100]. "
            + "If size is less than 1 or not specified then default description is used!"
            + "If size is bigger than 100, size becomes 100.")
})
public @interface ApiPageableWithLocale {
}
