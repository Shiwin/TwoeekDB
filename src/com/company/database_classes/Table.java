package com.company.database_classes;

import com.company.file_access.TableAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class-interface for working with table inside the database
 */
public class Table {

    //============================= private fields ===================================
    private TableAccess accessor;       // get access to the table according to given start/end positions in file
    private PrimaryIndex keyIndex;      // get access to the index according to given start/end positions in file

    private ColumnIndex[] columnIndexes;
    //================================================================================

    /**
     * Takes already defined accessor and keyIndex (they are defined in SimpleDB)
     * @param accessor
     * @param keyIndex
     * @param columnIndexes
     */
    public Table(TableAccess accessor, PrimaryIndex keyIndex, ColumnIndex[] columnIndexes){
        if (accessor == null) {
            return;
        }
        this.accessor = accessor;
        this.keyIndex = keyIndex;
        if(columnIndexes != null && columnIndexes.length != accessor.getCountOfColumns()){
            throw new IllegalArgumentException("count of columnIndexes must be the same as count of columns themselves");
        }
        this.columnIndexes = columnIndexes;
    }

    /**
     * add record into table block by using accessor ...
     * ... and indexing this new value
     * @param values
     * @throws Exception
     */
    public void addRecord(String[] values) throws Exception {
        /**
         * checks if this key already in the table
         * BUT makes addition slowly
         *
         */
        /*
        int key = accessor.getKeyColumn();
        String[] keyAlreadyWas = this.searchRecordByKeyColumn(values[key]);
        if(keyAlreadyWas != null){
            throw new Exception("Record with this primary key has been already added.");
        }
        */
        int nextNumber = this.accessor.getTableSize();
        if(this.keyIndex != null) {
            this.keyIndex.put(values[accessor.getKeyColumn()], nextNumber);
        }

        if(columnIndexes != null){
            for(int i = 0;i < columnIndexes.length;i++){
                if(this.columnIndexes[i] != null) {
                    this.columnIndexes[i].put(values[i], nextNumber);
                }
            }
        }

        int number = this.accessor.addRecord(values);
        if(nextNumber != number){
            throw new Exception("Next number and previous size are different.");
        }
    }

    /**
     *  Search records by column with given value (exact matching)
     *
     * @param columnName
     * @param value
     *
     * @param searchingLimit constrain for not freezing the system.
     *      Allows not to run through all records - only from 0 to searchingLimit
     *
     * @return
     * @throws Exception
     */
    public List<String[]> searchRecord(String columnName, String value, int searchingLimit) throws Exception {
        HashMap<String, Integer> columns = accessor.getColNamesHash();
        Integer colNumber = columns.get(columnName);
        if(colNumber == null){
            return null;
        }

        List<String[]> result = null;
        if(colNumber == accessor.getKeyColumn()){
            result = new ArrayList<>();
            String[] keyRecord = searchRecordByKeyColumn(value);
            if(keyRecord != null) {
                result.add(keyRecord);
            }
        }else {
            if(columnIndexes == null) {
                result = searchRecordLinear(colNumber, value, searchingLimit);
            }else{
                if(columnIndexes[colNumber] != null){
                    result = searchRecordByIndex(colNumber,value);
                }else{
                    result = searchRecordLinear(colNumber,value,0);
                }
            }
        }
        return result;
    }

    public String getName(){
        return this.accessor.getTableName();
    }

    public int getSize(){
        return this.accessor.getTableSize();
    }

    public int getMaxSize(){
        return this.accessor.getMaxTableSize();
    }

    public int getCountOfColumns(){
        return this.accessor.getCountOfColumns();
    }

    public String getKeyColumn(){
        return accessor.getColumnName(accessor.getKeyColumn());
    }

    //=========================== SERVICE METHODS ===============================

    /**
     * gets record by simple order number in table in file
     * @param i
     * @return
     * @throws IOException
     */
    private String[] getRecord(int i) throws IOException {
        return this.accessor.getRecord(i);
    }

    /**
     * search ONE matching record by using primary key index
     * @param value
     * @return
     * @throws Exception
     */
    private String[] searchRecordByKeyColumn(String value) throws Exception {
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

    private List<String[]> searchRecordByIndex(int colNumber, String value) throws Exception {
        if(this.columnIndexes == null){
            throw new IllegalStateException("Can't use searching by index for non-indexed column");
        }
        int[] recordNumbers = this.columnIndexes[colNumber].get(value);
        List<String[]> results = new ArrayList<>();
        if(recordNumbers == null){
            return results;
        }
        for (int i = 0; i < recordNumbers.length; i++) {
            String[] crtRecord = getRecord(recordNumbers[i]);
            results.add(crtRecord);
        }
        return results;
    }

    /**
     * LINEAR search of matching records by number of column (not name)
     *
     * @param colNumber
     * @param value
     *
     * @param searchingLimit constrain for not freezing the system.
     *      Allows not to run through all records - only from 0 to searchingLimit
     *
     * @return
     * @throws IOException
     */
    private List<String[]> searchRecordLinear(int colNumber, String value, int searchingLimit) throws IOException {
        List<String[]> results = new ArrayList<>();
        if(searchingLimit < 1){
            searchingLimit = getSize();
        }
        for (int i = 0; i < searchingLimit; i++) {
            String[] crtRec = this.accessor.getRecord(i);
            if (crtRec[colNumber].toLowerCase().equals(value.toLowerCase())) {
                results.add(crtRec);
            }
        }
        return results;
    }

}
