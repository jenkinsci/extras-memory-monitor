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

    public void test2() throws IOException {
        MemoryUsage data = new Top().monitor();
        System.out.println(data);
    }
}
