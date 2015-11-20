package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PrimaryIndex extends Index{
    private FileHashMap map;

    PrimaryIndex(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, boolean shouldBeInit) throws IOException {
        this.map = new FileHashMap(raf, startPosition, endPosition,tableAccessor.getMaxTableSize(),
                tableAccessor.getColumnSize(tableAccessor.getKeyColumn()),1,true);
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
}
