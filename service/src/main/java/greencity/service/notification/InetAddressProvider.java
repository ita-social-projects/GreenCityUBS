package greencity.service.notification;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class InetAddressProvider {
    /**
     * Method to get HostName [InetAddress.getLocalHost().getHostName()].
     *
     * @return the host name of the local host
     * @throws IllegalStateException if there is an error getting the host name.
     */
    public String getInetAddressHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Error getting host name: " + e.getMessage(), e);
        }
    }
}
