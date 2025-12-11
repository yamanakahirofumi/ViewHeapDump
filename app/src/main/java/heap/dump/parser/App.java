package heap.dump.parser;

import java.io.File;
import java.io.IOException;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;
import org.netbeans.lib.profiler.heap.JavaClass;

public class App {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java -jar heap-dump-parser.jar <heap-dump-file>");
            System.exit(1);
        }
        String heapDumpFilePath = args[0];
        File heapDumpFile = new File(heapDumpFilePath);
        if (!heapDumpFile.exists()) {
            System.err.println("Error: File not found: " + heapDumpFilePath);
            System.exit(1);
        }

        try {
            Heap heap = HeapFactory.createHeap(heapDumpFile);
            int classCount = 0;
            for (JavaClass javaClass : heap.getAllClasses()) {
                System.out.println("Class " + (++classCount) + ": " + javaClass.getName() + ", instances: " + javaClass.getInstancesCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
