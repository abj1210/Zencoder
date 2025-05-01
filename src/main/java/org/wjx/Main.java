package org.wjx;

import org.wjx.partitioner.Partitioner;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Scanner;

/**
 * 主类，用于启动程序并提供编码与解码服务。
 * 该类通过控制台与用户交互，支持加载或生成划分器，并基于划分器执行多种字符串处理操作。
 * 用户可以选择是否创建新的划分器，或者从文件加载已有的划分器。
 * 加载划分器后，程序进入主服务循环，提供包括字符串编码、解码以及加密和解密的功能。
 * 支持的编码和解码模式包括普通模式和AES加密模式。
 * 程序运行期间，用户可以通过输入指定的选项来选择所需的服务，并在完成操作后继续选择其他服务或退出程序。
 * 所有用户输入均通过控制台进行，程序会根据输入执行相应的逻辑并输出结果。
 */
public class Main {
    /**
     * The main method serves as the entry point for the program. It handles user interaction
     * to create, load, and utilize a partitioner for encoding and decoding strings.
     * The method supports both plain and AES-encrypted operations and provides a menu-driven
     * interface for selecting services.
     *
     * @param args Command-line arguments passed to the program (not used in this implementation).
     *
     * The method performs the following steps:
     * 1. Prompts the user to create a new partitioner or load an existing one.
     * 2. If creating a new partitioner, it collects user input for parallelism size and dataset folder,
     *    generates the partitioner, and optionally saves it to a file.
     * 3. If loading an existing partitioner, it repeatedly prompts the user until a valid file is provided.
     * 4. Initializes a Zencoder instance with the loaded partitioner and enters a service loop.
     * 5. In the service loop, the user can choose from encoding/decoding options (with or without AES encryption)
     *    or exit the program.
     * 6. For AES-encrypted operations, the user can generate a new key or provide an existing one.
     * 7. The program continues to run until the user selects the exit option.
     *
     * Exceptions thrown during execution are propagated to the caller.
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        //加载划分器
        Partitioner partitioner = null;
        System.out.println("需要一个新的划分器嘛(Y/N)~");
        String input = scanner.nextLine();
        if (input.equals("Y") || input.equals("y")) {
            int hlsize = 0;
            while(hlsize <= 1) {
                System.out.println("输入字符并行数(要大于1哦)~");
                input = scanner.nextLine();
                hlsize = Integer.parseInt(input);
            }
            while(partitioner == null){
                System.out.println("输入一个数据集文件夹吧~");
                String filename = scanner.nextLine();
                partitioner = PartitionerGenerator.runGenerator(filename, hlsize);
                if(partitioner == null){
                    System.out.println("划分器读取失败???");
                }
            }
            System.out.println("划分器生成成功!要保存嘛(Y/N)~");
            input = scanner.nextLine();
            if (input.equals("Y") || input.equals("y")) {
                System.out.println("输入新文件名吧~");
                String filename = scanner.nextLine();
                PartitionerGenerator.savePartitioner(partitioner, filename);
            }
        }
        else{
            while(partitioner == null){
                System.out.println("输入划分器文件的地址吧~");
                String filename = scanner.nextLine();
                partitioner = PartitionerGenerator.loadPartitioner(filename);
                if(partitioner == null){
                    System.out.println("划分器读取失败???");
                }
            }
        }
        System.out.println("划分器 "+ partitioner +" 加载成功啦~");
        Zencoder zencoder = new Zencoder(partitioner);
        //主服务循环
        while(true){
            System.out.println("选择一项服务吧(输入相应的数字哦)~");
            System.out.println("0.编码字符串(不加密)");
            System.out.println("1.解码字符串(不加密)");
            System.out.println("2.编码字符串(AES加密)");
            System.out.println("3.解码字符串(AES加密)");
            System.out.println("4.退出程序");
            int choice = Integer.parseInt(scanner.nextLine());
            if(choice == 0){
                System.out.println("输入要编码的字符串吧~");
                String string = scanner.nextLine();
                String output = zencoder.encryptWithoutAES(string);
                System.out.println("编码成功啦:");
                System.out.println(output);
            }
            else if(choice == 1){
                System.out.println("输入要解码的字符串吧~");
                String string = scanner.nextLine();
                String output = zencoder.decryptWithoutAES(string);
                if(output == null){
                    System.out.println("解码失败???");
                }
                else{
                    System.out.println("解码成功啦:");
                    System.out.println(output);
                }
            }
            else if(choice == 2){
                String algorithm = "AES/CBC/PKCS5Padding";
                System.out.println("输入要编码的字符串吧~");
                String string = scanner.nextLine();
                System.out.println("需要新的密钥嘛(Y/N)~");
                input = scanner.nextLine();
                SecretKey key;
                if (input.equals("Y") || input.equals("y")) {
                    key = Zencoder.generateKey(256);
                }
                else{
                    System.out.println("输入你的密钥吧~");
                    String base64Key = scanner.nextLine();
                    byte[] decodedKey = Base64.getDecoder().decode(base64Key);
                    key = new SecretKeySpec(decodedKey, algorithm);
                }
                String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
                System.out.println("密钥生成啦:");
                System.out.println(encodedKey);
                String output = zencoder.encrypt(algorithm, string, key);
                System.out.println("编码成功啦:");
                System.out.println(output);
            }
            else if(choice == 3){
                String algorithm = "AES/CBC/PKCS5Padding";
                System.out.println("输入要解码的字符串吧~");
                String string = scanner.nextLine();
                System.out.println("输入你的密钥吧~");
                String base64Key = scanner.nextLine();
                byte[] decodedKey = Base64.getDecoder().decode(base64Key);
                SecretKey key = new SecretKeySpec(decodedKey, "AES");
                String output = zencoder.decrypt(algorithm, string, key);
                System.out.println("解码成功啦:");
                System.out.println(output);
            }
            else if(choice == 4){
                System.out.println("再见啦~");
                break;
            }
            else{
                System.out.println("是没见过的服务呢~");
            }
        }
        scanner.close();
    }
}