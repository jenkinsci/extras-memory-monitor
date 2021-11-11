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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For Solaris, where top(1) is an optional install. 
 *
 * @author Kohsuke Kawaguchi
 */
public class Solaris extends AbstractMemoryMonitorImpl {

    @Override
    public MemoryUsage monitor() throws IOException {
        long[] v = getSwap();
        return new MemoryUsage(
            getTotalPhysicalMemory(),
            getAvailablePhysicalMemory(),
            v[0],v[1] 
        );
    }

    private long getTotalPhysicalMemory() throws IOException {
        Process proc = startProcess("/usr/sbin/prtdiag");
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()));
        try {
            String line;
            while ((line=r.readLine())!=null) {
                if (line.contains("Memory size:")) {
                    line = line.substring(line.indexOf(':')+1).trim();
                    return parse(line);
                }
            }
            return -1;
        } finally {
            r.close();
        }
    }

    private long getAvailablePhysicalMemory() throws IOException {
        Process proc = startProcess("vmstat");
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()));
        try {
            String line;
            while ((line=r.readLine())!=null) {
                if (NUMBER_ONLY.matcher(line).matches()) {
                    return Long.parseLong(line.trim().split(" +")[4])*1024;
                }
            }
            return -1;
        } finally {
            r.close();
        }
    }

    /**
     * Returns total/availablae.
     */
    private long[] getSwap() throws IOException {
        long[] v = new long[]{-1,-1};
        Process proc = startProcess("/usr/sbin/swap","-s");
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()));
        /* output

            $ uname -a; swap -s
            SunOS kohsuke2 5.9 Generic_112233-12 sun4u sparc SUNW,Sun-Blade-2500 Solaris
            total: 800296k bytes allocated + 181784k reserved = 982080k used, 6014528k available
          */
        try {
            String line = r.readLine().toLowerCase();

            Matcher m = USED_SWAP.matcher(line);
            if (m.find()) {
                v[0] = Long.parseLong(m.group(1))*1024;
            }

            m = AVAILABLE_SWAP.matcher(line);
            if (m.find()) {
                v[1] = Long.parseLong(m.group(1))*1024;
            }

            // we want total/available, not used/available.
            if (v[0]!=-1 && v[1]!=-1)
                v[0] += v[1];
            return v;
        } finally {
            r.close();
        }
    }

    private Process startProcess(String... cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.getOutputStream().close();
        return proc;
    }

    private static final Pattern NUMBER_ONLY = Pattern.compile("[0-9 ]+");
    private static final Pattern USED_SWAP = Pattern.compile(" ([0-9]+)k used");
    private static final Pattern AVAILABLE_SWAP = Pattern.compile(" ([0-9]+)k available");
}
