package org.jvnet.hudson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
        BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
