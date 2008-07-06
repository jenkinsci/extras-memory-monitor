package org.jvnet.hudson;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class MemoryMonitorTest extends TestCase {
    public void test1() throws IOException {
        MemoryUsage data = MemoryMonitor.get().monitor();
        System.out.println(data);
    }
}
