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

    public long[] getTablesBlockStartEndPositions() throws IOException {
        return getBlockStartEndPositions(0);
    }

    public long[] getIndecesBlockStartEndPositions() throws IOException {
        return getBlockStartEndPositions(1);
    }

    public long[] getTableStartEnd(int tableNumber) throws IOException {
        return getSubBlockStartEndPosition(0, tableNumber);
    }

    public long[] getIndexStartEnd(int indexNumber) throws IOException {
        return getSubBlockStartEndPosition(1, indexNumber);
    }

    public long getTablesBlockSize() throws IOException {
        return getBlockSize(0);
    }

    public long getIndecesBlockSize() throws IOException {
        return getBlockSize(1);
    }

    public long getTableSize(int tableNumber) throws IOException {
        return getSubBlockSize(0, tableNumber);
    }

    public long getIndexSize(int indexNumber) throws IOException {
        return getSubBlockSize(1, indexNumber);
    }

    public int countOfTables(){
        return getCountOfSubBlocks(0);
    }

    public int countOfIndeces(){
        return getCountOfSubBlocks(1);
    }
}
