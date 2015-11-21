package com.company.database_classes;

import com.company.file_access.FileHashMap;
import com.company.file_access.TableAccess;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Index {
    private static final int SIZE_OF_INTEGER = 7;       // max size of position value (in case of integer primary key)
    public static int getSizeOfIntegerValue(){
        return SIZE_OF_INTEGER;
    }

    protected int maxCountOfRepeatedValues;
    protected FileHashMap map;    //file hashmap

    Index(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, int columnNumber, int countOfValues, boolean shouldBeInit) throws IOException {
        if(!shouldBeInit){
            raf.seek(startPosition);
            this.maxCountOfRepeatedValues = Integer.parseInt(raf.readLine());
        }else{
            this.maxCountOfRepeatedValues = countOfValues;
        }
        this.map = new FileHashMap(raf, startPosition + 8, endPosition,tableAccessor.getMaxTableSize(),  // 8 - size for meta
                tableAccessor.getColumnSize(columnNumber), SIZE_OF_INTEGER,this.maxCountOfRepeatedValues,true);
        if(shouldBeInit){
            writeMeta(raf, startPosition);
            this.map.initializeEmpty();
        }
    }

    private void writeMeta(RandomAccessFile raf, long startPosition) throws IOException {
        raf.seek(startPosition);
        raf.write(String.valueOf(maxCountOfRepeatedValues).getBytes());
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
}
