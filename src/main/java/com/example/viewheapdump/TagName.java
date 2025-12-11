package com.example.viewheapdump;

import java.util.Arrays;

public enum TagName {
    STRING_IN_UTF8((byte) 0x01),
    LOAD_CLASS((byte) 0x02),
    STACK_FRAME((byte) 0x04),
    STACK_TRACE((byte) 0x05),

    HEAP_SUMMARY((byte) 0x07),

    HEAP_DUMP((byte) 0x0C),
    HEAP_DUMP_SEGMENT((byte) 0x1C),
    HEAP_DUMP_END((byte) 0x2C),
    // sub
    ;

    private final byte id;

    TagName(byte id) {
        this.id = id;
    }

    public static TagName of(byte id) {
        return Arrays.stream(values())
                .filter(t -> t.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown tag name: " + id));
    }
}
