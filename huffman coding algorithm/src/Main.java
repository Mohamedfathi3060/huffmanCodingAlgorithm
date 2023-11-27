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
    private static byte[] stringToBytes(String s){
        // Ensure that the binary string length is a multiple of 8

        if (s.length() % 8 != 0) {
            int paddingLength = 8 - s.length()%8;
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < paddingLength; i++) {
                padding.append('0');
            }
            s = s + padding.toString();
        }

        // Create an array to store the bytes
        byte[] byteArray = new byte[s.length() / 8];

        // Convert each group of 8 binary digits to a byte
        for (int i = 0; i < s.length(); i += 8) {
            String binaryByte = s.substring(i, i + 8);
            byte decimalValue = (byte) Integer.parseInt(binaryByte, 2);
            byteArray[i / 8] = decimalValue;
        }

        return byteArray;
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
            Q.add(MAP.get(c));
        }

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
        HashMap<String,Character>  obj = new HashMap<>();
        for ( Character c : MAP.keySet()){
            obj.put(MAP.get(c).code,c);
        }
        Node x;
        // iterate over data again to write each char code to file
        for (int i = 0; i < data.length(); i++) {
            x = MAP.get(data.charAt(i));
            // add node code to file
            result += x.code;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("comp.huff"))) {
            oos.writeObject(obj);

            // sign to indicate reach end of the MAP data
            oos.writeByte(0xFF);


            byte[] binaryData = stringToBytes(result);
            oos.write(binaryData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }
    public static void deComp(String file) throws IOException {
        HashMap<String, Character> MAP = new HashMap<>();
        byte[] binaryData ;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            // Deserialize the object
            Object obj = ois.readObject();

            if (obj instanceof HashMap) {
                MAP = (HashMap<String, Character>) obj ;
            } else {
                System.out.println("Error happened");
                return;
            }

            // Read the delimiter (0xFF)
            int delimiter = ois.readUnsignedByte();
            if (delimiter != 0xFF) {
                System.out.println("Error happened");
                return;
            }
            // Read regular binary data
            binaryData = new byte[ois.available()];
            ois.read(binaryData);

        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

            ///////////////////////////////////////////////////////
        StringBuilder binaryStringBuilder = new StringBuilder() ;
        for (byte x : binaryData){
            binaryStringBuilder.append(String.format("%8s", Integer.toBinaryString(x & 0xFF)).replace(' ', '0'));

        }
            String data = binaryStringBuilder.toString();
            String result = "";
            String x = "";

            for (String s : MAP.keySet()) {
                System.out.println(MAP.get(s) + " -> " + s);
            }
            int i = 0;
            int j = i + 1;
            while (i < data.length() && j <= data.length()) {
                x = data.substring(i, j);
                if (MAP.containsKey(x)) {
                    // code found
                    result += MAP.get(x);
                    i = j;
                }
                j++;
            }
        System.out.println(result);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("decomp.txt"))) {
                // Write the entire big string to the file
                writer.write(result);
            } catch (IOException e) {
                e.printStackTrace();
            }



    }

    public static void main(String[] args) throws IOException, InterruptedException {

//        File x = new File("data.txt");
//        comp(x);
//        deComp("comp.huff");

       SwingUtilities.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("HuffMan Compression App");
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
                JFileChooser fileChooser = new JFileChooser("M:\\java library\\huffmanCodingAlgorithm\\huffman coding algorithm");
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