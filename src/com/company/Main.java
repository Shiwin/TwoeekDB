package com.company;

import java.io.File;
import java.io.RandomAccessFile;

public class Main {

    public static void main(String[] args) {
        String fileName = "test.ts";
        File file = new File(fileName);
        RandomAccessFile raf;

        long[][] sizes = new long[2][4];
        sizes[0][0] = 1000;
        sizes[0][1] = 3100;
        sizes[0][2] = 3200;
        sizes[0][3] = 2300;

        sizes[1][0] = 3000;
        sizes[1][1] = 3100;
        sizes[1][2] = 4200;
        sizes[1][3] = 1300;

        SimpleDB db = new SimpleDB(fileName);


    }
}
