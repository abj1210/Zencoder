package org.wjx.norunner.wordloader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * A utility class for reading text files encoded in UTF-8 line by line.
 * This class provides a simple interface to open a file and retrieve its content
 * one line at a time. It uses a buffered reader internally to ensure efficient reading.
 *
 * The constructor accepts a file path and initializes the reader with UTF-8 encoding.
 * If the file does not exist or cannot be read, an exception is thrown during initialization.
 *
 * The primary method, getNextLine, retrieves the next available line from the file.
 * When the end of the file is reached, it returns null. Any IOException encountered
 * during reading is propagated to the caller for handling.
 *
 * This class is designed to be used in scenarios where sequential access to file content
 * is required, such as processing large text files or extracting specific data patterns.
 */
public class UTF8Reader {
    /**
     * A buffered reader used for reading text files encoded in UTF-8 line by line.
     * This reader is initialized with an input stream reader that ensures proper decoding
     * of UTF-8 encoded content. It provides efficient reading capabilities by buffering
     * the input data, minimizing the number of I/O operations required to retrieve lines.
     *
     * The buffered reader is utilized internally by methods that process file content,
     * such as retrieving individual lines or extracting specific patterns from the text.
     * If an I/O exception occurs during reading, it is propagated to the caller for handling.
     *
     * This field is immutable and initialized during the construction of the containing class.
     */
    private final BufferedReader br;

    /**
     * Constructs a new UTF8Reader instance to read text files encoded in UTF-8 line by line.
     * This constructor initializes the reader with a buffered input stream, ensuring efficient
     * reading of file content. The provided file path is used to locate and open the file for reading.
     *
     * If the specified file does not exist or cannot be accessed, a FileNotFoundException is thrown.
     * If the file's encoding is not supported, an UnsupportedEncodingException is thrown.
     *
     * @param filename the path to the file to be read, represented as a Path object
     * @throws FileNotFoundException if the file specified by the path does not exist or cannot be opened
     * @throws UnsupportedEncodingException if the UTF-8 encoding is not supported (this is unlikely in modern environments)
     */
    public UTF8Reader(Path filename) throws FileNotFoundException, UnsupportedEncodingException {
        FileInputStream fis = new FileInputStream(filename.toString());
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        br = new BufferedReader(isr);
    }

    /**
     * Retrieves the next line from the UTF-8 encoded text file being read.
     * This method reads a line of text from the file, returning it as a string.
     * Lines are considered to be terminated by any one of a line feed ('\n'),
     * a carriage return ('\r'), or a carriage return followed immediately by a line feed ("\r\n").
     *
     * If the end of the file is reached and no more lines are available, this method returns null.
     * Any IOException that occurs during the reading process is propagated to the caller for handling.
     *
     * @return the next line of text from the file, or null if the end of the file has been reached
     * @throws IOException if an I/O error occurs while reading from the file
     */
    public String getNextLine() throws IOException {
        return br.readLine();
    }
}
