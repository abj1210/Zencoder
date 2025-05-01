package org.wjx.partitioner;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HuffmanTest {

    @Test
    void getStringBitStream() {
        Map<String, Integer> map  = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);
        map.put("E", 5);
        map.put("F", 6);
        map.put("G", 7);
        Huffman huffman = new Huffman(map);
        BitStream result = huffman.getStringBitStream("D");
        assertEquals("110", result.toString());
    }

    @Test
    void cutWord() {
        Map<String, Integer> map  = new HashMap<>();
        map.put("A", 1);
        map.put("B", 2);
        map.put("C", 3);
        map.put("D", 4);
        map.put("E", 5);
        map.put("F", 6);
        map.put("G", 7);
        Huffman huffman = new Huffman(map);
        BitStream bitStream = new BitStream();
        bitStream.push(true);
        bitStream.push(true);
        bitStream.push(false);

        assertEquals("D", huffman.cutWord(bitStream, false));
    }
}