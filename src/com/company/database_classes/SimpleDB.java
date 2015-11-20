package com.company.database_classes;

import com.company.helpers.Logger;
import com.company.helpers.TableInfo;
import com.company.file_access.StructureHandler;
import com.company.file_access.TableAccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleDB {

    private static String dbFolder = "database";
    private static String dbExtension = "db";
    private static Logger dbLogger = new Logger();


    //========================= USER STATIC METHODS ==================================
    public static boolean exists(String dbName){
        File file = new File(dbFolder + "\\" + dbName + "." + dbExtension);
        return file.exists();
    }
    public static String getDbFolder(){
        return dbFolder;
    }
    public static void setDBFolder(String dbFolder){
        SimpleDB.dbFolder = dbFolder;
    }

    public static SimpleDB createDB(String name, TableInfo[] tablesDescription){
        if(name == null){
            throw new NullPointerException("name of db is null");
        }
        if(tablesDescription == null){
            throw new NullPointerException("tables description is null");
        }

        for (int i = 0; i < tablesDescription.length; i++) {
            if(!tablesDescription[i].isReady()){
                throw new IllegalArgumentException("Table " + tablesDescription[i].getName() + " isn't ready for initialization." +
                        "Check for enough number of columns or correctly set primary key");
            }
        }

        File folder = new File(dbFolder);
        if(!folder.exists()){
            folder.mkdir();
        }
        if(!folder.isDirectory()){
            folder.mkdir();
        }

        String path = dbFolder + "\\" + name + "." + dbExtension;
        File dbFile = new File(path);
        if(dbFile.exists()){
            dbLogger.message("Database with this name already exists");
            return null;
        }
        try {
            dbFile.createNewFile();
        } catch (IOException e) {
            dbLogger.message("Can't create file " + path);
            return null;
        }

        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(dbFile, "rw");
        } catch (FileNotFoundException e) {
            dbLogger.message("Can't create RandomAccessFile for " + path);
            return null;
        }
        StructureHandler structure = null;
        try {
            structure = StructureHandler.createStructureHandler(raf, calculateFileSizes(tablesDescription));
        } catch (IOException e) {
            dbLogger.message("Can't create structure of file " + path);
            return null;
        }

        SimpleDB crtDB = new SimpleDB(dbFile, raf, structure);
        for(int i = 0; i < tablesDescription.length;i++) {
            crtDB.initializeTable(i,tablesDescription[i].getName(),tablesDescription[i].getColumnNames(),
                    tablesDescription[i].getColumnSizes(),tablesDescription[i].getKeyColumnNumber());
        }

        return crtDB;
    }
    //================================================================================

    //========================== SERVICE STATIC METHODS ==============================
    private static long[][] calculateFileSizes(TableInfo[] tableInfos){
        long[][] tableAndIndexesSizes = new long[2][];
        for(int i = 0; i < tableAndIndexesSizes.length;i++){
            tableAndIndexesSizes[i] = new long[tableInfos.length];
        }

        long header = TableAccess.getHeaderSize();
        for(int crtTable = 0; crtTable < tableAndIndexesSizes[0].length;crtTable++){
            int maxRecords = tableInfos[crtTable].getMaxNumberOfRecords();
            tableAndIndexesSizes[0][crtTable] = header + tableInfos[crtTable].getRecordLength() * maxRecords;
        }

        double indexFactor = 1.5;
        int coumnOfValues = 1;
        for(int crtIndex = 0;crtIndex < tableAndIndexesSizes[1].length;crtIndex++){
            tableAndIndexesSizes[1][crtIndex] = (long)(((PrimaryIndex.getSizeOfIntegerValue() * coumnOfValues
                    + tableInfos[crtIndex].getKeyColumnSize()) * tableInfos[crtIndex].getMaxNumberOfRecords()) * indexFactor);
        }

        return tableAndIndexesSizes;
    }
    //================================================================================

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //============================= private fields ===================================
    private File dbFile;
    private RandomAccessFile raf;                   // raf does all job with file
    private StructureHandler structure;             // structure knows positions (start/end) of eny block (tables, indexes) ...
                                                    // ... or any exact table or index
    private HashMap<String, Integer> tableNames;    // allows get access to the table by name (not only by order number)
    private boolean ready;                          // check if database is ready or not for work
    //================================================================================

    /**
     * get access to database if it already exists. Otherwise does nothing (only message in log)
     * @param name
     */
    public SimpleDB(String name){
        ready = false;
        File dbFile = new File(dbFolder + "\\" + name + "." + dbExtension);
        if(!dbFile.exists()){
            dbLogger.message("DB doesn't exist. Opening is canceled");
            return;
        }

        this.dbFile = dbFile;
        try {
            this.raf = new RandomAccessFile(this.dbFile, "rw");
        } catch (FileNotFoundException e) {
            dbLogger.message("RandomAccessFile can't find the file. Opening is canceled");
            return;
        }
        this.structure = new StructureHandler(raf);
        this.tableNames = new HashMap<>();
        getTableNames();
        ready = true;
    }

    /**
     * Adds a RANGE of records
     * @param tableName
     * @param records The first dimension ([this][]) - the whole record
     *                the second dimension ([][this]) - each column value in exact record
     */
    public void addRecords(String tableName, String[][] records){
        if(tableName == null){
            throw new NullPointerException("tableName is null");
        }
        if(records == null){
            throw new NullPointerException("records is null");
        }

        Table table = getTable(tableName);
        if(table == null){
            dbLogger.message("Can't find table with name " + tableName);
            return;
        }

        for (int i = 0; i < records.length; i++) {
            String[] crtRecord = records[i];
            if(crtRecord.length != table.getCountOfColumns()){
                throw new IllegalArgumentException("wrong number of column in record " + i);
            }

            try {
                table.addRecord(crtRecord);
            } catch (Exception e) {
                dbLogger.message("Can't add record: " + e.getMessage());
            }
        }
    }

    /**
     * Search for records:
     *
     *      FROM tableName
     *      WHERE [columnName]=value
     *
     * @param tableName
     * @param columnName
     * @param value
     * @param searchingLimit constrain for not freezing the system.
     *      Allows not to run through all records - only from 0 to searchingLimit
     *
     * @param exactlyMatches if true - search matches of full value in column
     *          if false - search for substrings (not implemented)
     *
     * @return list of matching records
     */
    public List<String[]> findRecords(String tableName, String columnName, String value, int searchingLimit, boolean exactlyMatches){
        if(tableName == null){
            throw new NullPointerException("tableName is null");
        }
        if(columnName == null){
            throw new NullPointerException("columnName is null");
        }
        if(value == null){
            throw new NullPointerException("value is null");
        }

        Table table = getTable(tableName);
        if(table == null){
            dbLogger.message("Can't find table with name " + tableName);
            return null;
        }

        List<String[]> results = new ArrayList<>();
        if(exactlyMatches){
            try {
                results = table.searchRecord(columnName,value, searchingLimit);
            } catch (Exception e) {
                dbLogger.message("Can't search record" + e.getMessage());
            }
        }else{
            // TODO implement
        }
        return results;
    }

    /**
     * @param tableName
     * @return number of records in table
     */
    public int getTableSize(String tableName){
        if(tableName == null){
            throw new NullPointerException("tableName is null");
        }
        Table table = getTable(tableName);
        if(table == null){
            dbLogger.message("Can't find table with name " + tableName);
            return -1;
        }
        return table.getSize();
    }

    /**
     * @param tableName
     * @return max allowed number of records in table
     */
    public int getTableMaxSize(String tableName){
        if(tableName == null){
            throw new NullPointerException("tableName is null");
        }
        Table table = getTable(tableName);
        if(table == null){
            dbLogger.message("Can't find table with name " + tableName);
            return -1;
        }
        return table.getMaxSize();
    }

    /**
     * @return number of records
     */
    public int getTablesCount(){
        return this.structure.countOfTables();
    }
    //===========================================================================

    //=========================== SERVICE METHODS ===============================

    /**
     * Constructor for creating a new database. Used in static method createDB()
     * @param file
     * @param raf
     * @param structure
     */
    private SimpleDB(File file, RandomAccessFile raf, StructureHandler structure){
        ready = false;
        this.dbFile = file;
        this.raf = raf;
        this.structure = structure;
        this.tableNames = new HashMap<>();
        ready = true;
    }

    /**
     * Initialize table with given parameters
     * @param tableNumber
     * @param tableName
     * @param colNames
     * @param colSizes
     * @param keyColumn
     */
    private void initializeTable(int tableNumber, String tableName, String[] colNames, int[] colSizes, int keyColumn){
        if(!isReady()){
            dbLogger.message("Database isn't created");
            return;
        }
        if(tableNumber < 0 && tableNumber >= structure.countOfTables()){
            dbLogger.message("No table with this number. Initialization was stopped");
            return;
        }

        long[] startEnd = null;
        try {
            startEnd = structure.getTableStartEnd(tableNumber);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return;
        }
        try {
            TableAccess accessor = TableAccess.createTableAccess(raf,startEnd[0], startEnd[1], tableName, colNames, colSizes, keyColumn);
            long[] indexStartEnd = structure.getIndexStartEnd(tableNumber);
            PrimaryIndex keyIndex = new PrimaryIndex(raf, indexStartEnd[0], indexStartEnd[1], accessor,true);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return;
        }
        getTableNames();
    }

    /**
     * set inner private field tableNames for getting access to database by name (not only by number)
     */
    private void getTableNames(){
        int count = getTablesCount();
        for(int i = 0; i < count;i++){
            TableAccess accessor = null;
            long[] startEnd = new long[0];
            try {
                startEnd = structure.getTableStartEnd(i);
            } catch (IOException e) {
                dbLogger.message(e.getMessage());
            }
            try {
                accessor = new TableAccess(raf, startEnd[0],startEnd[1]);
            } catch (Exception e) {
                // skip uninitialized table
            }
            if(accessor != null){
                tableNames.put(accessor.getTableName(), i);
            }
        }
    }

    /**
     * Get table by number
     * @param tableNumber
     * @return
     */
    private Table getTable(int tableNumber){
        if(!isReady()){
            throw new IllegalStateException("Database isn't created");
        }
        if(tableNumber < 0 && tableNumber >= structure.countOfTables()){
            throw new IllegalStateException("No table with this number. Initialization was stopped");
        }
        long[] startEnd = null;
        try {
            startEnd = structure.getTableStartEnd(tableNumber);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return null;
        }
        try {
            long[] indexStartEnd = structure.getIndexStartEnd(tableNumber);
            TableAccess accessor = new TableAccess(raf, startEnd[0], startEnd[1]);
            PrimaryIndex keyIndex = new PrimaryIndex(raf, indexStartEnd[0], indexStartEnd[1], accessor, false);
            Table crtTable = new Table(accessor, keyIndex);
            return crtTable;
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return null;
        } catch (IllegalStateException estate){
            dbLogger.message(estate.getMessage());
            return null;
        }
    }

    /**
     * Get table by name. Can be used as public, if it is necessary
     * @param tableName
     * @return
     */
    private Table getTable(String tableName){
        Integer tableNumber = this.tableNames.get(tableName);
        if(tableNumber != null) {
            return getTable(tableNumber);
        }else{
            return null;
        }
    }

    /**
     * check if database is ready or not for work
     * @return
     */
    public boolean isReady(){
        return ready;
    }
    //===========================================================================
}