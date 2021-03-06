package com.company.database_classes;

import com.company.file_access.TableAccess;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ColumnIndex extends Index{

    ColumnIndex(RandomAccessFile raf, long startPosition, long endPosition, TableAccess tableAccessor, int columnNumber, int countOfValues, int countOfUniqWordsInRecordColumn, boolean shouldBeInit) throws IOException {
        super(raf, startPosition, endPosition, tableAccessor, columnNumber, countOfValues, countOfUniqWordsInRecordColumn, shouldBeInit);
    }
}
