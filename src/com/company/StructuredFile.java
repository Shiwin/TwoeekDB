package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class create blocked structure in file.
 *
 * To create new structured file you should use static method 'createStructuredFile'.
 * Arguments are given: name of the new file, array of block positions, end of file.
 *
 * To use existed structured file, create the instance using constructor.
 * Arguments are given: name of existed structured file.
 *
 * public methods:
 *      int getCountOfBlocks()
 *      long getBlockPosition(int blockNumber)
 *      long getBlockEndPosition(int blockNumber)
 *      long getEndOfFile()
 */
public class StructuredFile {

    private static final String SPLITTER = " ";
    private static final String HEADER_END = "\n";

    public static StructuredFile createStructuredFile(String fileName, long[] blocksPositions, long endOfFile) throws IOException {
        if(fileName == null){
            throw new NullPointerException();
        }
        if(blocksPositions == null){
            throw new NullPointerException();
        }
        if(blocksPositions.length == 0){
            throw new IllegalArgumentException("blocksPositions is empty");
        }
        if(endOfFile < blocksPositions[blocksPositions.length - 1]){
            throw new IllegalArgumentException("endOfFile must be greater then position of the last block");
        }
        File file = new File(fileName);
        if(file.exists()){
            throw new IllegalStateException("file is already exist");
        }

        file.createNewFile();

        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        long previousPosition = 0;
        for(int i = 0;i < blocksPositions.length;i++){

            if(previousPosition > blocksPositions[i]){
                throw new IllegalArgumentException("positions must follow in increasing order");
            }
            previousPosition = blocksPositions[i];

            String crtValue = String.valueOf(blocksPositions[i]);
            raf.write(crtValue.getBytes());
            if(i < blocksPositions.length - 1) {
                raf.write(SPLITTER.getBytes());
            }else{
                raf.write(HEADER_END.getBytes());
            }

            long pointer = raf.getFilePointer();
            if(pointer > blocksPositions[0]){
                throw new IllegalArgumentException("pointers doesn't fit header size (move forward the first block position)");
            }
        }

        raf.setLength(endOfFile);

        raf.close();

        return new StructuredFile(file, blocksPositions);
    }

    private File file;
    private RandomAccessFile raf;
    private long[] blocksPositions;
    private long endOfFile;

    private StructuredFile(File file, long[] blocksPositions) throws IOException {
        this.file = file;
        this.blocksPositions = blocksPositions;
        this.raf = new RandomAccessFile(file, "rw");
        this.endOfFile = raf.length();
    }

    StructuredFile(String fileName) throws IOException {
        file = new File(fileName);
        if(!file.exists()){
            throw new FileNotFoundException();
        }

        this.raf = new RandomAccessFile(fileName,"rw");
        String header = raf.readLine();
        String[] stringBlocksPositions = header.split(String.valueOf(SPLITTER));
        blocksPositions = new long[stringBlocksPositions.length];
        for (int i = 0; i < blocksPositions.length; i++) {
            try {
                blocksPositions[i] = Long.valueOf(stringBlocksPositions[i]);
            }catch (Exception e){
                throw new IllegalStateException("can't parse long positions");
            }
        }

        if(blocksPositions.length == 0){
            throw new IllegalStateException("there is no blocks in this file");
        }

        endOfFile = raf.length();
    }

    public int getCountOfBlocks(){
        return blocksPositions.length;
    }

    public long getBlockPosition(int blockNumber){
        if(blockNumber < 0 || blockNumber > this.blocksPositions.length - 1){
            throw new IndexOutOfBoundsException("there is no block with this order number");
        }

        return blocksPositions[blockNumber];
    }

    public long getBlockEndPosition(int blockNumber){
        if(blockNumber < 0 || blockNumber > this.blocksPositions.length - 1){
            throw new IndexOutOfBoundsException("there is no block with this order number");
        }

        if(blockNumber == this.blocksPositions.length - 1){
            return endOfFile;
        }
        return blocksPositions[blockNumber + 1];
    }

    public long getEndOfFile(){
        return endOfFile;
    }
}
