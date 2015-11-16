//package com.company;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.io.RandomAccessFile;
//
//import static org.junit.Assert.*;
//
///**
// * Created by ivan on 10.11.15.
// */
//public class StructuredFileBlockTest {
//
//    public static String filePath = "test/test.txt";
//    public static long[] segmentsBegins = new long[]{20,100};
//    public static long beginOfFile = 0;
//    public static long endOfFile = 200;
//    RandomAccessFile ra;
//
//    @Before
//    public void setUp() throws Exception {
//        ra = new RandomAccessFile();
//    }
//
//    @Test
//    public void testCreateStructuredFileBlock() throws Exception {
//        File f = new File(filePath);
//        if (f.exists()){
//            f.delete();
//        }
//        StructuredFileBlock sf = StructuredFileBlock.createStructuredFileBlock(ra, beginOfFile, endOfFile, segmentsBegins);
//        assertTrue(f.exists());
//    }
//
//    @Test
//    public void testGetCountOfBlocks() throws Exception {
//        StructuredFileBlock sf = new StructuredFileBlock(ra);
//        assertEquals(2,sf.getCountOfBlocks());
//    }
//
//
//    @Test
//    public void testGetBlockPosition() throws Exception {
//        StructuredFileBlock sf = new StructuredFileBlock(filePath);
//        assertEquals(segmentsBegins[0],sf.getBlockBeginPosition(0));
//        assertEquals(segmentsBegins[1],sf.getBlockBeginPosition(1));
//    }
//
//    @Test
//    public void testGetBlockEndPosition() throws Exception {
//        StructuredFileBlock sf = new StructuredFileBlock(filePath);
//        assertEquals(segmentsBegins[1],sf.getBlockEndPosition(0));
//        assertEquals(endOfFile,sf.getBlockEndPosition(1));
//    }
//
//    @Test
//    public void testGetEndOfFile() throws Exception {
//        StructuredFileBlock sf = new StructuredFileBlock(filePath);
//        assertEquals(endOfFile,sf.getEndOfFile());
//    }
//}