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

import java.io.IOException;
import java.io.Serializable;

/**
 * Memory usage. Immutable.
 *
 * @author Kohsuke Kawaguchi
 */
public class MemoryUsage implements Serializable {
    /**
     * Total physical memory of the system, in bytes.
     * -1 if unknown.
     */
    public final long totalPhysicalMemory;

    /**
     * Of the total physical memory of the system, available bytes.
     * -1 if unknown.
     */
    public final long availablePhysicalMemory;

    /**
     * Total number of swap space in bytes.
     * -1 if unknown.
     */
    public final long totalSwapSpace;

    /**
     * Available swap space in bytes.
     * -1 if unknown.
     */
    public final long availableSwapSpace;

    public MemoryUsage(long totalPhysicalMemory, long availablePhysicalMemory, long totalSwapSpace, long availableSwapSpace) {
        this.totalPhysicalMemory = totalPhysicalMemory;
        this.availablePhysicalMemory = availablePhysicalMemory;
        this.totalSwapSpace = totalSwapSpace;
        this.availableSwapSpace = availableSwapSpace;
    }

    MemoryUsage(long[] v) throws IOException {
        this(v[0],v[1],v[2],v[3]);
        if(!hasData(v))
            throw new IOException("No data available");
    }

    @Override
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

    /*package*/ static boolean hasData(long[] values) {
        for (long v : values)
            if(v!=-1)   return true;
        return false;
    }

    private static final long serialVersionUID = 1L;
}
