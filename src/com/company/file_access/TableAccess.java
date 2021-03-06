package com.company.file_access;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class TableAccess {

    private static final String SPLITTER = " ";
    private static final String HEADER_END = "\n";

    private static long headerSize = 1000;

    public static long getHeaderSize(){
        return headerSize;
    }

    public static TableAccess createTableAccess(RandomAccessFile raf, long startPosition, long endPosition, String tableName, String[] colNames, int[] colSize, int keyColumn, int nextId) throws IOException {
        if(raf == null){
            throw new NullPointerException("raf is null");
        }

        if(tableName == null){
            throw new NullPointerException("tableName is null");
        }
        if(colNames == null){
            throw new NullPointerException("colName is null");
        }
        if(colSize == null){
            throw new NullPointerException("colSize is null");
        }

        if(startPosition > endPosition){
            throw new IllegalArgumentException("startPosition must be less then endPosition");
        }

        if(raf.length() < startPosition){
            throw new IllegalArgumentException("length of file is less then startPosition");
        }

        if(colNames.length != colSize.length){
            throw new IllegalArgumentException("different tableSize of colNames and colSizes");
        }

        if(keyColumn >= colNames.length){
            throw new IllegalArgumentException("keyColumn must be less then number of columns");
        }

        int colNumber = colNames.length;

        updateHeaderInFile(raf, startPosition, endPosition, tableName, colNames, colSize, keyColumn, 0, nextId);

        return new TableAccess(raf, startPosition, endPosition, startPosition + headerSize, tableName, colNames, colSize, keyColumn);
    }

    private RandomAccessFile raf;
    private long startPosition;
    private long endPosition;
    private long dataStartPosition;
    private String tableName;
    private String[] colNames;
    private HashMap<String, Integer> colNamesHash;
    private int[] colSize;
    private int tableSize;
    private int keyColumn;
    private int nextId;

    private TableAccess(RandomAccessFile raf, long startPosition, long endPosition, long dataStartPosition, String tableName, String[] colNames, int[] colSize, int keyColumn){
        this.raf = raf;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.dataStartPosition = dataStartPosition;
        this.tableName = tableName;
        this.colNames = colNames;
        this.keyColumn = keyColumn;
        colNamesHash = initializeColNameHash(this.colNames);

        this.colSize = colSize;
    }

    private static HashMap<String, Integer> initializeColNameHash(String[] colNames){
        if (colNames == null) {
            return null;
        }
        HashMap<String, Integer> map = new HashMap<>();
        for(int i = 0; i < colNames.length;i++){
            map.put(colNames[i], i);
        }
        return map;
    }

    private static void updateHeaderInFile(RandomAccessFile raf, long startPosition, long endPosition, String tableName, String[] colNames, int[] colSize, int keyColumn, int size, int nextId) throws IOException {
        int colNumber = colNames.length;
        StringBuilder sb = new StringBuilder();
        sb.append(startPosition);
        sb.append(SPLITTER);
        sb.append(endPosition);
        sb.append(SPLITTER);
        sb.append(startPosition + headerSize);
        sb.append(SPLITTER);
        sb.append(tableName);
        sb.append(SPLITTER);
        sb.append(colNumber);
        sb.append(SPLITTER);
        sb.append(keyColumn);
        sb.append(SPLITTER);
        sb.append(size);
        sb.append(SPLITTER);
        sb.append(nextId);
        sb.append(SPLITTER);
        for(int i = 0; i < colNumber;i++){
            sb.append(colNames[i]);
            sb.append(SPLITTER);
            sb.append(colSize[i]);
            if(i < colNumber - 1) {
                sb.append(SPLITTER);
            }
        }
        sb.append(HEADER_END);
        raf.seek(startPosition);
        raf.write(sb.toString().getBytes());
    }
    private void updateHeaderInFile() throws IOException {
        updateHeaderInFile(raf, startPosition, endPosition, tableName, colNames, colSize, keyColumn, tableSize, nextId);
    }

    private void parseHeader(String[] info){
        if(info == null){
            throw new NullPointerException("info is null");
        }
        int o = 0;
        try {
            this.startPosition = Integer.parseInt(info[o]);
            this.endPosition = Integer.parseInt(info[o + 1]);
            this.dataStartPosition = Integer.parseInt(info[o + 2]);
            this.tableName = info[o + 3];

            int colNumber = Integer.parseInt(info[o + 4]);
            this.keyColumn = Integer.parseInt(info[o + 5]);
            this.tableSize = Integer.parseInt(info[o + 6]);
            this.nextId = Integer.parseInt(info[0 + 7]);

            this.colNames = new String[colNumber];
            this.colSize = new int[colNumber];

            int prevOffset = o + 8;
            for (int i = 0; i < colNumber; i++) {
                this.colNames[i] = info[prevOffset + 2*i];
                this.colSize[i] = Integer.parseInt(info[prevOffset + 2*i + 1]);
            }


        }catch(Exception e){
            throw new IllegalStateException("It seems like table isn't initialized");
        }
    }

    public TableAccess(RandomAccessFile raf, long startPosition, long endPosition) throws IOException {
        if(raf == null){
            throw new NullPointerException("raf is null");
        }
        if(startPosition > endPosition){
            throw new IllegalArgumentException("startPosition must be less then endPosition");
        }
        if(raf.length() < startPosition){
            throw new IllegalArgumentException("length of file is less then startPosition");
        }

        this.raf = raf;
        raf.seek(startPosition);
        String header = raf.readLine();
        String[] info = header.split(SPLITTER);
        parseHeader(info);
        colNamesHash = initializeColNameHash(this.colNames);
    }

    private int getFullRecordLength(){
        int fullLength = 0;
        for(int i = 0;i < colSize.length;i++){
            fullLength += colSize[i];
        }
        return fullLength;
    }

    private long[] getCellStartEndPosition(int recordNumber, int colNumber){
        if(colNumber < 0 || colNumber > this.colNames.length){
            throw new IndexOutOfBoundsException("Wrong column number");
        }
        int fullLength = getFullRecordLength();

        long[] startEnd = new long[2];
        int offset = 0;
        if(colNumber > 0){
            for(int i = 0; i < colNumber;i++) {
                offset += colSize[i];
            }
        }
        startEnd[0] = dataStartPosition + (fullLength * recordNumber + offset);
        startEnd[1] = dataStartPosition + (fullLength * recordNumber + offset + colSize[colNumber]);
        return startEnd;
    }

    public void updateRecord(int recordNumber, String[] values) throws IOException {
        if(recordNumber >= tableSize){
            throw new IllegalArgumentException("There is no record with this number: " + recordNumber);
        }
        if(values.length != colNames.length){
            throw new IndexOutOfBoundsException("Wrong column number");
        }

        for(int i = 0; i < values.length;i++){
            if(values[i] != null) {
                updateColumnValue(recordNumber, i, values[i]);
            }
        }
    }

    public int addRecord(String[] values) throws IOException {
        if(values.length != colNames.length){
            throw new IndexOutOfBoundsException("Wrong column number");
        }

        for(int i = 0; i < values.length;i++){
            if(values[i] != null) {
                updateColumnValue(tableSize, i, values[i]);
            }
        }
        tableSize++;
        nextId++;
        updateHeaderInFile();
        return tableSize - 1;
    }

    public String[] getRecord(int recordNumber) throws IOException {
        if(recordNumber >= tableSize){
            throw new IllegalArgumentException("There is no record with this number: " + recordNumber);
        }

        String[] values = new String[colNames.length];
        for(int i = 0; i < values.length; i++){
            values[i] = getColumnValue(recordNumber, i);
        }
        return values;
    }

    public HashMap<String, Integer> getColNamesHash(){
        return colNamesHash;
    }

    private void updateColumnValue(int recordNumber, int colNumber, String value) throws IOException {
        if(value.length() > this.colSize[colNumber]){
            throw new IllegalArgumentException("length of value \'" + value +
                    "\' is bigger then it is allowed in colunm \'" + colNames[colNumber] + "\' (" + colSize[colNumber] + ")");
        }

        long[] startEnd = getCellStartEndPosition(recordNumber, colNumber);
        if(startEnd[1] > endPosition){
            throw new IllegalArgumentException("There is no record with this number: " + recordNumber);
        }

        raf.seek(startEnd[0]);
        raf.write(bufferString(startEnd[0], startEnd[1]).getBytes());
        raf.seek(startEnd[0]);
        raf.write(value.getBytes());
    }

    private String bufferString(long startPosition, long endPosition) throws IOException {
        StringBuffer sb = new StringBuffer();
        for(long i = startPosition;i < endPosition;i++){
            if(i < endPosition - 1) {
                sb.append(' ');
            }else{
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String getColumnValue(int recordNumber, int colNumber) throws IOException {
        if(colNumber < 0 || colNumber > this.colNames.length){
            throw new IndexOutOfBoundsException("Wrong column number");
        }
        long[] startEnd = getCellStartEndPosition(recordNumber, colNumber);
        if(startEnd[1] > endPosition){
            throw new IllegalArgumentException("There is no record with this number: " + recordNumber);
        }
        raf.seek(startEnd[0]);
        String value = raf.readLine().trim();
        return value;
    }

    public String getTableName() {
        return tableName;
    }

    public int getTableSize(){
        return tableSize;
    }

    public int getMaxTableSize(){
        long maxSize = (endPosition - startPosition) / getFullRecordLength();
        return (int)maxSize;
    }

    public int getCountOfColumns(){
        return this.colNames.length;
    }

    public String getColumnName(int columnNumber){
        if(columnNumber < 0 || columnNumber > colSize.length){
            throw new IndexOutOfBoundsException("ColumnNumber out of bounds");
        }

        return this.colNames[columnNumber];
    }

    public int getColumnSize(int columnNumber){
        if(columnNumber < 0 || columnNumber > colSize.length){
            throw new IndexOutOfBoundsException("ColumnNumber out of bounds");
        }

        return this.colSize[columnNumber];
    }

    public int getKeyColumn(){
        return keyColumn;
    }

    public int getNextId() {
        return nextId;
    }
}
