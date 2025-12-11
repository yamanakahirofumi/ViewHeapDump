package com.example.viewheapdump;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class HeapDumpFileTest {
    @Test
    public void testView() throws Exception {
        HeapDumpFile heapDumpFile = new HeapDumpFile(
                Path.of(this.getClass().getClassLoader().getResource("p1/heap.hprof").toURI()).toFile());

        heapDumpFile.view();
    }
}
