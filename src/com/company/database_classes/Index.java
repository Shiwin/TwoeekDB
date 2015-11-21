package com.company.database_classes;

import com.company.file_access.FileHashMap;
import com.company.file_access.TableAccess;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Index {
    protected FileHashMap map;    //file hashmap

    Index(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, int columnNumber, int sizeOfValue, int countOfValues, boolean shouldBeInit) throws IOException {
        this.map = new FileHashMap(raf, startPosition, endPosition,tableAccessor.getMaxTableSize(),
                tableAccessor.getColumnSize(columnNumber), sizeOfValue,countOfValues,true);
        if(shouldBeInit){
            this.map.initializeEmpty();
        }
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
}
