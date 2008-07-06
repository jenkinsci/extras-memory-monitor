package org.jvnet.hudson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * {@link MemoryMonitor} that parses the output from the <tt>top</tt> command.
 * @author Kohsuke Kawaguchi
 */
final class Top extends MemoryMonitor {
    public MemoryUsage monitor() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("top0"/*,"-b" MacOS doesn't understand the -b option*/);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.getOutputStream().close();

        // obtain first 8 lines, then kill 'top'
        // output is converted to lower case to simplify matching.
        List<String> lines = new ArrayList<String>();
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while((line=in.readLine())!=null && lines.size()<8)
                lines.add(line.toLowerCase());
            proc.destroy();
            in.close();
        }

        long[] values = new long[5];
        Arrays.fill(values,-1);

        OUTER:
        for( int i=0; i<5; i++ ) {
            for( Pattern p : PATTERNS[i] ) {
                for (String line : lines) {
                    try {
                        Matcher m = p.matcher(line);
                        if(m.find()) {
                            values[i] = parse(m.group(1));
                            continue OUTER;
                        }
                    } catch (NumberFormatException e) {
                        throw new IOException("Failed to parse "+line);
                    }
                }
            }
        }

        if(values[2]==-1 && values[3]!=-1 && values[4]!=-1)
            values[2] = values[3]+values[4];

        return new MemoryUsage(values);
    }

    private static long parse(String token) {
        token = token.trim();
        long multiplier = 1;
        if(token.endsWith("b"))
            token = cutTail(token);
        if(token.endsWith("k")) {
            multiplier = 1024;
            token = cutTail(token);
        }
        if(token.endsWith("m")) {
            multiplier = 1024*1024;
            token = cutTail(token);
        }
        if(token.endsWith("g")) {
            multiplier = 1024*1024*1024;
            token = cutTail(token);
        }

        return Long.parseLong(token)*multiplier;
    }

    private static String cutTail(String token) {
        return token.substring(0,token.length()-1);
    }

/*
On Solaris 10 + top from blastwave
==================================
$ uname -a && which top && top -b | head
SunOS wsinterop 5.10 Generic_118844-19 i86pc i386 i86pc
/opt/csw/bin/top
last pid: 27683;  load avg:  0.04,  0.04,  0.04;       up 39+01:47:58  18:16:35
99 processes: 97 sleeping, 1 zombie, 1 on cpu

Memory: 3647M phys mem, 1621M free mem, 2047M swap, 2047M free swap



On Ubuntu 8.4
==================================
% uname -a && which top && top -b | head
Linux unicorn 2.6.24-19-generic #1 SMP Wed Jun 18 14:15:37 UTC 2008 x86_64 GNU/Linux
/usr/bin/top
top - 18:28:09 up 2 days, 22:39, 10 users,  load average: 1.26, 1.41, 1.35
Tasks: 181 total,   1 running, 179 sleeping,   0 stopped,   1 zombie
Cpu(s):  4.9%us, 21.4%sy,  0.0%ni, 70.7%id,  2.9%wa,  0.0%hi,  0.1%si,  0.0%st
Mem:   4057400k total,  3369188k used,   688212k free,    82488k buffers
Swap:  4192956k total,   655028k used,  3537928k free,  1171404k cached

  PID USER      PR  NI  VIRT  RES  SHR S %CPU %MEM    TIME+  COMMAND
 7041 kohsuke   20   0  823m 411m  15m S   97 10.4 675:46.69 VirtualBox
 6606 root      20   0  241m 107m  19m S   12  2.7  16:30.86 Xorg
 6907 kohsuke   20   0  134m  14m 9184 S    2  0.4   0:51.56 metacity



From http://www.unixtop.org/about.shtml
=======================================
last pid: 15687;  load averages:  0.02,  0.01,  0.01
76 processes:  74 sleeping, 1 stopped, 1 on cpu
CPU states: 91.1% idle,  3.8% user,  5.1% kernel,  0.0% iowait,  0.0% swap
Memory: 32M real, 724K free, 32M swap in use, 368M swap free




From http://www.freebsd.org/doc/en/books/handbook/basics-processes.html
=======================================
% top
last pid: 72257;  load averages:  0.13,  0.09,  0.03    up 0+13:38:33  22:39:10
47 processes:  1 running, 46 sleeping
CPU states: 12.6% user,  0.0% nice,  7.8% system,  0.0% interrupt, 79.7% idle
Mem: 36M Active, 5256K Inact, 13M Wired, 6312K Cache, 15M Buf, 408K Free
Swap: 256M Total, 38M Used, 217M Free, 15% Inuse

  PID USERNAME PRI NICE  SIZE    RES STATE    TIME   WCPU    CPU COMMAND
72257 nik       28   0  1960K  1044K RUN      0:00 14.86%  1.42% top
 7078 nik        2   0 15280K 10960K select   2:54  0.88%  0.88% xemacs-21.1.14
  281 nik        2   0 18636K  7112K select   5:36  0.73%  0.73% XF86_SVGA
  296 nik        2   0  3240K  1644K select   0:12  0.05%  0.05% xterm
48630 nik        2   0 29816K  9148K select   3:18  0.00%  0.00% navigator-linu
  175 root       2   0   924K   252K select   1:41  0.00%  0.00% syslogd
 7059 nik        2   0  7260K  4644K poll     1:38  0.00%  0.00% mutt


The page doesn't include the description of what the memory line really means,
but http://support.apple.com/kb/HT1342 has some description.

Wired memory
This information can't be cached to disk, so it must stay in RAM. The amount depends on what applications you are using.

Active memory
This information is currently in RAM and has recently been used.

Inactive memory
This information has not recently been used but will remain in RAM until another application needs the space in RAM.  Then, Inactive memory will be cached to disk. Leaving Inactive memory in RAM for as long as possible is to your advantage.  If called upon by a process, it is quickly changed to Active memory.

If the inactive memory is cached to disk and is called upon by a process, it will be returned to RAM and marked as Active memory.

Free memory
This memory is not being use

*/

    private static final Pattern[][] PATTERNS = new Pattern[][] {
        // total phys. memory
        new Pattern[] {
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) phys mem"), // Sol10+blastwave
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) total"), // Linux
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) real") // unixtop.org
        },

        // available phys. memory
        new Pattern[] {
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) free")
        },

        // total swap memory
        new Pattern[] {
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) swap,"), // Sol10+blastwave
            Pattern.compile("^swap:.* ([0-9]+[kmb]) total") // Linux
        },

        // available swap memory
        new Pattern[] {
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) free swap"), // Sol10+blastwave
            Pattern.compile("^swap:.* ([0-9]+[kmb]) free"), // Linux
            Pattern.compile("^mem(?:ory)?:.* ([0-9]+[kmb]) swap free")  // unixtop
        },

        // swap in use.
        new Pattern[] {
            Pattern.compile("^mem(?:ory):.* ([0-9]+[kmb]) swap in use")  // unixtop
        }

    };
}
