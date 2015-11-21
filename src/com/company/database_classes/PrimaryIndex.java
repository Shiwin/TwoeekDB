package com.company.database_classes;

import com.company.file_access.TableAccess;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * provides constant access by primary key
 */
public class PrimaryIndex extends Index {

    //================================ STATIC ========================================

    private static final int SIZE_OF_INTEGER = 7;       // max size of position value (in case of integer primary key)
    public static int getSizeOfIntegerValue(){
        return SIZE_OF_INTEGER;
    }

    //================================================================================

    /**
     *
     * @param raf
     * @param startPosition start position of this index in file
     * @param endPosition end position of this index in file
     * @param tableAccessor gives information about the table itself
     * @param shouldBeInit shows if index should be initialized at the first time or it is already in the file
     *                     WARNING: initialization delete all index and run through all of hashtable to clear it
     * @throws IOException
     */
    PrimaryIndex(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, boolean shouldBeInit) throws IOException {
        super(raf, startPosition, endPosition,tableAccessor, tableAccessor.getKeyColumn(), SIZE_OF_INTEGER, 1, shouldBeInit);
    }

}
