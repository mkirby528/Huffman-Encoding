package main;

import io.BitSource;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import jdk.nashorn.api.tree.Tree;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Decoder {
    TreeMap<Integer, ArrayList<Integer>> codewordLengths = new TreeMap<>();
    private ArrayList<Integer> sizeList = new ArrayList<>();
    private int sizeIndex = 0;
    private TreeMap<String, Integer> huffmanCodes = new TreeMap<>();

    private int totalSymbols = 0;
    String infile, outfile;
    FileInputStream fis;
    FileOutputStream fos;
    BitSource file;


    public Decoder(String infile, String outfile) {
        this.infile = infile;
        this.outfile = outfile;
        try {
            this.fis = new FileInputStream(infile);
            this.fos = new FileOutputStream(outfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.file = new InputStreamBitSource(fis);

    }

    public int decode() throws IOException, InsufficientBitsLeftException {
        TreeMap<Integer, ArrayList<Integer>> lenghts = getCodewordLengths(256, file);
        TreeMap<String, Integer> huffmanCodes = generateHuffmanCodes(lenghts);
//        for (Map.Entry<String, Integer> entry : huffmanCodes.entrySet()) {
//            System.out.println("Code: " + entry.getKey() + ". Value: " + entry.getValue());
//        }
        try {
            totalSymbols = file.next(32);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String code = "";




        //loop through symbols 574992
        while (totalSymbols > 0) {
            if (file.next(1) == 0) {
                code += "0";
            } else {
                code += "1";
            }
            if (huffmanCodes.keySet().contains(code)) {
                fos.write(huffmanCodes.get(code));
                code = "";
                totalSymbols--;
            }

        }
        fos.close();
        return -1;


    }

    public TreeMap<Integer, ArrayList<Integer>> getCodewordLengths(int n, BitSource file) {
        try {
            for (int i = 0; i < n; i++) {
                int codelenght = file.next(8);
                codewordLengths.putIfAbsent(codelenght, new ArrayList<>());
                codewordLengths.get(codelenght).add(i);
                if (!sizeList.contains(codelenght)) {
                    sizeList.add(codelenght);
                }
            }
            Collections.sort(sizeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codewordLengths;
    }

    public TreeMap<String, Integer> generateHuffmanCodes(TreeMap<Integer, ArrayList<Integer>> codewordLengths) {
        int code = 0;
        int nextSize = 0;
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


}