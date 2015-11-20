package com.company;

import com.company.database_classes.SimpleDB;
import com.company.helpers.TableInfo;

import java.util.Arrays;
import java.util.Random;

public class Main {

    // random string
    public static String randomString(int maxLength){
        final int minLength = 1;
        if(maxLength < minLength){
            throw new IndexOutOfBoundsException("Max length less then min length");
        }

        char[] letters = {  '1','2','3','4','5','6','7','8','9','0',
                            'a','b','c','d','e','f','g','h','i','j',
                            'k','l','m','o','p','q','r','s','t','u',
                            'v','w','x','y','z',' ','-','\'','\"','\\'};

        Random rand = new Random();
        int length = minLength + rand.nextInt(maxLength - minLength);
        StringBuilder result = new StringBuilder();
        for(int i = 0;i < length;i++){
            result.append(letters[rand.nextInt(letters.length)]);
        }
        return result.toString();
    }

    /**
     * simulate values of set of records
     *
     * @param numberOfRecords
     * @param numberOfColumns
     * @param colSizes
     * @param startUniqIdFrom if you want to add records in existing database and you have to provide uniq ID from some offset
     * @return
     */
    public static String[][] generateArrayWithRecords(int numberOfRecords, int numberOfColumns, int[] colSizes, int startUniqIdFrom){
        String[][] result = new String[numberOfRecords][numberOfColumns];

        int uniqOffset = startUniqIdFrom;
        int uniqId = uniqOffset;
        for(int crtRec = 0;crtRec < numberOfRecords;crtRec++){
            if(crtRec < numberOfRecords - 1) {
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    if(crtCol == 0){
                        result[crtRec][crtCol] = String.valueOf(uniqId);
                    }else {
                        result[crtRec][crtCol] = randomString(colSizes[crtCol]);
                    }
                }
            }else{
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    if(crtCol == 0) {
                        result[crtRec][crtCol] = String.valueOf(uniqId);
                    }else{
                        result[crtRec][crtCol] = "IO";
                    }
                }
            }
            uniqId++;
        }
        return result;
    }

    public static void main(String[] args) {

        //========================= INITIALIZE THE TABLES ====================================

        TableInfo[] tables = new TableInfo[3];
        //table 1
        tables[0] = new TableInfo("student",1000000);
        tables[0].addColumn("student_id",7,true);
        tables[0].addColumn("student_name", 50, false);
        tables[0].addColumn("student_grade", 2, false);

        //table 2
        tables[1] = new TableInfo("teacher",1000000);
        tables[1].addColumn("teacher_id", 7, true);
        tables[1].addColumn("teacher_name", 50, false);
        tables[1].addColumn("teacher_room", 4, false);
        tables[1].addColumn("teacher_age", 3, false);

        //table 3
        tables[2] = new TableInfo("relation", 1000000);
        tables[2].addColumn("relation_id", 7, true);
        tables[2].addColumn("student_id", 7, false);
        tables[2].addColumn("teacher_id", 7, false);

        //=====================================================================================

        //============== INITIALIZE DB IF DOESN'T EXIST (OR JUST GET ACCESS)===================
        String dbName = "school_db";
        SimpleDB db = null;

        if(SimpleDB.exists(dbName)){
            db = new SimpleDB(dbName);
        }else{
            db = SimpleDB.createDB(dbName, tables);
        }

        //=====================================================================================

        //========================= WRITE RANDOM RECORDS ======================================
        //-----------------(don't forget to comment it after the first run)--------------------

        /**
         * HERE IS ADD PART
         */
        /*
        int uniqIdOffset = 0;
        String[][] records1 = generateArrayWithRecords(700000, tables[0].getColumnsCount(), tables[0].getColumnSizes(), uniqIdOffset);
        String[][] records2 = generateArrayWithRecords(700000, tables[1].getColumnsCount(), tables[1].getColumnSizes(), uniqIdOffset);
        String[][] records3 = generateArrayWithRecords(700000, tables[2].getColumnsCount(), tables[2].getColumnSizes(), uniqIdOffset);

        long startTime = System.nanoTime();
        db.addRecords(tables[0].getName(),records1);
        db.addRecords(tables[1].getName(),records2);
        db.addRecords(tables[2].getName(),records3);
        long endTime = System.nanoTime();

        System.out.println("Time: " + (endTime - startTime));
        */
        /**
         * HERE IS THE END OF EDDING PART
         */

        //=====================================================================================

        //======================= GET SOME DATABASE INFORMATION ===============================

        //number of table in db
        System.out.println("Number of tables in db: " + db.getTablesCount());

        //max number of record in table 1
        System.out.println(tables[0].getName() + ": max " + db.getTableMaxSize(tables[0].getName()) + ", crt " +
                                    db.getTableSize(tables[0].getName()));
        //max number of record in table 2
        System.out.println(tables[1].getName() + ": max " + db.getTableMaxSize(tables[1].getName()) + ", crt " +
                db.getTableSize(tables[1].getName()));
        //max number of record in table 3
        System.out.println(tables[2].getName() + ": max " + db.getTableMaxSize(tables[2].getName()) + ", crt " +
                db.getTableSize(tables[2].getName()));

        //-------------------------------------------------------------------------------------

        //=============================== SEARCHING RECORDS ===================================

        int crtTable = 0;
        String[] result = db.findRecords(tables[crtTable].getName(),
                tables[crtTable].getColumnNames()[0], "0", 0, true).get(0);

        System.out.println(Arrays.toString(result));
    }
}
