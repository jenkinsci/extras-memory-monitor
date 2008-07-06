package org.jvnet.hudson;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

/**
 * {@link MemoryMonitor} implementation for Windows.
 *
 * <p>
 * JNA requires that the class and interface be public.
 *
 * @author Kohsuke Kawaguchi
*/
public final class Windows extends MemoryMonitor {
    public MemoryUsage monitor() {
        MEMORYSTATUSEX mse = new MEMORYSTATUSEX();
        Kernel32.INSTANCE.GlobalMemoryStatusEx(mse);
        mse.read();

        return new MemoryUsage(
                mse.ullTotalPhys, mse.ullAvailPhys,
                mse.ullTotalPageFile, mse.ullAvailPageFile);
    }

    public interface Kernel32 extends StdCallLibrary {
        boolean GlobalMemoryStatusEx(MEMORYSTATUSEX p);

        Kernel32 INSTANCE = (Kernel32)Native.loadLibrary("kernel32",Kernel32.class);
    }

    public static final class MEMORYSTATUSEX extends Structure {
        public int dwLength = size();
        public int dwMemoryLoad;
        public long ullTotalPhys;
        public long ullAvailPhys;
        public long ullTotalPageFile;
        public long ullAvailPageFile;
        public long ullTotalVirtual;
        public long ullAvailVirtual;
        public long ullAvailExtendedVirtual;
    }
}
