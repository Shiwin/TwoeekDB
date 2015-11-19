package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;
import java.io.File;

public class Main {

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

    public static boolean generateFileWithRecords(String fileName, int numberOfRecords, int numberOfColumns, int[] colSizes) throws IOException {
        final String splitter = " ";

        File file = new File(fileName);
        if(file.exists()){
            return false;
        }
        file.createNewFile();

        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        for(int crtRec = 0;crtRec < numberOfRecords;crtRec++){
            StringBuilder sb = new StringBuilder();
            if(crtRec < numberOfRecords - 1) {
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    sb.append(randomString(colSizes[crtCol]));
                    sb.append(splitter);
                }
                sb.append("\n");
                raf.write(sb.toString().getBytes());
            }else{
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    sb.append("Hi");
                    sb.append(splitter);
                }
                sb.append("\n");
                raf.write(sb.toString().getBytes());
            }
        }
        raf.close();

        return true;
    }

    public static String[][] generateArrayWithRecords(int numberOfRecords, int numberOfColumns, int[] colSizes){
        String[][] result = new String[numberOfRecords][numberOfColumns];

        for(int crtRec = 0;crtRec < numberOfRecords;crtRec++){
            if(crtRec < numberOfRecords - 1) {
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    result[crtRec][crtCol] = randomString(colSizes[crtCol]);
                }
            }else{
                for (int crtCol = 0; crtCol < numberOfColumns; crtCol++) {
                    result[crtRec][crtCol] = "Hi";
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String dbName = "test";

        long[][] sizes = new long[2][4];
        sizes[0][0] = 10000000;
        sizes[0][1] = 31000000;
        sizes[0][2] = 32000000;
        sizes[0][3] = 23000000;

        sizes[1][0] = 30000000;
        sizes[1][1] = 31000000;
        sizes[1][2] = 42000000;
        sizes[1][3] = 13000000;

        //SimpleDB db = SimpleDB.createDB(dbName, sizes);
        SimpleDB db = new SimpleDB(dbName);

        String tableName = "students";
        String[] columns = {"id", "name", "grade"};
        int[] colSizes = {100, 800, 20};
        //db.initializeTable(0, tableName, columns, colSizes);

        Table table = db.getTable(0);

        try {
            System.out.println(Arrays.toString(table.findRecordLinear(0, "Hi")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write test records
/*        String[][] records = generateArrayWithRecords(10000, colSizes.length,colSizes);

        for(int i = 0;i < records.length;i++){
            try {
                table.addRecord(records[i]);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }*/
        //

//        try {
//            generateFileWithRecords("recs", 100, colSizes.length, colSizes);
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }
    }
}
