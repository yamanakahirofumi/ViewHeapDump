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

public class HeapDumpFile {
    private final File heapDumpFile;
    private int identifierSize;
    private final Map<Long, String> utf8Strings = new HashMap<>();
    private final Map<Long, Frame> frames = new HashMap<>();

    public record Frame(long stackFrameId, long methodNameId, long methodSignatureId, long sourceFileNameId, int classSerialNumber, int lineNumber) {}

    public HeapDumpFile(File heapDumpFile) {
        this.heapDumpFile = heapDumpFile;
    }

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
            while (in.available() > 0) {
                this.viewRecord(in, number);
                number++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void viewRecord(DataInputStream in, int number) throws IOException {
        byte tag = in.readByte();
        int time_offset_micro = in.readInt();
        long length = Integer.toUnsignedLong(in.readInt());

        System.out.println("number: " + number);
        System.out.printf("tag: %d (0x%02X)\n", tag, tag);
        System.out.println("time_offset_micro: " + time_offset_micro);
        System.out.println("length: " + length);

        switch (tag) {
            case 0x01: { // HPROF_UTF8
                if (length < identifierSize) {
                    System.out.println("Invalid length for HPROF_UTF8 record, skipping.");
                    if (length > 0) in.skip(length);
                    break;
                }
                long id = readId(in);
                long bytesToRead = length - identifierSize;
                if (bytesToRead > 1_000_000) { // sanity limit
                    System.out.println("HPROF_UTF8 record too large to process, skipping.");
                    in.skip(bytesToRead);
                    break;
                }
                byte[] bytes = new byte[(int) bytesToRead];
                in.readFully(bytes);
                String s = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("HPROF_UTF8: " + s);
                this.utf8Strings.put(id, s);
                break;
            }
            case 0x02: { // HPROF_LOAD_CLASS
                int classSerialNumber = in.readInt();
                long classObjectId = readId(in);
                int stackTraceSerialNumber = in.readInt();
                long classNameId = readId(in);
                System.out.println("HPROF_LOAD_CLASS: " + classSerialNumber + ", " + classObjectId + ", " + stackTraceSerialNumber + ", " + this.utf8Strings.get(classNameId));
                break;
            }
            case 0x04: { // HPROF_FRAME
                long stackFrameId = readId(in);
                long methodNameId = readId(in);
                long methodSignatureId = readId(in);
                long sourceFileNameId = readId(in);
                int classSerialNumber = in.readInt();
                int lineNumber = in.readInt();
                Frame frame = new Frame(stackFrameId, methodNameId, methodSignatureId, sourceFileNameId, classSerialNumber, lineNumber);
                frames.put(stackFrameId, frame);
                System.out.println("HPROF_FRAME: " + frame);
                break;
            }
            case 0x05: { // HPROF_TRACE
                int stackTraceSerialNumber = in.readInt();
                int threadSerialNumber = in.readInt();
                int numberOfFrames = in.readInt();
                System.out.println("HPROF_TRACE: " + stackTraceSerialNumber + ", " + threadSerialNumber + ", " + numberOfFrames);
                for (int i = 0; i < numberOfFrames; i++) {
                    long frameId = readId(in);
                    Frame frame = frames.get(frameId);
                    if (frame != null) {
                        System.out.println("  " + utf8Strings.get(frame.methodNameId()) + utf8Strings.get(frame.methodSignatureId()) + " (" + utf8Strings.get(frame.sourceFileNameId()) + ":" + frame.lineNumber() + ")");
                    } else {
                        System.out.println("  Unknown frame: " + frameId);
                    }
                }
                break;
            }
            case 0x0C: // HPROF_HEAP_DUMP
            case 0x1C: { // HPROF_HEAP_DUMP_SEGMENT
                System.out.println("HPROF_HEAP_DUMP or HPROF_HEAP_DUMP_SEGMENT");
                in.skip(length);
                break;
            }
            default:
                in.skip(length);
                System.out.println("Skipping record with tag: " + tag);
        }
        System.out.println("--------------------");
    }

    private long readId(DataInputStream in) throws IOException {
        if (identifierSize == 4) {
            return Integer.toUnsignedLong(in.readInt());
        } else {
            return in.readLong();
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

            this.identifierSize = in.readInt(); // big-endian by default
            long timestamp = in.readLong();
            number = number + 12;

            String timestampIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.ofEpochMilli(timestamp));

            System.out.println("formatString: " + formatString);
            System.out.println("identifierSize: " + this.identifierSize);
            System.out.println("timestamp: " + timestamp);
            System.out.println("timestampIso: " + timestampIso);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            return number;
        }
    }
}
