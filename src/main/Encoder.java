package main;

import io.OutputStreamBitSink;

import java.io.*;
import java.util.*;

public class Encoder {

    private TreeMap<Integer, Integer> symbolFreqs = new TreeMap<>();
    private TreeMap<Integer, ArrayList<Integer>> lengths = new TreeMap<>();
    private TreeMap<String, Integer> huffmanCodes = new TreeMap<>();
    private ArrayList<Integer> sizeList = new ArrayList<>();
    private int numSym;
    private FileInputStream fis;
    private FileOutputStream fos;
    OutputStreamBitSink bitSink;
    String inPath, outPath;

    public Encoder(String inPath, String outPath) {
        try {
            fis = new FileInputStream(inPath);
            fos = new FileOutputStream(outPath);
            bitSink = new OutputStreamBitSink(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.inPath = inPath;
        this.outPath = outPath;
        this.numSym = 0;

    }

    public void encode() throws IOException {

        symbolFreqs = calcSymbProbs(fis);
        Node root = buildMinVarianceTree(symbolFreqs);
        getCodeLengths(root, 0);
        huffmanCodes = generateHuffmanCodes(lengths);

        for (int i = 0; i < 256; i++) {
            int len = getKeyByValue(huffmanCodes, i).length();
            byte b = (byte) len;
            bitSink.write(b, 8);
        }
        bitSink.write(numSym, 32);



        fis = new FileInputStream(inPath);
        while (fis.available()!=0) {
            int symbol = fis.read();
            String code = getKeyByValue(huffmanCodes, symbol);
            bitSink.write(code);
        }
        bitSink.padToWord();
        fis.close();
        fos.close();


    }


    private TreeMap<Integer, Integer> calcSymbProbs(FileInputStream file) throws IOException {
        TreeMap<Integer, Integer> freqs = new TreeMap<>();
        for (int i = 0; i < 256; i++) {
            freqs.put(i, 0);
        }
        while (true) {
            try {
                int symb = file.read();
                numSym++;
                Integer old = freqs.get(symb);
                freqs.put(symb, 1 + old);

            } catch (Exception e) {
                break;
            }
        }
        file.close();
        return freqs;

    }

    private Node buildMinVarianceTree(TreeMap<Integer, Integer> symbolProbs) {
        PriorityQueue<Node> nodes = new PriorityQueue<>(256, new NodeComparator());
        for (Map.Entry<Integer, Integer> entry : symbolProbs.entrySet()) {
            int value = entry.getKey();
            double prob = entry.getValue();
            Node newNode = new Node(value, prob, null, null);
            nodes.add(newNode);
        }

        //Check priority queue order
//        while(nodes.size() > 1){
//            Node n = nodes.remove();
//            System.out.println(n.value + " Frequencey --->" + n.freq);
//        }

        while (nodes.size() > 1) {
            Node node1 = nodes.remove();
            Node node2 = nodes.remove();

//            System.out.println("Left --> Value: " + node1.value + "---> Freq: " + node1.freq + "---> Height: " + node1.getHeight(node1));
//            System.out.println("Right --> Value: " + node2.value + "---> Freq: " + node2.freq + "---> Height: " + node2.getHeight(node2));

            Node interiorNode = new Node(-1, (node1.freq + node2.freq), node1, node2);
//            System.out.println("Combined --> Value: " + interiorNode.value + "---> Freq: " + interiorNode.freq + "---> Height: " + interiorNode.getHeight(interiorNode) + "\n");

            nodes.add(interiorNode);
        }
        return nodes.remove();
    }

    private void getCodeLengths(Node root, int codeLength) {

        if (root == null) {
            return;
        }

        //Base Case
        if (root.isLeaf()) {
            lengths.putIfAbsent(codeLength, new ArrayList<>());
            lengths.get(codeLength).add(root.value);
            Collections.sort(lengths.get(codeLength));

            if (!sizeList.contains(codeLength)) {
                sizeList.add(codeLength);
                Collections.sort(sizeList);
            }
            return;
        }

        // Add 1 when on going left or right.
        getCodeLengths(root.left, codeLength + 1);
        getCodeLengths(root.right, codeLength + 1);


    }

    public TreeMap<String, Integer> generateHuffmanCodes(TreeMap<Integer, ArrayList<Integer>> codewordLengths) {
        int code = 0;
        int nextSize = 0;
        int sizeIndex = 0;
        Iterator<Map.Entry<Integer, ArrayList<Integer>>> it = codewordLengths.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ArrayList<Integer>> entry = it.next();
            int lenght = entry.getKey();
            for (Integer value : entry.getValue()) {
                if (value == entry.getValue().get(entry.getValue().size() - 1)) {
                    if (sizeIndex < sizeList.size() - 1) {
                        sizeIndex++;
                    }
                    nextSize = sizeList.get(sizeIndex);
                } else {
                    nextSize = lenght;
                }
                String format = "%" + lenght + "s" + "";
                String codeInString = String.format(format, Integer.toBinaryString(code)).replace(' ', '0');
                huffmanCodes.put(codeInString, value);
                code = (code + 1) << ((nextSize) - (lenght));
            }
        }
        return huffmanCodes;
    }


    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


    class NodeComparator implements Comparator<Node> {
        public int compare(Node a, Node b) {
            if (a.freq < b.freq)
                return -1;
            if (a.freq > b.freq)
                return 1;
            if (a.getHeight(a) < b.getHeight(b))
                return -1;
            if (a.getHeight(a) > b.getHeight(b))
                return 1;


            return 0;
        }
    }
}
