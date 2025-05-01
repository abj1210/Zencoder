package org.wjx.norunner.wordmap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a mapping of words as nodes in a graph structure, where connections between words are stored as edges.
 * Each word is associated with a WordNode that tracks its outgoing connections to other words.
 * This class provides functionality to add edges between words and retrieve the underlying graph structure.
 * The class is serializable, allowing instances to be saved or transmitted.
 */
public class WordMap implements Serializable {
    /**
     * A mapping of words to their corresponding WordNode objects, representing a graph structure.
     * Each key in the map is a word, and its value is a WordNode that stores outgoing connections
     * to other words as edges. This structure facilitates the tracking of relationships between words,
     * where each WordNode maintains a count of how often it connects to other specific words.
     * The graph is used internally to manage and retrieve word associations efficiently.
     */
    Map<String, WordNode> graph;
    /**
     * Tracks the total number of connections (edges) established between words in the graph.
     * This counter is incremented each time a new edge is added between two words, providing
     * a cumulative count of all connections within the WordMap instance. It serves as a metric
     * to quantify the overall connectivity of the graph structure.
     */
    int conncnt;

    /**
     * Constructs a new WordMap instance, initializing the internal graph structure and connection counter.
     * The graph is represented as a HashMap where each key is a word, and its value is a WordNode object
     * that tracks outgoing connections to other words. The connection counter is initialized to zero,
     * representing an empty graph with no established edges. This constructor prepares the WordMap for
     * subsequent operations such as adding edges and retrieving the graph structure.
     */
    public WordMap() {
        graph = new HashMap<>();
        conncnt = 0;
    }

    /**
     * Adds a directed edge between two words in the graph, representing a sequential relationship.
     * If either word does not already exist in the graph, a new node is created for it.
     * The connection count is incremented to reflect the addition of the edge.
     *
     * @param w1 the source word from which the edge originates; must not be null
     * @param w2 the target word to which the edge points; must not be null
     */
    public void addEdge(String w1, String w2) {
        if (!graph.containsKey(w1)) {
            WordNode newnode = new WordNode();
            graph.put(w1, newnode);
        }
        if (!graph.containsKey(w2)) {
            WordNode newnode = new WordNode();
            graph.put(w2, newnode);
        }
        graph.get(w1).addEdge(w2);
        conncnt++;
    }

    /**
     * Retrieves the internal graph structure representing word connections.
     * The graph is stored as a map where each key is a word, and its corresponding value
     * is a WordNode object that tracks outgoing connections to other words.
     * This method provides read-only access to the graph for further processing or analysis.
     *
     * @return a Map containing the graph structure, where keys are words (String) and values
     *         are WordNode objects representing the nodes in the graph.
     */
    public Map<String, WordNode> getGraph() {
        return graph;
    }

    @Override
    public String toString() {
        return "WordMap{ nodes = " + graph.size() + " connections = " + conncnt + " }";
    }
}
