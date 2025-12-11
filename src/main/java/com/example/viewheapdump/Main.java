package com.example.viewheapdump;

import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "viewheapdump", mixinStandardHelpOptions = true, version = "viewheapdump 1.0")
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"-f", "--file"}, description = "Heap dump file to view")
    File heapDumpFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        return new HeapDumpFile(heapDumpFile).view();
    }
}
