package com.company;

import com.company.file_access.StructureHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

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

    //-----------------------------------------------------------------------------------

    private File dbFile;
    private RandomAccessFile raf;
    private StructureHandler structure;
    private Table crtTable;
    private HashMap<String, Integer> tableNames;
    private boolean ready;

    private SimpleDB(File file, RandomAccessFile raf, StructureHandler structure){
        ready = false;
        this.dbFile = file;
        this.raf = raf;
        this.structure = structure;
        this.crtTable = null;
        this.tableNames = new HashMap<>();
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
        this.tableNames = new HashMap<>();
        getTableNames();

        this.crtTable = null;
        ready = true;
    }

    public void initializeTable(int tableNumber, String tableName, String[] colNames, int[] colSizes, int keyColumn){
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
            crtTable = new Table(accessor, keyIndex);
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return;
        }
        getTableNames();
    }

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

    public Table getTable(int tableNumber){
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
            crtTable = new Table(accessor, keyIndex);
            return crtTable;
        } catch (IOException e) {
            dbLogger.message(e.getMessage());
            return null;
        } catch (IllegalStateException estate){
            dbLogger.message(estate.getMessage());
            return null;
        }
    }

    public Table getTable(String tableName){
        Integer tableNumber = this.tableNames.get(tableName);
        if(tableNumber != null) {
            return getTable(tableNumber);
        }else{
            return null;
        }
    }

    public int getTablesCount(){
        return this.structure.countOfTables();
    }

    public boolean isReady(){
        return ready;
    }
}
