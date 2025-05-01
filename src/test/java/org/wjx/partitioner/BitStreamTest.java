package org.wjx.partitioner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BitStreamTest {
    BitStream bitStream;

    @Test
    void pop() {
        byte[] input = new byte[]{(byte)0b00110011, (byte)0b10101010};
        bitStream  = new BitStream(input);
        for ( int i = 0 ; i<input.length ; i++ ) {
            for ( int j = 0 ; j<8 ; j++ ) {
                boolean bit = ((input[i] >> (7-j)) & 1) == 1;
                assertTrue(bit == bitStream.pop());
            }
        }
    }

    @Test
    void isEmpty() {
        byte[] input = new byte[]{(byte)0b00110011, (byte)0b10101010};
        bitStream  = new BitStream(input);
        assertFalse(bitStream.isEmpty());
    }

    @Test
    void size() {
        byte[] input = new byte[]{(byte)0b00110011, (byte)0b10101010};
        bitStream  = new BitStream(input);
        assertEquals(16, bitStream.size());
    }

    @Test
    void push() {
        bitStream = new BitStream();
        int[] input = new int[]{1,0,1,1,0,1,1};
        for ( int i = 0 ; i<input.length ; i++ ) {
            bitStream.push(input[i] == 1);
        }
        for ( int i = 0 ; i<input.length ; i++ ) {
            assertTrue((input[i] == 1) == bitStream.pop());
        }
    }

    @Test
    void toByteArray() {
        byte[] input = new byte[]{(byte)0b00110011, (byte)0b10101010};
        bitStream  = new BitStream(input);
        byte[] result = bitStream.toByteArray();
        assertArrayEquals(input, result);
    }
}