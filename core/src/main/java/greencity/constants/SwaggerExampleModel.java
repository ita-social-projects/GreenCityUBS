package greencity.constants;

public final class SwaggerExampleModel {
    private SwaggerExampleModel() {
    }

    private static final String BEFORE_EXAMPLE = "<div>\n"
        + "\t<ul class=\"tab\">\n"
        + "\t\t<li class=\"tabitem active\">\n"
        + "\t\t\t<a class=\"tablinks\" data-name=\"example\">Example Value</a>\n"
        + "\t\t</li>\n"
        + "\t\t<li class=\"tabitem\">\n"
        + "\t\t\t<a class=\"tablinks\" data-name=\"model\">Model</a>\n"
        + "\t\t</li>\n"
        + "\t</ul>\n"
        + "\t<pre>\n";

    private static final String AFTER_EXAMPLE = "\t</pre>\n"
        + "</div>";

    private static final String EMPLOYEE_BEGIN =
        "  \"employeeDto\": \n"
            + "{\n"
            + "  \"firstName\": \"string\",\n"
            + "  \"lastName\": \"string\",\n"
            + "  \"phoneNumber\": \"string\",\n"
            + "  \"email\": \"string\",\n";

    private static final String EMPLOYEE_END =
        "  \"employeePositions\": [ \n"
            + " {\n"
            + "      \"id\": 0,\n"
            + "      \"name\": \"string\"\n"
            + "  }\n"
            + " ]},\n"
            + " \"tariffId\": "
            + "  [\n 0 \n"
            + "  ]\n"
            + "}";

    public static final String ADD_NEW_EMPLOYEE =
        BEFORE_EXAMPLE
            + "{\n"
            + EMPLOYEE_BEGIN
            + EMPLOYEE_END
            + AFTER_EXAMPLE;

    public static final String EMPLOYEE_DTO =
        BEFORE_EXAMPLE
            + "{\n"
            + EMPLOYEE_BEGIN
            + "  \"id\": 0,\n"
            + EMPLOYEE_END
            + AFTER_EXAMPLE;
}
