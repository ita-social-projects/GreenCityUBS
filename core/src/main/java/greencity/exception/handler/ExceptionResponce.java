package greencity.exception.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response object with information of occur exception.
 *
 * @author Marian Milian
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExceptionResponce {
    private String message;
    @JsonIgnore
    private String timeStamp;
    @JsonIgnore
    private String trace;
    @JsonIgnore
    private String path;

    /**
     * Constructor with parameters.
     */
    public ExceptionResponce(Map<String, Object> errorAttributes) {
        this.setPath((String) errorAttributes.get("path"));
        this.setMessage((String) errorAttributes.get("message"));
        this.setTimeStamp((String) errorAttributes.get("timestamp"));
        this.setTrace((String) errorAttributes.get("trace"));
    }
}
