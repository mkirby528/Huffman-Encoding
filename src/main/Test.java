package main;

import io.*;

import java.io.*;

public class Test {
    public static void main(String[] args) throws IOException, InsufficientBitsLeftException {

        Decoder decoder = new Decoder("data\\compressed.dat", "data\\uncompressed.dat");
        Encoder encoder = new Encoder("data\\input.dat", "data\\recompressed.dat");

        decoder.decode();
        encoder.encode();
        Decoder decoder2 = new Decoder("data\\recompressed.dat", "data\\unrecompressed.dat");
        decoder2.decode();


        FileInputStream fis = new FileInputStream("data\\uncompressed.dat");
        double[] trueProbs = new double[256];

        for (int i = 0; i < 255; i++) {
            trueProbs[i] = 0.0;
        }

        double theoreticalEntropy = 0.0;
        double symbolCount = 0.0;
        while (true) {
            try {
                int symbol = fis.read();
                symbolCount++;
                trueProbs[symbol] = trueProbs[symbol] + 1.0;
            } catch (Exception e) {
                break;
            }
        }
        fis.close();

        for (int i = 0; i < 256; i++) {
            trueProbs[i] = trueProbs[i] / symbolCount;
            if (trueProbs[i] != 0.0) {
                theoreticalEntropy += trueProbs[i] * (Math.log(trueProbs[i])/Math.log(2));

            }

        }
        theoreticalEntropy /= -1;
        System.out.println("Theoretical Entropy: " + theoreticalEntropy);

        double compressedCount = 0;
        fis = new FileInputStream("data\\compressed.dat");
        BitSource bs = new InputStreamBitSource(fis);
        while (true) {
            try {
                bs.next(1);
                compressedCount++;
            } catch (InsufficientBitsLeftException e) {
                break;
            }
        }
        compressedCount -= 260;
        System.out.println("Original Compressed entropy: " + (compressedCount / symbolCount));


        double recompressedCount = 0;
        fis = new FileInputStream("data\\recompressed.dat");
        bs = new InputStreamBitSource(fis);
        while (true) {
            try {
                bs.next(1);
                recompressedCount++;
            } catch (InsufficientBitsLeftException e) {
                break;
            }
        }
        recompressedCount -= 260;
        System.out.println("My Enocder Compressed Entropy: " + (recompressedCount / symbolCount));


    }
}
