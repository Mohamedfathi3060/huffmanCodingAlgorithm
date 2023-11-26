import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.HashMap;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.lang.Thread.sleep;

class Node {
    public int freq;
    public char character;
    public String code;
    public Node left;
    public Node right;
    Node(){
        freq = 1;
        code = "";
        character = ' ';
        left = right = null;
    }
}
class the_comparator implements Comparator<Node> {
    @Override
    public int compare(Node n1 , Node n2){
        return    n1.freq - n2.freq;
    }
}

public class Main {
    private static void recursion(Node node , String s){
        if(node == null) return;
        node.code = s ;
        recursion(node.left,node.code + '0');
        recursion(node.right,node.code + '1');
    }
    public static void comp(File file) throws FileNotFoundException{
        Scanner read  = new Scanner(file);
        String data ="";
        String result = "";
        while (read.hasNextLine()){
            data = data.concat(read.nextLine());
        }
        // data is ready
        PriorityQueue<Node> Q = new PriorityQueue<>(new the_comparator());
        HashMap<Character,Node>  MAP = new HashMap<>();
        for (int i = 0; i < data.length(); i++) {
            if(MAP.containsKey(data.charAt(i))){
                MAP.get(data.charAt(i)).freq++;
            }
            else{
                Node x = new Node();
                x.character = data.charAt(i);
                MAP.put(data.charAt(i),x);
            }
        }
        for ( Character c : MAP.keySet()){
//            System.out.println("char " + c + " -> "+ MAP.get(c).freq);
            Q.add(MAP.get(c));
        }
        // output Q sorted values
//        while ( !Q.isEmpty()){
//            System.out.println("char " + Q.peek().character + " -> "+ Q.peek().freq);
//            Q.poll();
//        }


        // build Tree
        while (Q.size() > 1){
            Node o1 = Q.poll();
            Node o2 = Q.poll();
            Node mergeNode = new Node() ;
            mergeNode.freq = o1.freq + o2.freq ;
            // set right child to the smallest freq to take 1 after coding
            mergeNode.right = o1 ;
            mergeNode.left = o2  ;
            // add new merged node
            Q.add(mergeNode);
        }
        Node root = Q.peek();
        // fill tree with proper codes
        recursion(root,"");

        // set Overhead table to result value
        for ( Character c : MAP.keySet()){
            String x = Integer.toBinaryString(c);
            System.out.println(c +" -> "+MAP.get(c).code);
            // add 00000 to right of code to prevent Error in reading
            result +=  setBitRight(MAP.get(c).code);
            result +=  charToBin(x);
        }

        // sign to indicate reach end of the MAP data
        result += "11111111";

        Node x;
        // iterate over data again to write each char code to file
        for (int i = 0; i < data.length(); i++) {
                x = MAP.get(data.charAt(i));
                // add node code to file
                result += x.code;
        }
        System.out.println(result);
        // write to file
        writeBitsToFile("comp.huff",result);
    }
    public static void deComp(String file) throws IOException{
        String data = readBitsFromFile(file);
        HashMap<String,Character> MAP = new HashMap<>();
        String result ="";
        int i = 0 ;
        String x = "";
        while (!data.substring(i, i + 8).equals("11111111")){
            x = data.substring(i,i+8);
            MAP.put(x,(char) Integer.parseInt(data.substring(i+8,i+16),2));
            i+=16;
        }
        for (String s : MAP.keySet()){
            System.out.println(MAP.get(s) + " -> "+ s);
        }
        int  j = i+1 ;
        while (i  < data.length() && j <= data.length()){
            x = data.substring(i,j);
            x = setBitRight(x);
            if(MAP.containsKey(x)){
                // code found
                result += MAP.get(x);
                i = j ;
            }
            j++;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("decomp.txt"))) {
            // Write the entire big string to the file
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private static String charToBin(String x){
        int paddingLength = 8 - x.length();
        if (paddingLength > 0) {
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < paddingLength; i++) {
                padding.append('0');
            }
            x = padding.toString() + x;
        }
        return x ;
    }
    private static String setBitRight(String x){
        int paddingLength = 8 - x.length();
        if (paddingLength > 0) {
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < paddingLength; i++) {
                padding.append('0');
            }
            x = x + padding.toString();
        }
        return x ;
    }
    private static void writeBitsToFile(String fileName, String bits) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            int currentByte = 0;
            int bitsCount = 0;

            for (char bitChar : bits.toCharArray()) {
                if (bitChar == '1') {
                    currentByte = (currentByte << 1) | 1;
                } else if (bitChar == '0') {
                    currentByte = (currentByte << 1);
                } else {
                    // Ignore non-binary characters
                    continue;
                }

                bitsCount++;

                // Write the current byte when 8 bits are accumulated
                if (bitsCount == 8) {
                    fos.write(currentByte);
                    currentByte = 0;
                    bitsCount = 0;
                }
            }

            // If there are remaining bits, pad with zeros and write the last byte
            if (bitsCount > 0) {
                currentByte <<= (8 - bitsCount);
                fos.write(currentByte);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String readBitsFromFile(String fileName) {
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(fileName)) {
            int currentByte;

            while ((currentByte = fis.read()) != -1) {
                for (int i = 7; i >= 0; i--) {
                    int bit = (currentByte >> i) & 1;
                    result.append(bit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        String data = "ABBCCCDDDDFFFFF";
//        PriorityQueue<Node> Q = new PriorityQueue<>(new the_comparator());
//        HashMap<Character,Node>  MAP = new HashMap<>();
//        for (int i = 0; i < data.length(); i++) {
//            if(MAP.containsKey(data.charAt(i))){
//                MAP.get(data.charAt(i)).freq++;
//
//            }
//            else{
//                Node x = new Node();
//                x.character = data.charAt(i);
//                MAP.put(data.charAt(i),x);
//
//            }
//        }
//        for ( Character c : MAP.keySet()){
////            System.out.println("char " + c + " -> "+ MAP.get(c).freq);
//            Q.add(MAP.get(c));
//        }
//
//        String x = Integer.toBinaryString(' ') ;
//        int paddingLength = 8 - x.length();
//        if (paddingLength > 0) {
//            StringBuilder padding = new StringBuilder();
//            for (int i = 0; i < paddingLength; i++) {
//                padding.append('0');
//            }
//            x = padding.toString() + x;
//        }
//        System.out.println(x);
//        File x = new File("data.txt");
//        comp(x);
        deComp("comp.huff");

       // SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("File Compression App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 500));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel row1 = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(10, 40);
        textArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        row1.add(textArea, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse");
        row1.add(browseButton, BorderLayout.EAST);

        JPanel row2 = new JPanel();
        row2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");

        row2.add(compressButton);
        row2.add(decompressButton);

        mainPanel.add(row1);
        mainPanel.add(row2);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser("M:\\java library\\LZW\\LZW_implementation");
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    textArea.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement compression logic here
                File file = new File(textArea.getText());

                try {
                    comp(file);
                    JOptionPane.showMessageDialog(frame, "Operation was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(frame, "Operation failed.", "Failure", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implement decompression logic here
                FileInputStream file = null;
                try {
//                    file = new FileInputStream(textArea.getText());
                    deComp(textArea.getText());
                    JOptionPane.showMessageDialog(frame, "Operation was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Operation failed.", "Failure", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);

                }
            }
        });

    }
}