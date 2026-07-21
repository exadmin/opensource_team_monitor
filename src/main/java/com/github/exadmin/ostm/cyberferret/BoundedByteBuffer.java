package com.github.exadmin.ostm.cyberferret;

import java.io.OutputStream;

final class BoundedByteBuffer extends OutputStream {
    private final byte[] buffer;
    private int start;
    private int size;

    BoundedByteBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Buffer capacity must be positive.");
        buffer = new byte[capacity];
    }

    @Override
    public synchronized void write(int value) {
        if (size < buffer.length) {
            buffer[(start + size) % buffer.length] = (byte) value;
            size++;
        } else {
            buffer[start] = (byte) value;
            start = (start + 1) % buffer.length;
        }
    }

    @Override
    public synchronized void write(byte[] bytes, int offset, int length) {
        if (length >= buffer.length) {
            offset += length - buffer.length;
            length = buffer.length;
            start = 0;
            size = 0;
        }
        for (int index = 0; index < length; index++) write(bytes[offset + index]);
    }

    synchronized byte[] toByteArray() {
        byte[] result = new byte[size];
        for (int index = 0; index < size; index++) {
            result[index] = buffer[(start + index) % buffer.length];
        }
        return result;
    }
}
