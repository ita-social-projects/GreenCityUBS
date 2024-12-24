package greencity.constants;

public final class SwaggerExampleModel {
    private SwaggerExampleModel() {
    }

    private static final String BEFORE_EXAMPLE = """
        <div>
            <ul class="tab">
                <li class="tabitem active">
                    <a class="tablinks" data-name="example">Example Value</a>
                </li>
                <li class="tabitem">
                    <a class="tablinks" data-name="model">Model</a>
                </li>
            </ul>
            <pre>
        """;

    private static final String AFTER_EXAMPLE = """
            </pre>
        </div>""";

    private static final String EMPLOYEE_BEGIN = """
          "employeeDto":\s
        {
          "firstName": "string",
          "lastName": "string",
          "phoneNumber": "string",
          "email": "string",
        """;

    private static final String EMPLOYEE_END = """
          "employeePositions": [\s
         {
              "id": 0,
              "name": "string"
          }
         ]},
         "tariffId": \
          [
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
