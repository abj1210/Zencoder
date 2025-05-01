package org.wjx.norunner;

import org.wjx.norunner.wordloader.WordLoader;
import org.wjx.norunner.wordmap.WordMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * The NextOneGenerator class is responsible for generating a WordMap by processing text files
 * located in a specified directory. It utilizes the WordLoader class to extract words from the
 * files and constructs a WordMap where edges represent sequential word relationships.
 *
 * The runGenerator method traverses the given directory, identifies regular files, and processes
 * them to build a WordMap. Words are read sequentially, and edges are added to the WordMap based
 * on their order of appearance. The process continues until all words from the files are processed.
 *
 * If an exception occurs during file traversal or word processing, it is caught and logged. The
 * generated WordMap is returned if successful; otherwise, null is returned.
 *
 * This class assumes that the input files contain valid UTF-8 encoded text and that the words
 * extracted are filtered based on specific criteria (e.g., Unicode script).
 */
public class NextOneGenerator {
    /**
     * Traverses the specified directory, processes all regular files within it, and generates a WordMap
     * based on the sequential relationships between words extracted from the files.
     *
     * The method uses a WordLoader to extract words from the files, filtering them based on specific
     * criteria (e.g., Unicode script). It then constructs a WordMap where edges represent sequential
     * word relationships. The process continues until all words from the files are processed or an
     * exception occurs.
     *
     * If an exception occurs during file traversal or word processing, it is caught and logged. The
     * generated WordMap is returned if successful; otherwise, null is returned.
     *
     * @param path the path to the directory containing the input files; this should be a valid directory
     *             path with readable text files encoded in UTF-8
     * @return a WordMap instance representing the sequential relationships between words if the generation
     *         is successful, or null if an error occurs during processing
     */
    public static WordMap runGenerator(String path) {
        WordLoader wordLoader;
        WordMap map = new WordMap();
        try {
            Stream<Path> stream = Files.walk(Paths.get(path));
            Path[] filePaths = stream.filter(Files::isRegularFile)
                    .toArray(Path[]::new);
            wordLoader = new WordLoader(filePaths);
            try {
                String lastWord = wordLoader.getNextWord();
                while (true) {
                    String nextWord = wordLoader.getNextWord();
                    map.addEdge(lastWord, nextWord);
                    lastWord = nextWord;
                }
            } catch (IOException e) {
                System.out.println("Read over");
            }
            System.out.println(map + " generated.");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
