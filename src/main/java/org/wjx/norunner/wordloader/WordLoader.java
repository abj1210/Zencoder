package org.wjx.norunner.wordloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class for loading and retrieving individual words from multiple UTF-8 encoded text files.
 * This class processes lines from the provided files, extracting words that belong to the Han script
 * (commonly used in Chinese, Japanese, and Korean writing systems). It supports sequential reading
 * across multiple files, ensuring continuous word retrieval until all files are exhausted.
 *
 * The class initializes with an array of file paths, opening the first file for reading. When the end
 * of a file is reached, it automatically transitions to the next file in the sequence. Words are extracted
 * line by line, filtering out non-Han characters to ensure only relevant content is returned.
 *
 * The primary method, getNextWord, provides access to individual words in sequence. If no words are
 * available in the current line or file, the method handles transitioning to the next line or file
 * internally. When all files have been processed and no more words remain, an IOException is thrown
 * to indicate the end of available content.
 *
 * This class relies on the UTF8Reader utility for efficient line-by-line reading of UTF-8 encoded files.
 * It is designed for scenarios where selective extraction of specific character sets from large text
 * files is required, such as linguistic analysis or data preprocessing tasks.
 *
 * Exceptions related to file access, unsupported encodings, or unexpected I/O errors are propagated
 * to the caller for handling. Proper error handling is essential when using this class to manage
 * resources and respond to potential issues during file processing.
 */
public class WordLoader {
    /**
     * A list of words extracted from a line of text, filtered to include only those
     * that consist of Han script characters (Chinese characters). This list is populated
     * by the `getLineWords` method, which processes a line of text retrieved from a UTF-8
     * encoded file. Each word in the list corresponds to a single Han character extracted
     * from the line.
     *
     * The list is initialized as null and is updated whenever `getLineWords` is called.
     * If the end of the file is reached or no Han characters are found in the line,
     * the list may remain null or become empty, depending on the content of the line.
     *
     * This field is used internally to store intermediate results during the processing
     * of text files and is accessed by methods that require sequential access to the
     * extracted words, such as retrieving the next word.
     */
    List<String> words = null;
    /**
     * An array of Path objects representing the file paths from which words are loaded.
     * Each path corresponds to a UTF-8 encoded text file that is processed sequentially.
     * The files are read line by line, and words are extracted based on specific criteria.
     *
     * This field is immutable and initialized during the construction of the containing class.
     * It is used internally to manage the sequence of files being processed and ensures
     * that all specified files are accessible before processing begins.
     *
     * If any of the paths point to non-existent or inaccessible files, an exception is thrown
     * during the initialization of the containing class.
     */
    private final Path[] paths;
    /**
     * Represents the current index position within the list of words extracted from a line of text.
     * This variable is used to track the progress of word retrieval, ensuring sequential access
     * to individual words in the list. It is updated as words are consumed and serves as an
     * indicator of the next word to be retrieved. If the index exceeds or matches the size of
     * the word list, it signifies that all words have been processed.
     *
     * The value of this variable is incremented during calls to retrieve the next word, and it
     * is reset or adjusted when new lines of text are processed and new words are loaded.
     *
     * This field is primarily utilized in conjunction with the `words` list to manage word-by-word
     * iteration in a controlled and predictable manner.
     */
    private int idx;
    /**
     * A UTF8Reader instance used to sequentially read text files encoded in UTF-8 line by line.
     * This reader is responsible for efficiently retrieving file content as strings, one line at a time.
     * It is initialized with a file path provided during the construction of the containing class
     * and is utilized to process the file's content for specific operations, such as extracting words.
     *
     * The reader ensures proper decoding of UTF-8 encoded content and leverages buffered reading
     * to minimize I/O overhead. If an I/O exception occurs during file reading, it is propagated
     * to the caller for handling.
     *
     * This field is initialized once and remains immutable throughout the lifecycle of the containing class.
     */
    private UTF8Reader ur;

    /**
     * Constructs a new WordLoader instance to process an array of text files encoded in UTF-8.
     * This constructor initializes the loader with the provided file paths and prepares it
     * to read words sequentially from the files. The first file in the array is opened for reading
     * using a UTF8Reader, which ensures proper decoding of UTF-8 content. Subsequent files are
     * processed as needed during word extraction.
     *
     * If the provided array of paths is empty or null, the behavior of this constructor is undefined.
     * If the first file in the array does not exist or cannot be accessed, a FileNotFoundException
     * is thrown. If the file's encoding is not supported, an UnsupportedEncodingException is thrown.
     *
     * @param paths an array of Path objects representing the file paths to be processed; the array
     *              must contain at least one valid path to a readable text file encoded in UTF-8
     * @throws FileNotFoundException if the first file specified by the path does not exist or cannot be opened
     * @throws UnsupportedEncodingException if the UTF-8 encoding is not supported (this is unlikely in modern environments)
     */
    public WordLoader(Path[] paths) throws FileNotFoundException, UnsupportedEncodingException {
        this.paths = paths;
        ur = new UTF8Reader(paths[0]);
        idx = 1;
    }

    /**
     * Reads the next line from the current file being processed and extracts words consisting
     * exclusively of Han script characters (Chinese characters). The method retrieves a line
     * using the UTF8Reader instance, processes it to filter out non-Han characters, and stores
     * the resulting words in the internal list. If the end of the file is reached and no more
     * lines are available, an IOException is thrown.
     *
     * The method uses Unicode code points to iterate through each character in the line,
     * converting them to strings for further processing. It filters characters based on their
     * Unicode script property, retaining only those classified as Han script. The filtered
     * characters are then collected into a list of words.
     *
     * This method is designed to be invoked repeatedly to process multiple lines until all
     * relevant words are extracted. It assumes that the UTF8Reader instance is properly
     * initialized and that the file contains valid UTF-8 encoded text.
     *
     * @throws IOException if an I/O error occurs while reading the file or if the end of the
     *                     file is reached unexpectedly
     */
    private void getLineWords() throws IOException {
        String line = ur.getNextLine();
        if (line == null)
            throw new IOException("EOF");
        words = line.codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .filter(s -> {
                    int codePoint = s.codePointAt(0);
                    return Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN;
                })
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the next word consisting exclusively of Han script characters (Chinese characters)
     * from the sequence of text files being processed. This method ensures a continuous supply
     * of words by transitioning between files when necessary.
     *
     * The method operates on an internal list of words extracted from the current file. If the
     * list is empty or null, it invokes the `getLineWords` method to populate the list with words
     * from the next available line. If the end of the current file is reached and no more lines
     * are available, the method switches to the next file in the sequence, reinitializing the
     * reader and repeating the process.
     *
     * If all files have been exhausted and no more words are available, an IOException is thrown
     * with the message "Read over."
     *
     * This method assumes that the internal state of the class, including the list of words and
     * the file reader, is properly initialized and maintained throughout the processing lifecycle.
     *
     * @return the next word consisting exclusively of Han script characters from the current or
     *         subsequent file in the sequence
     * @throws IOException if an I/O error occurs during file reading or if no more words are
     *                     available across all files
     */
    public String getNextWord() throws IOException {
        try {
            while (words == null || words.isEmpty())
                getLineWords();
            String word = words.get(0);
            words.remove(0);
            return word;
        } catch (IOException e) {
            if (idx < paths.length) {
                ur = new UTF8Reader(paths[idx]);
                idx++;
                while (words == null || words.isEmpty())
                    getLineWords();
                String word = words.get(0);
                words.remove(0);
                return word;
            } else
                throw new IOException("Read over.");
        }
    }
}
