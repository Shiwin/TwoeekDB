package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

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

        WrapFile wrap = null;
        try {
            if (file.exists()) {
                raf = new RandomAccessFile(file, "rw");
                wrap = new WrapFile(raf);
            } else {
                raf = new RandomAccessFile(file, "rw");
                wrap = WrapFile.createWrapFile(raf,sizes);
            }
        }catch (IOException e){
            System.out.println("lol1");
        }

        if(wrap == null){
            System.out.println("lol2");
            return;
        }

        System.out.println(Arrays.toString(wrap.getTableStartEnd(2)));
    }
}
