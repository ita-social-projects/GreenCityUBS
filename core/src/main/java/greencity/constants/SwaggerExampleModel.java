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
        "{\n"
            + "  \"email\": \"string\",\n"
            + "  \"employeePositions\": [ \n"
            + "    {\n"
            + "      \"id\": 0,\n"
            + "      \"name\": \"string\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"firstName\": \"string\",\n";

    private static final String EMPLOYEE_END =
        "  \"lastName\": \"string\",\n"
            + "  \"phoneNumber\": \"string\",\n"
            + "  \"receivingStations\": [\n"
            + "    {\n"
            + "      \"id\": 0,\n"
            + "      \"name\": \"string\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    public static final String ADD_NEW_EMPLOYEE =
        BEFORE_EXAMPLE
            + EMPLOYEE_BEGIN
            + EMPLOYEE_END
            + AFTER_EXAMPLE;

    public static final String EMPLOYEE_DTO =
        BEFORE_EXAMPLE
            + EMPLOYEE_BEGIN
            + "  \"id\": 0,\n"
            + "  \"image\": \"string\",\n"
            + EMPLOYEE_END
            + AFTER_EXAMPLE;
}
