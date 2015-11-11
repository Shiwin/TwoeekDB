package com.company.file_access;

import java.io.IOException;
import java.io.RandomAccessFile;

public class StructureHandler extends WrapFile {

    public static StructureHandler createStructureHandler(RandomAccessFile raf, long[][] sizes) throws IOException {
        return WrapFile.createWrapFile(raf, sizes);
    }

    public StructureHandler(RandomAccessFile raf) {
        super(raf);
    }

    public long[] getTablesBlockStartEndPositions(){
        return getBlockStartEndPositions(0);
    }

    public long[] getIndecesBlockStartEndPositions(){
        return getBlockStartEndPositions(1);
    }

    public long[] getTableStartEnd(int tableNumber){
        return getSubBlockStartEndPosition(0, tableNumber);
    }

    public long[] getIndexStartEnd(int indexNumber){
        return getSubBlockStartEndPosition(1, indexNumber);
    }

    public long getTablesBlockSize(){
        return getBlockSize(0);
    }

    public long getIndecesBlockSize(){
        return getBlockSize(1);
    }

    public long getTableSize(int tableNumber){
        return getSubBlockSize(0, tableNumber);
    }

    public long getIndexSize(int indexNumber){
        return getSubBlockSize(1, indexNumber);
    }

    public int countOfTables(){
        return getCountOfSubBlocks(0);
    }

    public int countOfIndeces(){
        return getCountOfSubBlocks(1);
    }
}
