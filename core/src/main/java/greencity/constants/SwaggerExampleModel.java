package greencity.constants;

public final class SwaggerExampleModel {
    private SwaggerExampleModel() {
    }

    private static final String BEFORE_EXAMPLE = """
        <div>
        \t<ul class="tab">
        \t\t<li class="tabitem active">
        \t\t\t<a class="tablinks" data-name="example">Example Value</a>
        \t\t</li>
        \t\t<li class="tabitem">
        \t\t\t<a class="tablinks" data-name="model">Model</a>
        \t\t</li>
        \t</ul>
        \t<pre>
        """;

    private static final String AFTER_EXAMPLE = "\t</pre>\n"
        + "</div>";

    private static final String EMPLOYEE_BEGIN =
        """
              "employeeDto":\s
            {
              "firstName": "string",
              "lastName": "string",
              "phoneNumber": "string",
              "email": "string",
            """;

    private static final String EMPLOYEE_END =
        """
              "employeePositions": [\s
             {
                  "id": 0,
                  "name": "string"
              }
             ]},
             "tariffId":   [
             0\s
              ]
            }""";

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
