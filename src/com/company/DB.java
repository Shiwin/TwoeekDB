package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.SimpleTimeZone;

/**
 * Created by ivan on 11.11.15.
 */
public class DB {

    /**
     * sizes of table and index segments
     */
    public static final long tableSize = 10000;
    public static final long indexSize = 20000;


    public static final long tableSizes[] = new long[]{
            1000,1000,1000,1014
    };

    public static final int numberOfTables = tableSizes.length;

    public static final long headerSize = 100;

    private StructuredFileBlock structuredFileBlock;
    private RandomAccessFile randomAccessFile;
    private Table currentTable;

    /**
     * Create db connection
     * @param path to file with db. If file doesn't exist then create it
     */
    public DB(String path) {
        try {
            File f = new File(path);
            if (f.exists()){
                randomAccessFile = new RandomAccessFile(f,"rwa");
                randomAccessFile.setLength(headerSize+indexSize+tableSize);
                structuredFileBlock = new StructuredFileBlock(randomAccessFile,0,randomAccessFile.length());
            }else {
                randomAccessFile = new RandomAccessFile(f,"rw");
                randomAccessFile.setLength(headerSize+indexSize+tableSize);
                long[] sizes = new long[]{headerSize,headerSize+tableSize};
                structuredFileBlock = StructuredFileBlock.createStructuredFileBlock
                        (randomAccessFile,0,randomAccessFile.length(),sizes);
                StructuredFileBlock sf = StructuredFileBlock.createStructuredFileBlock(randomAccessFile,headerSize,
                        headerSize+tableSize,getTablesPosition());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Intit table
     * @param numberOfTable
     * @param name
     * @param fields - names of fields
     * @param sizes - sizes of fields
     * @return Table
     */
    public Table initTable(Integer numberOfTable, String name,String[] fields,Integer sizes){
        return null;
    }


    /**
     * Getting begining and ending possitions of the table
     * @param num number of table
     * @return long[2]
     */
    private long[] getTableBeginEnd(Integer num){
        long[] offsets = new long[2];
        offsets[0] = headerSize;
        for (int i = 0; i < num; i++) {
            offsets[0] += tableSizes[i];
        }
        offsets[1] = offsets[0]+tableSizes[num];
        return offsets;
    }

    /**
     * Transform table sizes to table positions
     * @return
     */
    private long[] getTablesPosition(){
        long[] tablesPositions = new long[numberOfTables];
        for (int i = 0; i < numberOfTables; i++) {
            tablesPositions[i] = getTableBeginEnd(i)[0];
        }
        return tablesPositions;
    }

}
