package com.company;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by ivan on 10.11.15.
 */
public class StructuredFileTest {

    public static String filePath = "test/test.txt";
    public static long[] segmentsBegins = new long[]{20,100};
    public static long endOfFile = 200;

    @Test
    public void testCreateStructuredFile() throws Exception {
        File f = new File(filePath);
        if (f.exists()){
            f.delete();
        }
        StructuredFile sf = StructuredFile.createStructuredFile(filePath,segmentsBegins,endOfFile);
        assertTrue(f.exists());
    }

    @Test
    public void testGetCountOfBlocks() throws Exception {
        StructuredFile sf = new StructuredFile(filePath);
        assertEquals(2,sf.getCountOfBlocks());
    }

    @Test
    public void testGetBlockPosition() throws Exception {
        StructuredFile sf = new StructuredFile(filePath);
        assertEquals(segmentsBegins[0],sf.getBlockBeginPosition(0));
        assertEquals(segmentsBegins[1],sf.getBlockBeginPosition(1));
    }

    @Test
    public void testGetBlockEndPosition() throws Exception {
        StructuredFile sf = new StructuredFile(filePath);
        assertEquals(segmentsBegins[1],sf.getBlockEndPosition(0));
        assertEquals(endOfFile,sf.getBlockEndPosition(1));
    }

    @Test
    public void testGetEndOfFile() throws Exception {
        StructuredFile sf = new StructuredFile(filePath);
        assertEquals(endOfFile,sf.getEndOfFile());
    }
}