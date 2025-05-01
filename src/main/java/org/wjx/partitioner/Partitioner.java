package org.wjx.partitioner;


import org.wjx.norunner.wordmap.WordMap;
import org.wjx.norunner.wordmap.WordNode;

import java.io.Serializable;
import java.util.*;

/**
 * The Partitioner class is responsible for partitioning a set of words into multiple Huffman trees
 * to facilitate efficient encoding and decoding of data. It uses a combination of Huffman coding
 * and probabilistic selection to encode a sequence of words into a bit stream and decode the bit
 * stream back into the original sequence of words.
 *
 * The class initializes a list of Huffman trees based on the input word map and partitions the words
 * into these trees. Each tree is constructed using a subset of the words, and the remaining words
 * are assigned as tail codes to the first tree. This allows for efficient encoding and decoding
 * while maintaining a balance between tree size and code length.
 *
 * The encoding process involves traversing the Huffman trees to determine the most likely next word
 * based on the current word and the bit stream. The decoding process reverses this by reconstructing
 * the bit stream from the sequence of words using the corresponding Huffman trees.
 *
 * The class provides methods for encoding a sequence of words into a bit stream, decoding a bit stream
 * back into a sequence of words, and generating a string representation of the partitioner's state.
 */
public class Partitioner implements Serializable {
    /**
     * An array of Huffman objects, each representing a Huffman coding tree
     * constructed from a set of words and their associated frequencies. The trees
     * are used for encoding and decoding operations within the containing class.
     * Each Huffman object encapsulates a root node, metadata about the tree's size,
     * and a mapping of words to their corresponding nodes in the tree. The array
     * is typically initialized with a predefined size and populated during the
     * construction or operational phases of the containing class.
     */
    Huffman[] huffmanList;
    /**
     * A map that associates words with their corresponding WordNode instances.
     * This structure is used to maintain a collection of words and their related
     * statistical or relational data, encapsulated within WordNode objects.
     * Each key in the map represents a unique word, while the value is a WordNode
     * that contains information about the word's edges and usage frequency.
     * This map serves as a foundational component for operations involving word
     * relationships, such as encoding, decoding, or analyzing word transitions.
     */
    Map<String, WordNode> wordMap;
    /**
     * Represents the size of the Huffman list used in the partitioning process.
     * This value determines the number of elements in the Huffman list, which is essential
     * for encoding and decoding operations. It is initialized during the construction of the
     * Partitioner object and influences the structure of the Huffman tree and the resulting
     * bitstream operations.
     */
    int hlSize;
    /**
     * Represents the size of the Huffman coding structure used within the Partitioner class.
     * This value typically indicates the number of unique symbols or nodes in the Huffman tree
     * that are utilized for encoding and decoding operations. It is derived from the size of the
     * input data provided during the initialization of the Huffman coding process.
     */
    int huffmanSize;

    /**
     * Constructs a Partitioner object that partitions a word graph into multiple Huffman trees
     * for efficient encoding and decoding. The constructor initializes the necessary data structures
     * and divides the words from the word graph into subsets, each of which is used to build a Huffman tree.
     *
     * @param wordMap the WordMap object containing the graph of words and their connections.
     *               The graph is expected to be a map where keys are words and values are WordNode objects.
     * @param hlSize  the number of Huffman trees to create. This determines how the words in the graph
     *               are divided into subsets for constructing the Huffman trees.
     */
    public Partitioner(WordMap wordMap, int hlSize) {
        this.wordMap = wordMap.getGraph();
        this.hlSize = hlSize;
        int totalwords = wordMap.getGraph().size();
        huffmanSize = (totalwords / (hlSize + 1)) - 1;
        huffmanList = new Huffman[hlSize];
        List<String> keyList = new ArrayList<>(wordMap.getGraph().keySet());
        Collections.shuffle(keyList);
        int j = 0;
        for (int i = 0; i < hlSize; i++) {
            Map<String, Integer> map = new HashMap<>();
            for (int k = 0; k < huffmanSize; k++) {
                String key = keyList.get(j + k);
                map.put(key, wordMap.getGraph().get(key).getTotal());
            }
            j += huffmanSize;
            huffmanList[i] = new Huffman(map);
        }
        String[] tailWord = new String[huffmanSize - 1];
        for (int k = 0; k < huffmanSize - 1; k++) {
            String key = keyList.get(j + k);
            tailWord[k] = key;
        }
        huffmanList[0].fillTailCode(tailWord);
    }

    /**
     * Encodes a given BitStream into a list of strings using a partitioned Huffman tree structure.
     * The method iteratively processes the input BitStream, selecting words based on their frequency
     * and encoding them until the entire stream is consumed or no further words can be extracted.
     *
     * @param bitStream the BitStream to be encoded. It represents the binary data to be processed
     *                  and transformed into a sequence of encoded strings.
     * @return a List of Strings representing the encoded output. Each string corresponds to a word
     *         extracted from the BitStream during the encoding process.
     */
    public List<String> encode(BitStream bitStream) {
        String current = null;
        List<String> res = new ArrayList<>();
        while (true) {
            int total = 0;
            Map<String, Integer> nextWord = new HashMap<>();
            Map<String, BitStream> nextBitStream = new HashMap<>();
            WordNode wordNode = current == null ? null : wordMap.get(current);
            for (int i = 0; i < hlSize; i++) {
                String word = huffmanList[i].cutWord(bitStream, false);
                if (word != null) {
                    int freq = wordNode == null ? 1 : wordNode.getEdges().getOrDefault(word, 1);
                    BitStream bs = huffmanList[i].getStringBitStream(word);
                    nextWord.put(word, freq);
                    nextBitStream.put(word, bs);
                    total += freq;
                }
            }
            if(nextWord.isEmpty()){
                res.add(huffmanList[0].cutWord(bitStream, true));
                break;
            } else {
                int random = new Random().nextInt(total);
                int cumulativeWeight = 0;
                for (String word : nextWord.keySet()) {
                    cumulativeWeight += nextWord.get(word);
                    if (random < cumulativeWeight) {
                        current = word;
                        break;
                    }
                }
                res.add(current);
                bitStream.cut(nextBitStream.get(current));
            }
        }
        return res;
    }

    /**
     * Decodes a list of words into a BitStream using a partitioned Huffman tree structure.
     * Each word in the input list is matched against the Huffman trees stored in huffmanList.
     * If a matching Huffman code is found, it is appended to the resulting BitStream.
     * If any word cannot be matched to a Huffman code, the method returns null.
     *
     * @param words a list of strings representing the words to be decoded. Each word is expected
     *              to have a corresponding Huffman code in the huffmanList.
     * @return a BitStream object containing the concatenated binary representation of the input words,
     *         or null if any word in the list cannot be matched to a valid Huffman code.
     */
    public BitStream decode(List<String> words) {
        BitStream bitStream = new BitStream();
        for (String word : words) {
            boolean find = false;
            for (int i = 0; i < hlSize; i++) {
                if (huffmanList[i].getStringBitStream(word) != null) {
                    bitStream.append(huffmanList[i].getStringBitStream(word));
                    find = true;
                    break;
                }
            }
            if (!find)
                return null;
        }
        return bitStream;
    }

    @Override
    public String toString() {
        return "Partitioner { HuffmanTreeSize=" + huffmanSize + ", HuffmanListNumber=" + hlSize + " }";
    }
}
