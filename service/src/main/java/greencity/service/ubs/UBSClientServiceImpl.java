package greencity.service.ubs;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UBSClientServiceImpl implements UBSClientService {
    /**
     * This method is used to extract the order ID from the provided data. The data
     * is a Base64 encoded string, which is first decoded into a regular string. The
     * decoded string is then converted into a JSON object, from which the order ID
     * is extracted.
     *
     * @param data The Base64 encoded string containing the order ID.
     * @return The order ID extracted from the data.
     */
    protected Long extractOrderIdFromData(String data) {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(decodedString);
        return Long.valueOf(jsonObject.getString("order_id"));
    }

    /**
     * This method is used to extract the status from the provided data. The data is
     * a Base64 encoded string, which is first decoded into a regular string. The
     * decoded string is then converted into a JSON object, from which the status is
     * extracted.
     *
     * @param data The Base64 encoded string containing the status.
     * @return The status extracted from the data.
     * @note This method is not intended for use in a test environment.
     */
    protected String extractStatusFromData(String data) { // Don`t use in test env
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(decodedString);
        return jsonObject.getString("status");
    }
}
