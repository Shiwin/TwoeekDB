package com.company.database_classes;

import com.company.file_access.FileHashMap;
import com.company.file_access.TableAccess;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Index {
    public static final int META_SIZE = 16;
    private static final int SIZE_OF_INTEGER = 7;       // max size of position value (in case of integer primary key)
    public static int getSizeOfIntegerValue(){
        return SIZE_OF_INTEGER;
    }

    protected int maxCountOfRepeatedValues;
    protected int countOfUniqWordsInRecordColumn;
    protected FileHashMap map;    //file hashmap

    Index(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, int columnNumber, int countOfValues, int countOfUniqWordsInRecordColumn, boolean shouldBeInit) throws IOException {
        if(!shouldBeInit){
            raf.seek(startPosition);
            this.maxCountOfRepeatedValues = Integer.parseInt(raf.readLine());
            this.countOfUniqWordsInRecordColumn = Integer.parseInt(raf.readLine());
        }else{
            this.maxCountOfRepeatedValues = countOfValues;
            this.countOfUniqWordsInRecordColumn = countOfUniqWordsInRecordColumn;
        }
        if(this.countOfUniqWordsInRecordColumn < 1) {
            this.map = new FileHashMap(raf, startPosition + META_SIZE, endPosition, tableAccessor.getMaxTableSize(),
                    tableAccessor.getColumnSize(columnNumber), SIZE_OF_INTEGER, this.maxCountOfRepeatedValues, true);
        }else{
            this.map = new FileHashMap(raf, startPosition + META_SIZE, endPosition, this.countOfUniqWordsInRecordColumn,
                    tableAccessor.getColumnSize(columnNumber), SIZE_OF_INTEGER, this.maxCountOfRepeatedValues, true);
        }
        if(shouldBeInit){
            writeMeta(raf, startPosition);
            //this.map.initializeEmpty();
        }
    }

    private void writeMeta(RandomAccessFile raf, long startPosition) throws IOException {
        raf.seek(startPosition);
        raf.write(String.valueOf(maxCountOfRepeatedValues).getBytes());
        raf.write("\n".getBytes());
        raf.write(String.valueOf(countOfUniqWordsInRecordColumn).getBytes());
        raf.write("\n".getBytes());
    }

    public boolean put(String key, int value) throws Exception {
        return this.map.put(key, value);
    }

    public int[] get(String key) throws Exception {
        return this.map.get(key);
    }

    public int getSizeOfKey(){
        return this.map.getSizeOfKey();
    }

    public int getSizeOfValue(){
        return this.map.getSizeOfValue();
    }

    public boolean isExist() throws IOException {
        return map.isExist();
    }

    public int getCountOfUniqWordsInRecordColumn() {
        return countOfUniqWordsInRecordColumn;
    }
}
