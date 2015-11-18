package com.company;

import com.company.file_access.StructureHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SimpleDB {

    private static String dbFolder = "database";
    private static String dbExtention = "db";
    private static Logger dbLogger = new Logger();

    public static String getDbFolder(){
        return dbFolder;
    }

    public static void setDBFolder(String dbFolder){
        SimpleDB.dbFolder = dbFolder;
    }

    public static SimpleDB createDB(String name, long[][] tablesAndIndexesSizes){
        File folder = new File(dbFolder);
        if(!folder.exists()){
            folder.mkdir();
        }
        if(!folder.isDirectory()){
            folder.mkdir();
        }

        String path = dbFolder + "\\" + name + "." + dbExtention;
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
            structure = StructureHandler.createStructureHandler(raf, tablesAndIndexesSizes);
        } catch (IOException e) {
            dbLogger.message("Can't create structure of file " + path);
            return null;
        }

        return new SimpleDB(dbFile, raf, structure);
    }

    private File dbFile;
    private RandomAccessFile raf;
    private StructureHandler structure;
    private TableAccess tableAccessor;
    private boolean ready;

    private SimpleDB(File file, RandomAccessFile raf, StructureHandler structure){
        ready = false;
        this.dbFile = file;
        this.raf = raf;
        this.structure = structure;
        ready = true;
    }

    public SimpleDB(String name){
        ready = false;
        File dbFile = new File(dbFolder + "\\" + name + "." + dbExtention);
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
        this.tableAccessor = null;
        ready = true;
    }

    public void initializeTable(int tableNumber, String tableName, String[] colNames, int[] colSizes){
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
            this.tableAccessor = TableAccess.createTableAccess(raf,startEnd[0], startEnd[1], tableName, colNames, colSizes);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return;
        }
    }

    private TableAccess getAccessor(int tableNumber){
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
            return new TableAccess(raf, startEnd[0], startEnd[1]);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return null;
        } catch (IllegalStateException estate){
            dbLogger.message(estate.getMessage());
            return null;
        }
    }

    public String getTableName(int tableNumber){
        this.tableAccessor = getAccessor(tableNumber);
        if(this.tableAccessor == null){
            dbLogger.message("tableAccessor isn't initialized. Can't get table name");
            return null;
        }

        return tableAccessor.getTableName();
    }

    public int getTableSize(int tableNumber){
        this.tableAccessor = getAccessor(tableNumber);
        if(this.tableAccessor == null){
            dbLogger.message("tableAccessor isn't initialized. Can't get table size");
            return -1;
        }

        return tableAccessor.getTableSize();
    }

    public int getTablesCount(){
        return this.structure.countOfTables();
    }

    //test
    public void addRecord(int tableNumber, String[] values){
        this.tableAccessor = getAccessor(tableNumber);
        if(this.tableAccessor == null){
            dbLogger.message("tableAccessor isn't initialized. Can't add record");
            return;
        }

        try {
            this.tableAccessor.addRecord(values);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
        }
    }

    public String[] getRecord(int tableNumber, int recordNumber){
        this.tableAccessor = getAccessor(tableNumber);
        if(this.tableAccessor == null){
            dbLogger.message("tableAccessor isn't initialized. Can't get record");
            return null;
        }

        try {
            return this.tableAccessor.getRecord(recordNumber);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
        }
        return null;
    }
    //

    public boolean isReady(){
        return ready;
    }
}
