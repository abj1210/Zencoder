package org.wjx;

import org.wjx.norunner.NextOneGenerator;
import org.wjx.partitioner.Partitioner;
import org.wjx.norunner.wordmap.WordMap;

import java.io.*;


/**
 * A utility class for generating, loading, and saving Partitioner instances.
 * This class provides static methods to create a Partitioner from a dataset,
 * load a serialized Partitioner from a file, and save a Partitioner instance to a file.
 *
 * The `runGenerator` method generates a Partitioner by processing a dataset and applying
 * a Huffman coding strategy based on the provided Huffman number.
 *
 * The `loadPartitioner` method deserializes a Partitioner object from a specified file,
 * allowing for reuse of previously generated Partitioner instances.
 *
 * The `savePartitioner` method serializes a given Partitioner object to a file,
 * enabling persistence of the Partitioner for later use.
 *
 * This class is designed to facilitate the creation and management of Partitioner objects,
 * which are used for encoding and decoding data using a combination of Huffman coding
 * and word mapping techniques.
 */
public class PartitionerGenerator {
    /**
     * Generates a Partitioner by processing the given dataset and applying a Huffman coding strategy
     * based on the provided Huffman number.
     *
     * This method first generates a WordMap by invoking the NextOneGenerator.runGenerator method
     * with the specified dataset. If the generated WordMap is null, the method returns null.
     * Otherwise, it constructs and returns a new Partitioner instance using the generated WordMap
     * and the specified Huffman number.
     *
     * @param dataset the path to the dataset used to generate the WordMap; this should be a valid
     *                directory or file path containing the input data
     * @param huffmanNumber the number used to configure the Huffman coding strategy within the
     *                      Partitioner; this determines the size of the Huffman list
     * @return a new Partitioner instance if the WordMap generation is successful, or null if the
     *         WordMap generation fails
     */
    public static Partitioner runGenerator(String dataset, int huffmanNumber) {
        WordMap map = NextOneGenerator.runGenerator(dataset);
        if(map == null)
            return null;
        return new Partitioner(map, huffmanNumber);
    }

    /**
     * Loads a serialized Partitioner object from the specified file.
     *
     * This method attempts to deserialize a Partitioner instance from the given file.
     * If the file is not found, cannot be read, or does not contain a valid serialized
     * Partitioner object, an error message will be printed to the standard error stream,
     * and null will be returned.
     *
     * @param filename the path to the file containing the serialized Partitioner object
     * @return the deserialized Partitioner object, or null if deserialization fails
     */
    public static Partitioner loadPartitioner(String filename) {
        Partitioner partitioner = null;
        try (FileInputStream fileIn = new FileInputStream(filename);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            partitioner = (Partitioner) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return partitioner;
    }

    /**
     * Serializes and saves a given Partitioner object to a file.
     *
     * This method uses Java's ObjectOutputStream to serialize the provided Partitioner
     * instance and writes it to the specified file. If an I/O exception occurs during
     * the serialization process, the exception is printed to the standard error stream.
     *
     * @param partitioner the Partitioner object to be serialized and saved; must not be null
     * @param filename the path to the file where the serialized Partitioner will be stored
     */
    public static void savePartitioner(Partitioner partitioner, String filename) {
        try (FileOutputStream fileOut = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(partitioner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
