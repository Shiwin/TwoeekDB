package com.company;

import com.company.file_access.StructureHandler;
import java.io.File;
import java.io.IOException;
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



        StructureHandler handler = null;
        try {
            if (file.exists()) {
                raf = new RandomAccessFile(file, "rw");
                handler = new StructureHandler(raf);
            } else {
                raf = new RandomAccessFile(file, "rw");
                handler = StructureHandler.createStructureHandler(raf,sizes);
            }
        }catch (IOException e){
            System.out.println("lol1");
        }

        if(handler == null){
            System.out.println("lol2");
            return;
        }

        for (int i = 0; i < handler.countOfTables(); i++) {
            System.out.println(handler.getTableSize(i));
        }
        System.out.println();
        for (int i = 0; i < handler.countOfIndeces(); i++) {
            System.out.println(handler.getIndexSize(i));
        }
    }
}
