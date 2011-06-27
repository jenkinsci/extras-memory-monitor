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

import java.io.File;
import java.io.IOException;

/**
 * Encapsulates how to compute {@link MemoryUsage}. 
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MemoryMonitor {
    /**
     * Obtains the memory usage statistics.
     *
     * @return
     *      always non-null object.
     * @throws IOException
     *      If the computation fails for some reason.
     */
    public abstract MemoryUsage monitor() throws IOException;

    /**
     * Obtains the {@link MemoryMonitor} implementation suitable
     * for the current platform.
     *
     * @throws IOException
     *      if no applicable implementation is found.
     */
    public static MemoryMonitor get() throws IOException {
        if(INSTANCE==null)
            INSTANCE = obtain();
        return INSTANCE;
    }

    private static MemoryMonitor obtain() throws IOException {
        if(File.pathSeparatorChar==';')
            return new Windows();

        if(new File("/proc/meminfo").exists())
            return new ProcMemInfo();   // Linux has this. Exactly since when, I don't know.

        // is 'top' available? if so, use it
        try {
            Top top = new Top();
            top.monitor();
            return top;
        } catch (Throwable _) {
            // fall through next
        }

        // Solaris?
        try {
            Solaris solaris = new Solaris();
            solaris.monitor();
            return solaris;
        } catch(Throwable _) {
            // next
        }

        throw new IOException(String.format("No suitable implementation found: os.name=%s os.arch=%s sun.arch.data.model=%s",
                System.getProperty("os.name"),System.getProperty("os.arch"),System.getProperty("sun.arch.data.model")));
    }

    /**
     * Main for test
     */
    public static void main(String[] args) throws Exception {
        System.out.println(new Solaris().monitor());
    }

    private static volatile MemoryMonitor INSTANCE = null;
}
