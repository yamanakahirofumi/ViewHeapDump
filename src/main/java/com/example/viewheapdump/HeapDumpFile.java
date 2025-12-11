package com.example.viewheapdump;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public record HeapDumpFile(File heapDumpFile) {
    public int view() {
        System.out.println("Viewing heap dump file: " + heapDumpFile.getAbsolutePath());
        int size = this.viewHeader();
        this.viewRecords(size);
        return 0;
    }

    private void viewRecords(int headerSize) {
        int number = 1;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(heapDumpFile.toPath())))) {
            in.skip(headerSize);
            Map<Long, String> map = new HashMap<>();
            while (in.available() > 0) {
                this.viewRecord(in, number, map);
                number++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void viewRecord(DataInputStream in, int number, Map<Long, String> map) throws IOException {
        byte tag = in.readByte();
        TagName tagName = TagName.of(tag);
        int i = in.readInt();
        long length = Integer.toUnsignedLong(in.readInt());
        System.out.println("number: " + number);
        System.out.printf("tag: 0x%02X\n", tag);
        System.out.println("tagName: " + tagName);
        System.out.println("timestamp: " + i);
        System.out.println("length: " + length);
        switch (tagName) {
            case STRING_IN_UTF8 -> {
                Long id = in.readLong(); //Id
                String s = new String(in.readNBytes((int) length - 8), StandardCharsets.UTF_8);
                map.put(id, s);
                System.out.println("value: " + s);
            }
            case LOAD_CLASS -> {
                in.readInt();
                in.readLong(); //Id
                in.readInt();
                long refId = in.readLong();
                String s = map.get(refId);
                System.out.println("class name: " + s);
            }
            case STACK_FRAME -> {
                in.readLong();
                in.readLong();
                in.readLong();
                in.readLong();
                in.readInt();
                in.readInt();
            }
            case STACK_TRACE -> {
                in.readInt();
                in.readInt();
                int frames = in.readInt();
                for (int j = 0; j < frames; j++) {
                    in.readLong();
                }
            }
            case HEAP_SUMMARY -> {
                in.readInt();
                in.readInt();
                in.readLong();
                in.readLong();
            }
            case HEAP_DUMP,HEAP_DUMP_SEGMENT -> {
                in.readNBytes((int) length);
            }
            case HEAP_DUMP_END -> {
                // TODO
                in.readNBytes((int) length);
            }
        }
    }

    private int viewHeader() {
        int number = 0;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(this.heapDumpFile.toPath())))) {
            StringBuilder sb = new StringBuilder();
            int b;
            while ((b = in.read()) != -1) {
                number++;
                if (b == 0 || b == '\n') {
                    break;
                }
                sb.append((char) b);
            }
            String formatString = sb.toString();

            int identifierSize = in.readInt(); // big-endian by default
            long timestamp = in.readLong();
            number = number + 12;

            String timestampIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(timestamp));

            System.out.println("formatString: " + formatString);
            System.out.println("identifierSize: " + identifierSize);
            System.out.println("timestamp: " + timestamp);
            System.out.println("timestampIso: " + timestampIso);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return number;
    }
}
