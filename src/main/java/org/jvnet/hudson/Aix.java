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
 * AIX
 * 
 * @author qxodream@gmail.com
 */
public class Aix extends AbstractMemoryMonitorImpl {

    @Override
    public MemoryUsage monitor() throws IOException {
        long[] v = getSwap();
        long[] m = getMemUsed();
        return new MemoryUsage(m[0], m[1], v[0], v[1]);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(MemoryMonitor.get().monitor());
    }

    /**
     * Returns total/availablae.
     */
    private long[] getSwap() throws IOException {
        long[] v = new long[] { -1, -1 };
        Process proc = startProcess("lsps", "-s");
        /*
     $ lsps -s
Total Paging Space   Percent Used
      45568MB              17%

         */

        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()))) {
            String line = null;
            while ((line = r.readLine()) != null) {
                Matcher m = SWAP.matcher(line);
                if (m.find()) {
                    long totalSwap = Long.parseLong(m.group(1));
                    String unit = m.group(2);
                    totalSwap = getSize(totalSwap, unit);
                    long used = Long.parseLong(m.group(3));
                    v[0] = totalSwap;
                    if (used > 0) {
                        v[1] = (totalSwap / 100) * (100 - used);
                    }        
                    break;
                }
            }
            return v;
        }
    }

    public long getSize(long totalSwap, String unit) {
        if ("MB".equals(unit)) {
            totalSwap = totalSwap * 1024 * 1024;
        } else if ("GB".equals(unit)) {
            totalSwap = totalSwap * 1024 * 1024;
        }
        return totalSwap;
    }

    private long[] getMemUsed() throws IOException {
        long[] v = new long[] { -1, -1 };
        Process proc = startProcess("vmstat");
        /*
$ vmstat

System configuration: lcpu=16 mem=25920MB

kthr    memory              page              faults        cpu
----- ----------- ------------------------ ------------ -----------
 r  b   avm   fre  re  pi  po  fr   sr  cy  in   sy  cs us sy id wa
 1  1 4986615 96970   0   0   0   0   12   0  34 4619 4005  2  1 98  0

         */
        try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()))) {
            String line = null;
            while ((line = r.readLine()) != null) {
                Matcher m = MEM_TOTAL.matcher(line);
                if (m.find()) {
                    long mem = Long.parseLong(m.group(1));
                    String unit = m.group(2);
                    mem = getSize(mem, unit);
                    v[0]=mem;
                    continue;
                }
                m = MEM_USED.matcher(line);
                if (m.find()) {
                    //long used = Long.parseLong(m.group(1));
                    long free = Long.parseLong(m.group(2));
                    v[1] =  free * 4096;//v[0] == -1 ?  free * 4096 : ( v[0] - used*4096);
                    break;
                }
            }
            return v;            
        }
    }

    private Process startProcess(String... cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.getOutputStream().close();
        return proc;
    }

    private static final Pattern SWAP = Pattern.compile("\\s+(\\d+)(MB|GB|KB)\\s+(\\d{1,2})%$");
    private static final Pattern MEM_USED = Pattern.compile("\\s+\\d+\\s+\\d+\\s+(\\d+)\\s+(\\d+)\\s+");
    private static final Pattern MEM_TOTAL = Pattern.compile("mem=(\\d+)(MB|GB)");
}
