package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class create blocked structure in file.
 *
 * To create new structured file you should use static method 'createStructuredFileBlock'.
 * Arguments are given: name of the new file, array of block positions, end of file.
 *
 * To use existed structured file, create the instance using constructor.
 * Arguments are given: name of existed structured file.
 *
 * public methods:
 *      int getCountOfBlocks()
 *      long getBlockBeginPosition(int blockNumber)
 *      long getBlockEndPosition(int blockNumber)
 *      long getEndPosition()
 */
public class StructuredFileBlock {

    private static final String SPLITTER = " ";
    private static final String HEADER_END = "\n";

    /**
     *
     * @param raf
     * @param startBlockPosition
     * @param endBlockPosition
     * @param blocksPositions
     * @return
     * @throws IOException
     */
    public static StructuredFileBlock createStructuredFileBlock(RandomAccessFile raf, long startBlockPosition, long endBlockPosition, long[] blocksPositions) throws IOException {
        if(raf == null){
            throw new NullPointerException();
        }
        if(blocksPositions == null){
            throw new NullPointerException();
        }
        if(blocksPositions.length == 0){
            throw new IllegalArgumentException("blocksPositions is empty");
        }
        if(startBlockPosition > endBlockPosition){
            throw new IllegalArgumentException("startBlockPosition must be less then endBlockPosition");
        }
        if(startBlockPosition > blocksPositions[0]){
            throw new IllegalArgumentException("startBlockPosition must be less then position of the first block");
        }
        if(endBlockPosition < blocksPositions[blocksPositions.length - 1]){
            throw new IllegalArgumentException("endBlockPosition must be greater then position of the last block");
        }

        long length = raf.length();
        if(endBlockPosition > length){
            throw new IllegalStateException("Size of file less then endBlock position");
        }

        raf.seek(startBlockPosition);

        long previousPosition = 0;
        for(int i = 0;i < blocksPositions.length;i++){
            if(previousPosition > blocksPositions[i]){
                throw new IllegalArgumentException("positions must follow in increasing order");
            }
            previousPosition = blocksPositions[i];
        }

        for(int i = 0;i < blocksPositions.length;i++){


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

        return new StructuredFileBlock(raf, startBlockPosition, endBlockPosition, blocksPositions);
    }

    private RandomAccessFile raf;
    private long[] blocksPositions;
    private long startPosition;
    private long endPosition;

    private StructuredFileBlock(RandomAccessFile raf, long startPosition,long endPosition, long[] blocksPositions) throws IOException {
        this.blocksPositions = blocksPositions;
        this.raf = raf;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    StructuredFileBlock(RandomAccessFile raf, long startPosition,long endPosition) throws IOException {
        this.raf = raf;
        this.raf.seek(startPosition);

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

        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    /**
     *
     * @return
     */
    public int getCountOfBlocks(){
        return blocksPositions.length;
    }

    /**
     *
     * @param blockNumber
     * @return
     */
    public long getBlockBeginPosition(int blockNumber){
        if(blockNumber < 0 || blockNumber > this.blocksPositions.length - 1){
            throw new IndexOutOfBoundsException("there is no block with this order number");
        }

        return blocksPositions[blockNumber];
    }

    /**
     *
     * @param blockNumber
     * @return
     */
    public long getBlockEndPosition(int blockNumber){
        if(blockNumber < 0 || blockNumber > this.blocksPositions.length - 1){
            throw new IndexOutOfBoundsException("there is no block with this order number");
        }

        if(blockNumber == this.blocksPositions.length - 1){
            return endPosition;
        }
        return blocksPositions[blockNumber + 1];
    }


    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition(){
        return endPosition;
    }
}
