package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.StringTokenizer;

/**
 * Low-level class for getting deal with tables
 */
public class AccessTable {

    /*
        Header format:
        beginTable endTable beginTableData name name|size|name|size...
     */

    public static final int headerSize = 500;
    public static final String  terminal = "|";

    private long begin,end;
    private RandomAccessFile randomAccessFile;
    private StructuredFileBlock structuredFileBlock;

    /**
     *
     * @param ra
     * @param begin
     * @param end
     */
    public AccessTable(RandomAccessFile ra,long begin,long end) {
        this.randomAccessFile = ra;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Create table with the scheme
     * Scheme format: ['attribute_name|attribute_size',...]\n
     * attribute_size in chars
     * @param scheme
     */
    public static void createTable(RandomAccessFile ra, long begin,long end, String name , String[] scheme){
        try {
            if (ra.length()<end){
                throw new ArrayIndexOutOfBoundsException("A table edge out of the file");
            }
            StringBuilder header = new StringBuilder(headerSize);
            header.append(String.format("%10d %10d %10d %s",begin,end,begin+headerSize,name));
            for (String s:scheme){
                header.append(" ");
                header.append(s);
            }
            header.append("\n");
            header.setLength(headerSize);
            ra.seek(begin);
            ra.writeChars(header.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Table getTable(RandomAccessFile ra,long begin){
        try {
            ra.seek(begin);
            String header = ra.readLine();
            String[] atributes = header.split(" ");
            long end = Long.parseLong(atributes[1]);
            long begin_of_data = Long.parseLong(atributes[2]);
            String name = atributes[3];
            HashMap<String ,Long> scheme = decodeScheme(atributes[4]);
            return new Table(begin,end,begin_of_data,name,scheme);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, Long> decodeScheme(String scheme) {
        StringTokenizer st = new StringTokenizer(scheme,terminal);
        HashMap<String ,Long> result = new HashMap<>();
        while (st.hasMoreTokens()){
            String key = st.nextToken();
            Long value = Long.parseLong(st.nextToken());
        }
        return result;
    }

    // may be used in future
    // public void dropTable(String name){}

}
