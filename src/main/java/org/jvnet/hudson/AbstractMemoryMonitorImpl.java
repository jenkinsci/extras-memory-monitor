package org.jvnet.hudson;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractMemoryMonitorImpl extends MemoryMonitor {
    protected long parse(String token) {
        token = token.toLowerCase().trim();
        long multiplier = 1;
        if(token.endsWith("b"))
            token = cutTail(token);
        if(token.endsWith("k")) {
            multiplier = 1024L;
            token = cutTail(token);
        }
        if(token.endsWith("m")) {
            multiplier = 1024L*1024;
            token = cutTail(token);
        }
        if(token.endsWith("g")) {
            multiplier = 1024L*1024*1024;
            token = cutTail(token);
        }

        return (long)(Float.parseFloat(token)*multiplier);
    }

    protected String cutTail(String token) {
        return token.substring(0,token.length()-1);
    }

}
