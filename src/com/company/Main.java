package com.company;

import java.io.File;
import java.io.RandomAccessFile;

public class Main {

    public static void main(String[] args) {
        String fileName = "test.txt";
        File file = new File(fileName);

        StructuredFileBlock fileBlocks = null;
        RandomAccessFile raf;
        long[] blocksPositions = new long[]{100,1000,3000,7000,10000};
        try {
            if (file.exists()) {
                raf = new RandomAccessFile(file, "rw");

                long[] lastSubBlocks = new long[]{10200, 12600};

                //fileBlocks = new StructuredFileBlock(raf,10000, 15000);
                fileBlocks = StructuredFileBlock.createStructuredFileBlock(raf, 10000, 15000, lastSubBlocks);
            }else {
                file.createNewFile();
                raf = new RandomAccessFile(file, "rw");
                raf.setLength(15000);

                fileBlocks = StructuredFileBlock.createStructuredFileBlock(raf, 0, raf.length(), blocksPositions);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        if(fileBlocks == null){
            System.out.println("lol");
            return;
        }

        System.out.println(fileBlocks.getCountOfBlocks());
    }
}
