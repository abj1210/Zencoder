package org.wjx.partitioner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Represents a stream of bits stored as a sequence of boolean values.
 * Provides methods for manipulating the bit stream, including pushing, popping,
 * appending, and converting to and from byte arrays.
 *
 * The class supports operations such as retrieving the first bit, checking if the stream is empty,
 * determining the size of the stream, and cutting overlapping prefixes with another bit stream.
 * Additionally, it allows recovery of bits at the front and provides a string representation of the bit sequence.
 *
 * BitStream can be initialized either as an empty stream or from a byte array, where each byte is expanded into
 * its individual bits in big-endian order.
 *
 * The cut operation removes matching prefixes between two bit streams until a mismatch is found or one of the streams
 * becomes empty. The append operation transfers all bits from another bit stream to the end of this stream.
 *
 * Note: The internal storage of the bit stream is implemented using a deque of boolean values.
 */
public class BitStream {
    /**
     * A deque representing a sequence of boolean values that correspond to individual bits in a bit stream.
     * The deque supports efficient addition and removal of bits from both ends, enabling operations such as
     * pushing bits to the end, popping bits from the front, and recovering bits at the beginning.
     * Each boolean value in the deque represents a single bit, where true corresponds to 1 and false corresponds to 0.
     * This structure is used internally to store and manipulate the bit stream, allowing for dynamic resizing and
     * flexible manipulation of the bit sequence.
     */
    Deque<Boolean> data;

    /**
     * Constructs a new BitStream instance with an empty internal data structure.
     * The internal storage is initialized as an empty deque, ready for bit-level operations.
     * This constructor is used when creating a BitStream without initial data.
     */
    public BitStream() {
        data = new ArrayDeque<>();
    }

    /**
     * Constructs a new BitStream instance from the given byte array.
     * Each byte in the input array is processed bit by bit, starting from the most significant bit (MSB)
     * to the least significant bit (LSB). The bits are stored as individual boolean values in an internal
     * deque structure, where `true` represents a bit value of 1 and `false` represents a bit value of 0.
     *
     * @param data the byte array to initialize the BitStream with; each byte is expanded into 8 boolean values
     */
    public BitStream(byte[] data) {
        this.data = new LinkedList<Boolean>();
        for (byte b : data) {
            for (int i = 0; i < 8; i++) {
                this.data.offerLast(((b >> (7 - i)) & 1) == 1);
            }
        }
    }

    /**
     * Retrieves the front element of the internal data structure without removing it.
     * This method provides access to the first boolean value stored in the underlying deque,
     * which represents the most significant bit currently present in the BitStream.
     *
     * @return the boolean value at the front of the internal data structure, or false if the structure is empty
     */
    public boolean front() {return data.peekFirst();}

    /**
     * Removes and returns the front element of the internal data structure.
     * This method retrieves the first boolean value stored in the underlying deque,
     * which represents the most significant bit currently present in the BitStream,
     * and removes it from the structure. If the internal data structure is empty,
     * the behavior depends on the implementation of the underlying deque's pollFirst method.
     *
     * @return the boolean value that was at the front of the internal data structure,
     *         or false if the structure is empty
     */
    public boolean pop() {return data.pollFirst();}

    /**
     * Checks if the internal data structure of this BitStream is empty.
     * This method determines whether there are any boolean values stored in the underlying deque.
     *
     * @return true if the internal data structure contains no elements, false otherwise
     */
    public boolean isEmpty() {return this.data.isEmpty();}

    /**
     * Returns the number of elements in the internal data structure of this BitStream.
     * The size represents the total count of boolean values stored in the underlying deque,
     * where each boolean corresponds to a single bit in the stream.
     *
     * @return the number of boolean values currently stored in the internal data structure
     */
    public int size() {return this.data.size();}

    /**
     * Adds a boolean value to the end of the internal data structure.
     * This method appends the specified boolean value to the tail of the underlying deque,
     * representing the addition of a single bit to the BitStream. The value `true` corresponds
     * to a bit value of 1, while `false` corresponds to a bit value of 0.
     *
     * @param b the boolean value to be added to the internal data structure
     */
    public void push(boolean b) {data.offerLast(b);}

    /**
     * Recovers the BitStream by adding a boolean value to the front of the internal data structure.
     * This method inserts the specified boolean value at the beginning of the underlying deque,
     * effectively prepending a single bit to the BitStream. The value `true` represents a bit value
     * of 1, while `false` represents a bit value of 0.
     *
     * @param b the boolean value to be added to the front of the internal data structure
     */
    public void recover(boolean b) {data.offerFirst(b);}

    /**
     * Converts the internal bit-level data structure into a byte array representation.
     * The method processes the bits stored in the internal data structure in chunks of 8,
     * packing each group of 8 bits into a single byte. The most significant bit (MSB) of
     * each byte is derived from the first bit of the corresponding 8-bit group, and the
     * least significant bit (LSB) is derived from the last bit of the group. The internal
     * data structure is consumed during this process, and its size is reduced accordingly.
     *
     * @return a byte array containing the packed representation of the internal bit stream,
     *         where each byte corresponds to an 8-bit segment of the original data
     */
    public byte[] toByteArray() {
        byte[] result = new byte[data.size() / 8];
        for (int i = 0; i < result.length; i++) {
            byte r = 0;
            for (int j = 0; j < 8; j++) {
                r = (byte) ((r << 1) | (data.pollFirst() ? 1 : 0));
            }
            result[i] = r;
        }
        return result;
    }

    /**
     * Removes matching leading bits from both this BitStream and the provided BitStream.
     * This method iteratively compares the front elements of the two BitStreams. If the front elements
     * of both streams are equal, they are removed from their respective streams. The process continues
     * until either one of the streams becomes empty or the front elements no longer match.
     *
     * @param bs the BitStream to compare and modify; must not be null
     */
    public void cut(BitStream bs) {
        while ((!bs.isEmpty()) && (!this.isEmpty())) {
            if ((bs.front()) == (this.front())) {
                this.pop();
                bs.pop();
            }
        }
    }

    /**
     * Appends the contents of the specified BitStream to the end of this BitStream.
     * This method iteratively removes the front element from the provided BitStream
     * and adds it to the end of this BitStream until the provided BitStream becomes empty.
     * The operation effectively transfers all bits from the given BitStream to this one,
     * leaving the provided BitStream empty as a result.
     *
     * @param bs the BitStream whose contents are to be appended; must not be null
     */
    public void append(BitStream bs) {
        while (!bs.isEmpty()) {
            this.push(bs.pop());
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (boolean b : data) {
            str.append(b ? "1" : "0");
        }
        return str.toString();
    }
}

