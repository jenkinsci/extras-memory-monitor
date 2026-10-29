package org.jvnet.hudson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TopTest {

    private static final long M = 1024L * 1024;
    private static final long G = 1024L * M;

    @Test
    void macOsSinceMavericksReportsUnusedPhysicalMemory() throws IOException {
        // top -S -l1 on macOS 27.0.1 (Apple Silicon, 32 GB)
        List<String> lines = Arrays.asList(
                "Processes: 963 total, 2 running, 961 sleeping, 5725 threads ",
                "2026/10/29 17:29:52",
                "Load Avg: 1.19, 1.34, 1.32 ",
                "CPU usage: 13.95% user, 11.46% sys, 74.58% idle ",
                "SharedLibs: 932M resident, 183M data, 142M linkedit.",
                "MemRegions: 908190 total, 8514M resident, 444M private, 4385M shared.",
                "PhysMem: 31G used (3666M wired, 8934M compressor), 374M unused.",
                "VM: 509T vsize, 57G framework vsize, 43086(0) swapins, 238468(0) swapouts.",
                "Swap: 2653M + 1443M free.",
                "Purgeable: 497M 13724(0) pages purged.");

        MemoryUsage usage = new Top().parseLines(lines);

        assertEquals(374 * M, usage.availablePhysicalMemory);
        assertEquals(31 * G + 374 * M, usage.totalPhysicalMemory);
        assertEquals(1443 * M, usage.availableSwapSpace);
        assertEquals(2653 * M + 1443 * M, usage.totalSwapSpace);
    }

    @Test
    void macOsBeforeMavericksReportsFreePhysicalMemory() throws IOException {
        // top -S -l1 on Mac OS X 10.7, from the comment in Top.java
        List<String> lines = Arrays.asList(
                "PhysMem: 648M wired, 2299M active, 1097M inactive, 4044M used, 50M free.", "Swap: 5362M + 782M free.");

        MemoryUsage usage = new Top().parseLines(lines);

        assertEquals(50 * M, usage.availablePhysicalMemory);
        assertEquals(4044 * M + 50 * M, usage.totalPhysicalMemory);
        assertEquals(782 * M, usage.availableSwapSpace);
        assertEquals(5362 * M + 782 * M, usage.totalSwapSpace);
    }

    @Test
    void linuxProcpsReportsTotalAndFree() throws IOException {
        List<String> lines = Arrays.asList(
                "KiB Mem:   3980580 total,  3266708 used,   713872 free,    66592 buffers",
                "KiB Swap:  4124668 total,   121968 used,  4002700 free,   390988 cached");

        MemoryUsage usage = new Top().parseLines(lines);

        assertEquals(3980580 * 1024L, usage.totalPhysicalMemory);
        assertEquals(713872 * 1024L, usage.availablePhysicalMemory);
        assertEquals(4124668 * 1024L, usage.totalSwapSpace);
        assertEquals(4002700 * 1024L, usage.availableSwapSpace);
    }

    @Test
    void outputWithoutMemoryLinesGivesNoData() throws IOException {
        assertNull(new Top().parseLines(Arrays.asList("top: unknown option -- S", "usage: top")));
    }
}
