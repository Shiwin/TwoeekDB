package com.company;

import java.io.IOException;

public class Table {

    private TableAccess accessor;

    public Table(TableAccess accessor) {
        if (accessor == null) {
            return;
        }
        this.accessor = accessor;
    }

    public void addRecord(String[] values) throws IOException {
        this.accessor.addRecord(values);
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
        return this.accessor.getCountOfColumn();
    }

    public String[] findRecordLinear(int colNumber, String value) throws IOException {
        String[] result = null;
        for(int i = 0;i < getSize();i++){
            String[] crtRec = this.accessor.getRecord(i);
            if(crtRec[colNumber].equals(value)){
                result = crtRec;
            }
        }
        return result;
    }
}
