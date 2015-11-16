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

    private SimpleDB(File file, RandomAccessFile raf, StructureHandler structure){
        this.dbFile = file;
        this.raf = raf;
        this.structure = structure;
    }

    public SimpleDB(String name){
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
    }

}
