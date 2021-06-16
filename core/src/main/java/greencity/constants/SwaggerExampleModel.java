package greencity.constants;

public final class SwaggerExampleModel {
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

    public static final String ADD_NEW_EMPLOYEE =
        BEFORE_EXAMPLE
            + "{\n"
            + "  \"email\": \"string\",\n"
            + "  \"employeePositions\": [ \n"
            + "    {\n"
            + "      \"id\": 0,\n"
            + "      \"position\": \"string\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"firstName\": \"string\",\n"
            + "  \"lastName\": \"string\",\n"
            + "  \"phoneNumber\": \"string\",\n"
            + "  \"receivingStations\": [\n"
            + "    {\n"
            + "      \"id\": 0,\n"
            + "      \"receivingStation\": \"string\"\n"
            + "    }\n"
            + "  ]\n"
            + "}"
            + AFTER_EXAMPLE;
}
