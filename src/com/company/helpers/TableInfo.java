package com.company.helpers;

import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * Class-container for preparing tables. It is taken by SimpleDB to create a new database
 */
public class TableInfo {
    //============================= private fields ===================================
    private String name;
    private List<String> columns;           //names of columns
    private List<Integer> columnsSizes;     //sizes (== length) of each column
    private int columnsCount;               //number of columns (not less then 1)
    private int maxNumberOfRecords;         //max value of allowed records in future table
    private int numberOfKeyColumn;          //number of column which will be the primary key - MUST BE DECLARED
    //================================================================================

    public TableInfo(String name, int maxNumberOfRecords){
        if(name == null){
            throw new NullPointerException("name is null");
        }
        if(maxNumberOfRecords < 1){
            throw new IllegalArgumentException("max number of records must be greater then 0");
        }
        this.name = name;
        this.maxNumberOfRecords = maxNumberOfRecords;
        this.columns = new ArrayList<>();
        this.columnsSizes = new ArrayList<>();
        columnsCount = 0;
        this.numberOfKeyColumn = -1;
    }

    /**
     * NOT ready if:
     *      1. number of column less then 1
     *      2. different number of columns and size of column's list ans sizes' list (impossible situation, throws exception, if it happens)
     *      3. primary key isn't given
     * @return
     */
    public boolean isReady(){
        if(this.columnsCount < 1){
            return false;
        }

        if(this.columns.size() != this.columnsCount || this.columnsSizes.size() != this.columnsCount){
            throw new IllegalStateException("columnCount is different from same list values");
        }

        if(this.numberOfKeyColumn == -1){
            return false;
        }

        return true;
    }

    /**
     * Adds description of new column
     * @param columnName
     * @param columnSize size in letters
     * @param isPrimary if this column is primary key
     *                  ONLY ONE COLUMN CAN BE PRIMARY
     */
    public void addColumn(String columnName, int columnSize, boolean isPrimary){
        if(columnName == null){
            throw new NullPointerException("columnName is null");
        }
        if(columnSize < 2){
            throw new IllegalArgumentException("columnSize must be greater then 1");
        }
        if(numberOfKeyColumn != -1 && isPrimary){
            throw new IllegalStateException("table already has the key column");
        }

        this.columns.add(columnName);
        this.columnsSizes.add(columnSize + 1); // 1 - length of '\n' in the end of column
        this.columnsCount++;
        if(isPrimary){
            this.numberOfKeyColumn = this.columnsCount - 1;
        }
    }

    public String getName() {
        return name;
    }

    public int getColumnsCount() {
        return columnsCount;
    }

    public int getMaxNumberOfRecords() {
        return maxNumberOfRecords;
    }

    public Pair<String, Integer> getColumns(int number) {
        if(number < 0 || number > columnsCount){
            throw new IndexOutOfBoundsException("number not in bounds");
        }
        Pair<String, Integer> pair = new Pair<>(this.columns.get(number), this.columnsSizes.get(number));
        return pair;
    }

    //==================== METHODS ARE USED IN CREATING DATABASE ======================

    /**
     * @return the MAXIMUM length of one record of this table
     */
    public int getRecordLength(){
        int length = 0;
        for(int i = 0;i < this.columnsCount;i++){
            length += columnsSizes.get(i);
        }
        return length;
    }

    public int getKeyColumnSize(){
        return this.columnsSizes.get(this.numberOfKeyColumn);
    }

    public String[] getColumnNames() {
        String[] colNames = new String[this.columns.size()];
        for(int i = 0;i < colNames.length;i++){
            colNames[i] = this.columns.get(i);
        }
        return colNames;
    }

    public int[] getColumnSizes() {
        int[] colSizes = new int[this.columnsSizes.size()];
        for (int i = 0; i < colSizes.length; i++) {
            colSizes[i] = this.columnsSizes.get(i);
        }
        return colSizes;
    }

    public int getKeyColumnNumber() {
        return this.numberOfKeyColumn;
    }
}
