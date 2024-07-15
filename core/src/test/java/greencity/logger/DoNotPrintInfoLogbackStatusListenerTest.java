package greencity.logger;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoNotPrintInfoLogbackStatusListenerTest {
    @Mock
    private StatusManager statusManager;
    @Mock
    private Context context;

    private DoNotPrintInfoLogbackStatusListener spyListener;

    @BeforeEach
    public void setUp() {
        DoNotPrintInfoLogbackStatusListener listener = new DoNotPrintInfoLogbackStatusListener();
        listener.setContext(mock(ch.qos.logback.core.Context.class));
        spyListener = Mockito.spy(listener);
    }

    @Test
    void testAddStatusEvent_WarnLevel() {
        Status warnStatus = new WarnStatus("Warn", this);
        spyListener.addStatusEvent(warnStatus);
        verify(spyListener, times(1)).addStatusEvent(warnStatus);
    }

    @Test
    void testAddStatusEvent_InfoLevel() {
        Status infoStatus = new InfoStatus("Info", this);
        spyListener.addStatusEvent(infoStatus);
        verify(spyListener, times(1)).addStatusEvent(infoStatus);
    }

}