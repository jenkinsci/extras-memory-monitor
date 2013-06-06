package org.jvnet.hudson;

import java.io.IOException;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class MemoryMonitorTest {

    @Test public void monitor() throws IOException {
        MemoryUsage data = MemoryMonitor.get().monitor();
        System.out.println(data);
    }

    @Test public void top() throws IOException {
        MemoryUsage data = new Top().monitor();
        System.out.println(data);
    }
}
