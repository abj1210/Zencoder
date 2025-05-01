package org.wjx;

import org.wjx.partitioner.BitStream;
import org.wjx.partitioner.Partitioner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Zencoder class provides functionality for encryption and decryption of data
 * using AES algorithms, as well as encoding and decoding through a custom partitioning mechanism.
 * It integrates with a Partitioner to encode and decode data into a sequence of words,
 * which can be used for obfuscation or compression purposes.
 *
 * The class supports both AES-based encryption/decryption and non-AES encoding/decoding.
 * AES operations require a SecretKey and an initialization vector (IV) for security.
 * Non-AES methods rely solely on the Partitioner for encoding and decoding bit streams.
 *
 * The generateKey method facilitates the creation of AES keys with specified key sizes.
 * The encrypt and decrypt methods handle AES encryption and decryption, respectively,
 * while also leveraging the Partitioner for additional encoding or decoding.
 *
 * The encryptWithoutAES and decryptWithoutAES methods provide encoding and decoding
 * capabilities without involving AES encryption, relying only on the Partitioner's logic.
 *
 * This class is designed for scenarios where data needs to be securely encrypted or
 * transformed into a different representation for storage or transmission.
 */
public class Zencoder {
    /**
     * The partitioner is an instance of the Partitioner class, which is responsible for organizing words into multiple
     * Huffman trees to facilitate efficient encoding and decoding of data. It partitions a set of words into these trees,
     * where each tree is constructed using a subset of the words. Remaining words are assigned as tail codes to the first
     * tree, ensuring a balance between tree size and code length.
     *
     * This partitioner is utilized by the containing class to manage encoding and decoding operations. It provides methods
     * for encoding a sequence of words into a bit stream and decoding a bit stream back into the original sequence of words.
     * The encoding process involves probabilistic selection based on the current word and the bit stream, while the decoding
     * process reconstructs the bit stream from the sequence of words using the corresponding Huffman trees.
     *
     * The partitioner's configuration, including the number of Huffman trees and their sizes, is determined during initialization
     * based on the provided word map and partitioning parameters. This allows for efficient and flexible encoding and decoding
     * operations tailored to the specific dataset.
     */
    Partitioner partitioner;

    /**
     * Constructs a new Zencoder instance with the specified partitioner.
     * The partitioner is used to manage encoding and decoding operations
     * by organizing words into Huffman trees and providing methods for
     * encoding and decoding data streams.
     *
     * @param partitioner the partitioner object responsible for managing
     *                    the Huffman trees and word mappings used in encoding
     *                    and decoding processes
     */
    public Zencoder(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    /**
     * Generates a secret key for AES encryption with the specified key size.
     * The method uses the KeyGenerator class to create a new AES key
     * initialized with the given key size.
     *
     * @param keySize the size of the key in bits (e.g., 128, 192, or 256)
     * @return a SecretKey object representing the generated AES key
     * @throws Exception if an error occurs during key generation
     */
    public static SecretKey generateKey(int keySize) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    /**
     * Encrypts the provided input string using the specified encryption algorithm and secret key.
     * The method generates a random initialization vector (IV) for added security, combines it with
     * the encrypted data, and encodes the result using a partitioner-based encoding mechanism.
     *
     * @param algorithm the name of the encryption algorithm to use (e.g., "AES/CBC/PKCS5Padding")
     * @param input the plaintext string to be encrypted
     * @param key the secret key used for encryption, typically generated using a key generation method
     * @return the encrypted and encoded string resulting from the encryption process
     * @throws Exception if an error occurs during encryption or encoding
     */
    public String encrypt(String algorithm, String input, SecretKey key)
            throws Exception {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
        BitStream stream = new BitStream(combined);
        List<String> res = partitioner.encode(stream);
        StringBuilder output = new StringBuilder();
        for (String word : res) {
            output.append(word);
        }
        return output.toString();
    }

    /**
     * Decrypts the provided cipher text using the specified encryption algorithm and secret key.
     * The method decodes the cipher text into a bit stream, extracts the initialization vector (IV)
     * and the actual cipher text, and then performs decryption using the provided algorithm and key.
     *
     * @param algorithm the name of the encryption algorithm to use for decryption
     *                  (e.g., "AES/CBC/PKCS5Padding")
     * @param cipherText the encoded cipher text to be decrypted
     * @param key the secret key used for decryption, typically generated using a key generation method
     * @return the decrypted plaintext string resulting from the decryption process
     * @throws Exception if an error occurs during decoding, extraction, or decryption
     */
    public String decrypt(String algorithm, String cipherText, SecretKey key)
            throws Exception {
        List<String> words = cipherText.codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .collect(Collectors.toList());
        BitStream stream = partitioner.decode(words);
        if (stream == null)
            return null;
        byte[] cipherBytes = stream.toByteArray();
        byte[] extractedIv = new byte[16];
        byte[] extractedCipherText = new byte[cipherBytes.length - 16];

        System.arraycopy(cipherBytes, 0, extractedIv, 0, 16);
        System.arraycopy(cipherBytes, 16, extractedCipherText, 0, cipherBytes.length - 16);


        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(extractedIv));
        byte[] plainText = cipher.doFinal(extractedCipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    /**
     * Encrypts the provided input string using a custom encoding mechanism that does not rely on AES encryption.
     * The method converts the input string into a bit stream, encodes it using the partitioner's encoding logic,
     * and concatenates the resulting encoded words into a single output string.
     *
     * @param input the plaintext string to be encrypted using the custom encoding mechanism
     * @return the encrypted string resulting from the custom encoding process
     */
    public String encryptWithoutAES(String input){
        BitStream bs = new BitStream(input.getBytes(StandardCharsets.UTF_8));
        List<String> res = partitioner.encode(bs);
        StringBuilder output = new StringBuilder();
        for (String word : res) {
            output.append(word);
        }
        return output.toString();
    }

    /**
     * Decrypts the provided cipher text using a custom decoding mechanism that does not rely on AES encryption.
     * The method processes the cipher text by splitting it into individual characters, decoding the characters
     * into a bit stream using the partitioner's decode logic, and converting the resulting bit stream back
     * into a plaintext string. If the decoding process fails, the method returns null.
     *
     * @param cipherText the encoded cipher text to be decrypted using the custom decoding mechanism
     * @return the decrypted plaintext string resulting from the custom decoding process, or null if decoding fails
     */
    public String decryptWithoutAES(String cipherText){
        List<String> words = cipherText.codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .collect(Collectors.toList());
        BitStream stream = partitioner.decode(words);
        if (stream == null)
            return null;
        return new String(stream.toByteArray(), StandardCharsets.UTF_8);
    }
}
