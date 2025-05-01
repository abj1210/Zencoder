package org.wjx.partitioner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * Implements Huffman coding for data compression. This class constructs a Huffman tree
 * based on the frequency of input words and provides methods to encode and decode words
 * using the generated tree.
 *
 * The Huffman tree is built using a priority queue, where nodes with lower frequencies
 * are prioritized. Each leaf node represents a word, and internal nodes represent combined
 * frequencies of their children. The tree structure enables efficient encoding and decoding
 * of words into bit streams.
 *
 * The class supports tail code assignment for internal nodes, which can be used when the
 * bit stream ends prematurely during decoding. This feature requires the tree to be fully
 * initialized with tail codes.
 *
 * The `getStringBitStream` method encodes a given word into a bit stream by traversing the
 * tree from the leaf node to the root. The `cutWord` method decodes a bit stream back into
 * a word by traversing the tree from the root to a leaf node. If tail code support is
 * enabled and the bit stream ends prematurely, the method returns the word associated with
 * the current node.
 *
 * The nested `Node` class represents individual nodes in the Huffman tree. Each node stores
 * a word, its frequency, and references to its parent and child nodes. Nodes are comparable
 * based on their frequency, which is used to maintain the priority queue during tree construction.
 *
 * This class implements the Serializable interface, allowing the Huffman tree to be serialized
 * and deserialized for storage or transmission.
 */
public class Huffman implements Serializable {
    /**
     * Represents the root node of a Huffman tree. This node serves as the starting point for traversing
     * the tree and is essential for encoding and decoding operations. The root node contains references
     * to its left and right child nodes, forming the hierarchical structure of the tree. It may also
     * store frequency information and a word (if applicable), which are used in the construction and
     * comparison of nodes during the Huffman coding process.
     */
    Node root;
    /**
     * Represents the size of the data structure or collection managed by the Huffman class.
     * This variable tracks the number of elements or nodes in the structure, providing a measure
     * of its current capacity or extent. It is used internally to manage operations such as
     * encoding, decoding, or traversing the structure efficiently.
     */
    int size;
    /**
     * Indicates whether the Huffman coding process has completed and the full code representation is available.
     * When set to true, it signifies that the encoding or decoding process has finished successfully
     * and the resulting bitstream contains the complete encoded data.
     * If false, it implies that the process is still ongoing or incomplete.
     */
    boolean fullCode;
    /**
     * A map that associates string representations of words with their corresponding Huffman nodes.
     * This map is used to store the initial set of nodes before constructing the Huffman tree.
     * Each key in the map represents a unique word, and the associated value is a Node object
     * containing information about the word's frequency and its position within the tree structure.
     * The nodes stored in this map serve as the foundation for building the Huffman encoding tree,
     * where each node may eventually become a leaf node in the final tree structure.
     */
    Map<String, Node> nodeList;

    /**
     * Constructs a Huffman tree based on the frequency of words provided in the input map.
     * The method initializes the priority queue with nodes representing each word and its frequency.
     * It then builds the Huffman tree by repeatedly combining the two nodes with the lowest frequencies
     * until only one node (the root) remains. The resulting tree is used for encoding and decoding.
     *
     * @param words a map where keys are words and values are their corresponding frequencies
     */
    Huffman(Map<String, Integer> words) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        nodeList = new HashMap<>();
        size = words.size();
        for (String word : words.keySet()) {
            int freq = words.get(word);
            Node node = new Node(word, freq);
            priorityQueue.add(node);
            nodeList.put(word, node);
        }
        while (priorityQueue.size() > 1) {
            Node left = priorityQueue.poll();
            Node right = priorityQueue.poll();
            Node newNode = new Node("\0", left.freq + right.freq);
            newNode.addLeftSon(left);
            newNode.addRightSon(right);
            priorityQueue.offer(newNode);
        }
        root = priorityQueue.poll();
        fullCode = false;
    }

    /**
     * Fills the tail codes for nodes in the Huffman tree that have both left and right children.
     * This method traverses the tree using a depth-first approach, assigning words from the input array
     * to eligible nodes. The traversal ensures that only nodes with both children are assigned a word.
     * Once all eligible nodes are processed, the fullCode flag is set to true, indicating that the
     * tail codes have been successfully assigned.
     *
     * @param words an array of strings representing the words to be assigned as tail codes to eligible nodes
     */
    public void fillTailCode(String[] words) {
        Stack<Node> stack = new Stack<>();
        stack.push(root);
        int i = 0;
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node.left != null && node.right != null) {
                node.word = words[i];
                nodeList.put(node.word, node);
                i++;
            }
            if (node.left != null)
                stack.push(node.left);
            if (node.right != null)
                stack.push(node.right);
        }
        fullCode = true;
    }

    /**
     * Converts the given word into its corresponding BitStream representation based on the Huffman tree.
     * The method traverses the tree from the node associated with the word back to the root, encoding
     * the path as a sequence of bits (false for left edges, true for right edges). The resulting bit
     * sequence is then reversed and returned as a BitStream object.
     *
     * @param word the input word to be converted into a BitStream; must exist in the nodeList
     * @return a BitStream object representing the encoded path of the word in the Huffman tree,
     *         or null if the word does not exist in the nodeList
     */
    public BitStream getStringBitStream(String word) {
        if (nodeList.containsKey(word)) {
            Node current = nodeList.get(word);
            Stack<Boolean> stk = new Stack<>();
            while (current != root) {
                if (current.parent.left == current) {
                    stk.push(false);
                }
                if (current.parent.right == current) {
                    stk.push(true);
                }
                current = current.parent;
            }
            BitStream bitStream = new BitStream();
            while (!stk.isEmpty()) {
                bitStream.push(stk.pop());
            }
            return bitStream;
        } else return null;
    }

    /**
     * Decodes a word from the given BitStream by traversing the Huffman tree.
     * The method starts at the root of the tree and navigates through the tree based on the bits
     * popped from the BitStream. If the BitStream is exhausted before reaching a leaf node, the
     * method recovers the bits and optionally returns the current node's word if tailCode is true
     * and the tree has fullCode enabled. If no valid word is found, null is returned.
     *
     * @param bitStream the BitStream containing the encoded bits to decode; must not be null
     * @param tailCode  a boolean flag indicating whether to return the current node's word if the
     *                  BitStream is exhausted and the tree has fullCode enabled
     * @return the decoded word if a valid leaf node is reached, or null if decoding fails
     */
    public String cutWord(BitStream bitStream, boolean tailCode) {
        Node current = root;
        Stack<Boolean> stack = new Stack<>();
        while (current.left != null && current.right != null) {
            if (bitStream.isEmpty()) {
                while (!stack.isEmpty()) {
                    bitStream.recover(stack.pop());
                }
                if (tailCode && fullCode)
                    return current.word;
                else return null;
            }
            boolean b = bitStream.pop();
            stack.push(b);
            if (b) {
                current = current.right;
            } else {
                current = current.left;
            }
        }
        while (!stack.isEmpty()) {
            bitStream.recover(stack.pop());
        }
        return current.word;
    }

    /**
     * Represents a node in a Huffman tree. Each node contains a word, its frequency,
     * and references to its parent and child nodes (left and right). The class implements
     * the Comparable interface to allow comparison based on frequency and is serializable
     * to support persistence or transmission of the tree structure.
     *
     * The compareTo method defines the natural ordering of nodes based on their frequency,
     * enabling their use in priority queues or sorted collections. Nodes are compared by
     * subtracting the frequency of the current node from the frequency of another node.
     *
     * Nodes can be linked together to form a binary tree structure using the addLeftSon
     * and addRightSon methods, which establish parent-child relationships between nodes.
     */
    static class Node implements Comparable<Node>, Serializable {
        /**
         * Represents a word associated with a node in a Huffman tree. This field stores the actual
         * string value of the word, which is used as part of the data structure for encoding and
         * decoding operations in Huffman coding. The word is immutable once assigned to the node
         * and serves as a key identifier for the node within the tree.
         */
        String word;
        /**
         * Represents the frequency of the word associated with a node in a Huffman tree.
         * The frequency is used to determine the priority of the node during the construction
         * of the tree and influences the encoding process. Nodes with lower frequencies are
         * prioritized higher, as the compareTo method defines the natural ordering based on
         * this field. This value is immutable once assigned to the node and plays a critical
         * role in the efficiency of the Huffman coding algorithm.
         */
        int freq;
        /**
         * Represents the parent node of the current node in a Huffman tree structure.
         * This field establishes a reference to the immediate ancestor of the node within
         * the binary tree hierarchy. It is used to navigate upward in the tree and maintain
         * the parent-child relationships between nodes. The parent reference is initialized
         * to null when a node is created and is updated when the node is added as a child
         * (left or right) to another node using the addLeftSon or addRightSon methods.
         */
        Node parent;
        /**
         * Represents the left child node in a binary tree structure.
         * This variable holds a reference to another Node object, which is the immediate
         * left descendant of the current node. If there is no left child, this value may be null.
         * The left child is typically used in algorithms involving tree traversal, such as
         * in-order, pre-order, or post-order traversal, as well as in operations like insertion
         * or searching within the tree.
         */
        Node left, right;

        /**
         * Constructs a new Node with the specified word and frequency.
         * Initializes the left, right, and parent pointers to null.
         *
         * @param word the word associated with this node
         * @param freq the frequency of the word associated with this node
         */
        public Node(String word, int freq) {
            this.word = word;
            this.freq = freq;
            this.left = null;
            this.right = null;
            this.parent = null;
        }

        /**
         * Adds a left child node to the current node.
         * The method sets the left pointer of this node to the specified node and updates
         * the parent pointer of the specified node to reference this node.
         *
         * @param n the node to be added as the left child of this node
         */
        public void addLeftSon(Node n) {
            left = n;
            n.parent = this;
        }

        /**
         * Adds a right child node to the current node.
         * The method sets the right pointer of this node to the specified node and updates
         * the parent pointer of the specified node to reference this node.
         *
         * @param n the node to be added as the right child of this node
         */
        public void addRightSon(Node n) {
            right = n;
            n.parent = this;
        }

        @Override
        public int compareTo(Node o) {
            return freq - o.freq;
        }
    }
}
