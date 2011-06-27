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
