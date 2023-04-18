package greencity.service.notification;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class InetAddressProvider {
    /**
     * Method to get HostName [InetAddress.getLocalHost().getHostName()].
     */
    public String getInetAddressHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
