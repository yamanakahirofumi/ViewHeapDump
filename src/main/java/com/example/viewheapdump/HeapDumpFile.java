package com.example.viewheapdump;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        int i = in.readInt();
        int length = in.readInt();
        in.skip(length);
        System.out.println("number: " + number);
        System.out.println("tag: " + tag);
        System.out.println("timestamp: " + i);
        System.out.println("length: " + length);
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
        }finally {
            return number;
        }
    }
}
