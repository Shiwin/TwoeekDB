package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Сергей on 11.11.2015.
 */
public class WrapFile {
    protected static final long headerSize = 300;
    protected static long[] getInBlockPositions(long offset, long[] sizes){
        long[] positions = new long[sizes.length];
        long localOffset = offset + headerSize;
        positions[0] = localOffset;
        for(int i = 1; i < positions.length; i++){
            positions[i] = positions[i - 1] + sizes[i - 1];
        }
        return positions;
    }

    protected static long fullSize(long[] sizes){
        long fullSize = 0;
        for (int i = 0; i < sizes.length; i++) {
            fullSize += sizes[i];
        }
        return fullSize;
    }

    protected static long[] getTopBlockPositions(long offset, long[][] sizes){
        long[] positions = new long[sizes.length];

        positions[0] = offset;
        for (int i = 1; i < sizes.length; i++) {
            positions[i] = positions[i - 1] + fullSize(sizes[i - 1]);
        }

        return positions;
    }

    protected static WrapFile createWrapFile(RandomAccessFile raf, long[][] sizes) throws IOException {
        long[] topBlockPositions = getTopBlockPositions(headerSize, sizes);
        long fullBlocksSize = 0;
        for (int i = 0; i < sizes.length; i++) {
            fullBlocksSize += fullSize(sizes[i]);
        }
        raf.setLength(fullBlocksSize);
        StructuredFileBlock sfb = StructuredFileBlock.createStructuredFileBlock(raf, 0, raf.length(), topBlockPositions);

        for (int i = 0; i < sizes.length - 1; i++) {

            StructuredFileBlock localSFB = StructuredFileBlock.createStructuredFileBlock(
                            raf, topBlockPositions[i],topBlockPositions[i + 1],getInBlockPositions(topBlockPositions[i],sizes[i]));


        }
        int pos = topBlockPositions.length - 1;
        StructuredFileBlock localSFB = StructuredFileBlock.createStructuredFileBlock(
                raf, topBlockPositions[pos],raf.length(),getInBlockPositions(topBlockPositions[pos],sizes[pos]));

        return new WrapFile(raf);
    }

    protected RandomAccessFile raf;

    WrapFile(RandomAccessFile raf){
        this.raf = raf;
    }

    protected long[] getBlockStartEndPositions(int blockNumber){

        long length = 0;
        long[] startEnd = new long[2];
        try {
            length = raf.length();
            StructuredFileBlock sfb = new StructuredFileBlock(raf, 0, length);
            if(blockNumber > sfb.getCountOfBlocks()){
                throw new IllegalArgumentException("No such blockNumber: " + blockNumber);
            }
            startEnd[0] = sfb.getBlockBeginPosition(blockNumber);
            startEnd[1] = sfb.getBlockEndPosition(blockNumber);
        } catch (IOException e) {
            System.out.println("Wrong initialization of RandomAccessFile"); // TODO add logger
        }
        return startEnd;
    }

    protected long[] getTablesBlockStartEndPositions(){
        return getBlockStartEndPositions(0);
    }

    protected long[] getIndecesBlockStartEndPositions(){
        return getBlockStartEndPositions(1);
    }

    protected long[] getSubBlockStartEndPosition(int blockNumber, int subBlockNumber){
        long[] startEnd = new long[2];
        long length = 0;
        try{
            length = raf.length();
            StructuredFileBlock sfb = new StructuredFileBlock(raf, 0, length);
            if(blockNumber > sfb.getCountOfBlocks()){
                throw new IllegalArgumentException("No such blockNumber: " + blockNumber);
            }
            StructuredFileBlock tabSfb = new StructuredFileBlock(raf, sfb.getBlockBeginPosition(blockNumber), sfb.getBlockEndPosition(blockNumber));

            if(subBlockNumber > sfb.getCountOfBlocks()){
                throw new IllegalArgumentException("No such subBlockNumber: " + subBlockNumber);
            }
            startEnd[0] = tabSfb.getBlockBeginPosition(subBlockNumber);
            startEnd[1] = tabSfb.getBlockEndPosition(subBlockNumber);
        }catch (IOException e){
            System.out.println("Wrong initialization of RandomAccessFile"); // TODO add logger
        }

        return startEnd;
    }

    protected long[] getTableStartEnd(int tableNumber){
        return getSubBlockStartEndPosition(0, tableNumber);
    }

    protected long[] getIndexStartEnd(int indexNumber){
        return getSubBlockStartEndPosition(1, indexNumber);
    }
}
