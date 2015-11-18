package com.company;

import java.util.Arrays;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        String dbName = "test";

        long[][] sizes = new long[2][4];
        sizes[0][0] = 10000;
        sizes[0][1] = 31000;
        sizes[0][2] = 32000;
        sizes[0][3] = 23000;

        sizes[1][0] = 30000;
        sizes[1][1] = 31000;
        sizes[1][2] = 42000;
        sizes[1][3] = 13000;

        //SimpleDB db = SimpleDB.createDB(dbName, sizes);
        SimpleDB db = new SimpleDB(dbName);

        String tableName = "students";
        String[] columns = {"id", "name", "grade"};
        int[] colSizes = {100, 800, 20};
        //db.initializeTable(0, tableName, columns, colSizes);

        String[] values = {"1", "Sergey", "B"};

        //db.addRecord(0, values);
        System.out.println(Arrays.toString(db.getRecord(0, 0)));
    }
}
