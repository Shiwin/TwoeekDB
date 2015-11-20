package com.company;

import java.io.IOException;
import java.util.HashMap;

public class Table {

    private TableAccess accessor;
    private PrimaryIndex keyIndex;

    public Table(TableAccess accessor, PrimaryIndex keyIndex) {
        if (accessor == null) {
            return;
        }
        this.accessor = accessor;
        this.keyIndex = keyIndex;
    }

    public void addRecord(String[] values) throws Exception {
        int number = this.accessor.addRecord(values);
        this.keyIndex.put(values[accessor.getKeyColumn()], number);
    }

    public String[] getRecord(int i) throws IOException {
        return this.accessor.getRecord(i);
    }

    public String getName(){
        return this.accessor.getTableName();
    }

    public int getSize(){
        return this.accessor.getTableSize();
    }

    public int getCountOfColumns(){
        return this.accessor.getCountOfColumns();
    }

    private String[] findRecordByKeyColumn(String value) throws Exception {
        String[] result = null;
        int[] recordNumbers = keyIndex.get(value);
        if(recordNumbers == null){
            return null;
        }
        if(recordNumbers.length != 1){
            throw new IllegalStateException("Index must keep only one value for primary key");
        }
        return getRecord(recordNumbers[0]);
    }

    private String[] findRecordLinear(int colNumber, String value) throws IOException {
        String[] result = null;
        for (int i = 0; i < getSize(); i++) {
            String[] crtRec = this.accessor.getRecord(i);
            if (crtRec[colNumber].equals(value)) {
                result = crtRec;
            }
        }
        return result;
    }
    public String[] findRecord(int colNumber, String value) throws Exception {
        String[] result = null;
        if(colNumber == accessor.getKeyColumn()){
            result = findRecordByKeyColumn(value);
        }else {
            result = findRecordLinear(colNumber, value);
        }
        return result;
    }

    public String[] findRecord(String columnName, String value) throws Exception {
        HashMap<String, Integer> columns = accessor.getColNamesHash();
        Integer colNumber = columns.get(columnName);
        if(colNumber == null){
            return null;
        }
        return findRecord(colNumber, value);
    }

    public String getKeyColumn(){
        return accessor.getColumnName(accessor.getKeyColumn());
    }
}
