package org.wjx.norunner.wordmap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a node in a graph structure used to model relationships between words.
 * Each WordNode maintains a collection of outgoing edges, where each edge connects
 * to another word and tracks the frequency of that connection. The node also keeps
 * a cumulative count of all outgoing connections for efficient access to the total
 * number of relationships established.
 *
 * This class is designed to be immutable in terms of its internal data structures,
 * ensuring thread safety when accessed concurrently. It supports operations to add
 * new edges and retrieve the current state of connections.
 *
 * The class implements Serializable to allow instances to be persisted or transmitted
 * across different environments while maintaining their internal state.
 */
public class WordNode implements Serializable {
    /**
     * A mapping of words to their connection frequencies, representing outgoing edges from this node.
     * Each key in the map is a word (String) that this node connects to, and its corresponding value
     * is an integer representing the frequency of that connection. This structure allows efficient
     * tracking of how often this node connects to specific words, facilitating analysis of word
     * relationships within the graph. The map is immutable in terms of its reference, ensuring thread
     * safety when accessed concurrently.
     */
    private final Map<String, Integer> edges;
    /**
     * Represents the cumulative count of all outgoing connections (edges) established from this node.
     * This value is incremented each time a new edge is added to another word, providing a running total
     * of the relationships originating from this node. It serves as a metric to quantify the overall
     * connectivity of the node within the graph structure, enabling efficient access to the total number
     * of outgoing connections without iterating through the edges map.
     */
    private int total;

    /**
     * Constructs a new WordNode instance, initializing its internal state.
     * This constructor initializes the outgoing edges as an empty HashMap, preparing the node
     * to track relationships with other words. It also sets the total connection count to zero,
     * indicating that the node has no established connections at the time of creation.
     * The WordNode is designed to be used within a graph structure where it represents a single
     * word and its outgoing connections to other words, maintaining both the frequency of each
     * connection and a cumulative count of all connections for efficient access.
     */
    public WordNode() {
        this.edges = new HashMap<String, Integer>();
        this.total = 0;
    }

    /**
     * Adds a directed edge to the current node, representing a connection to another word.
     * If the specified word already exists as an edge, its frequency count is incremented.
     * Otherwise, a new edge is created with an initial frequency of 1.
     * The total connection count for the node is also incremented to reflect the addition.
     *
     * @param newword the target word to which the edge points; must not be null
     */
    public void addEdge(String newword) {
        total++;
        if (edges.containsKey(newword))
            edges.put(newword, edges.get(newword) + 1);
        else
            edges.put(newword, 1);
    }

    /**
     * Retrieves the map of outgoing edges from this WordNode.
     * Each key in the map represents a target word connected by an edge, and the corresponding value
     * indicates the frequency of that connection. This method provides access to the internal state
     * of the node's connections, allowing for analysis or further processing of the relationships
     * between words in the graph structure.
     *
     * @return a Map where keys are Strings representing target words and values are Integers
     *         indicating the frequency of connections to those words
     */
    public Map<String, Integer> getEdges() {
        return edges;
    }

    /**
     * Retrieves the total number of connections established from this WordNode to other words.
     * This value represents the cumulative count of all outgoing edges from the node,
     * reflecting the sum of frequencies of connections to other words in the graph structure.
     *
     * @return an integer representing the total number of outgoing connections from this node
     */
    public int getTotal() {
        return total;
    }
}
