package heap.dump.parser;

import java.util.ArrayList;
import java.util.List;

public class HeapDumpGenerator {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("HeapDumpGenerator started. PID: " + ProcessHandle.current().pid());
        List<Object> objects = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            objects.add(new byte[1024]); // 1KB byte array
        }
        System.out.println("Objects created. Waiting indefinitely...");
        Thread.sleep(Long.MAX_VALUE);
    }
}
