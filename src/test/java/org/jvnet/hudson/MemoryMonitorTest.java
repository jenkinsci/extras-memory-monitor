package org.jvnet.hudson;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * @author Kohsuke Kawaguchi
 */
class MemoryMonitorTest {

    @Test
    void monitor() throws IOException {
        MemoryUsage data = MemoryMonitor.get().monitor();
        System.out.println(data);
    }

    @Test
    void top() throws IOException {
        assumeFalse(isWindows(), "Windows cannot run this test");
        MemoryUsage data = new Top().monitor();
        System.out.println(data);
    }

    private static boolean isWindows() {
        return File.pathSeparatorChar == ';';
    }
}
