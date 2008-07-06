package org.jvnet.hudson;

/**
 * @author Kohsuke Kawaguchi
 */
public class MemoryUsage {
    public final long totalPhysicalMemory;
    public final long availablePhysicalMemory;

    public final long totalSwapSpace;
    public final long availableSwapSpace;

    public MemoryUsage(long totalPhysicalMemory, long availablePhysicalMemory, long totalSwapSpace, long availableSwapSpace) {
        this.totalPhysicalMemory = totalPhysicalMemory;
        this.availablePhysicalMemory = availablePhysicalMemory;
        this.totalSwapSpace = totalSwapSpace;
        this.availableSwapSpace = availableSwapSpace;
    }

    public String toString() {
        return String.format("Memory:%d/%dMB  Swap:%d/%dMB",
            toMB(availablePhysicalMemory),
            toMB(totalPhysicalMemory),
            toMB(availableSwapSpace),
            toMB(totalSwapSpace));        
    }

    private static long toMB(long l) {
        return l/(1024*1024);
    }
}
