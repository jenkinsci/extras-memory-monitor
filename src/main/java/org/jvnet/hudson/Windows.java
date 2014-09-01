/*
 * The MIT License
 *
 * Copyright (c) 2008-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jvnet.hudson;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.util.Arrays;
import java.util.List;

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

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("dwLength", "dwMemoryLoad", "ullTotalPhys",
                "ullAvailPhys", "ullTotalPageFile", "ullAvailPageFile",
                "ullTotalVirtual", "ullAvailVirtual", "ullAvailExtendedVirtual");
        }
    }  
}
