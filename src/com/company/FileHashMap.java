package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileHashMap {

    private final int SIZE_OF_VALUE = 7;
    private final String FILLER = " ";
    private final String EMPTY_CELL = "\n";
    private final String CELL_END = "\n";

    RandomAccessFile raf;
    long startPosition;
    long endPosition;
    int sizeOfKey;
    int fullCellSize;
    int tableSize;
    int numberOfValues;

    FileHashMap(RandomAccessFile raf, long startPosition, long endPosition, int tableSize, int sizeOfKey, int numberOfValues, boolean allowedExtendIfPossible) throws IOException {
        if(raf == null){
            throw new NullPointerException("raf is null");
        }
        if(startPosition > endPosition){
            throw new IllegalArgumentException("startPosition must be less then endPosition");
        }
        if(raf.length() < endPosition){
            throw new IllegalArgumentException("raf length is less then endPosition");
        }

        this.raf = raf;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.sizeOfKey = sizeOfKey;
        this.numberOfValues = numberOfValues;
        this.fullCellSize = SIZE_OF_VALUE * this.numberOfValues + this.sizeOfKey + CELL_END.length();

        long expectedTableLength = this.fullCellSize * tableSize;

        if(expectedTableLength > endPosition - startPosition){
            throw new IllegalArgumentException("expected length of hash table is less then allocated space in file");
        }

        if(allowedExtendIfPossible){
            long availableSpace = endPosition - startPosition;
            long newTableSize = availableSpace / this.fullCellSize;
            expectedTableLength = this.fullCellSize * newTableSize;
            this.tableSize = (int)newTableSize;
        }else{
            this.tableSize = tableSize;
        }

        this.endPosition = this.startPosition + expectedTableLength;
    }

    public void initializeEmpty() throws IOException {
        for(int i = 0;i < tableSize;i++){
            long crtPosition = getCellPosition(i);
            raf.seek(crtPosition);
            raf.write(EMPTY_CELL.getBytes());
        }
    }

    public void clear() throws IOException {
        for(int i = 0;i < tableSize;i++){
            long crtPosition = getCellPosition(i);
            raf.seek(crtPosition);
            raf.write(emptyString(this.fullCellSize).getBytes());

            raf.seek(crtPosition);
            raf.write(EMPTY_CELL.getBytes());
        }
    }

    public boolean put(String key, int value) throws Exception {
        if(key == null){
            throw new NullPointerException("key is null");
        }
        if(key.length() > sizeOfKey){
            throw new IllegalArgumentException("length of key must be not greater then " + sizeOfKey);
        }

        int cellHash = hashFunction(key);
        int crtHash = cellHash;

        raf.seek(getCellPosition(crtHash));
        String cell = raf.readLine();
        if (cell.length() > EMPTY_CELL.length()) {
            String[] keyValues = parseKeyValue(cell);
            String crtKey = keyValues[0];
            if (key.equals(crtKey)) {
                writeValue(crtHash, keyValues.length - 1, String.valueOf(value));
                raf.seek(getCellLastPosition(crtHash));
                raf.write(CELL_END.getBytes());
                return true;
            }else{
                while(cell.length() > EMPTY_CELL.length() && crtHash < tableSize) {
                    crtHash++;
                    Long position = null;
                    try {
                        position = getCellPosition(crtHash);
                    }catch (IndexOutOfBoundsException e){
                        throw new IllegalStateException("Can't deal with collision");
                    }
                    raf.seek(position);
                    cell = raf.readLine();
                    if (cell.length() > EMPTY_CELL.length()) {
                        keyValues = parseKeyValue(cell);
                        crtKey = keyValues[0];
                        if (key.equals(crtKey)) {
                            writeValue(crtHash, keyValues.length - 1, String.valueOf(value));
                            raf.seek(getCellLastPosition(crtHash));
                            raf.write(CELL_END.getBytes());
                            return true;
                        }
                    }else{
                        writeKeyValueInEmptyCell(crtHash,key,value);
                        return true;
                    }
                }
            }
        }else{
            writeKeyValueInEmptyCell(crtHash,key,value);
            return true;
        }
        return false;
    }

    public int[] get(String key) throws Exception {
        int hash = hashFunction(key);

        String[] resultStrings = getKeyValueByHash(hash);
        while((resultStrings == null || !resultStrings[0].equals(key)) && hash < tableSize){
            hash++;
            resultStrings = getKeyValueByHash(hash);
        }
        if(resultStrings == null){
            return null;
        }
        int[] result = new int[resultStrings.length - 1];
        for(int i = 0;i < result.length;i++){
            result[i] = Integer.parseInt(resultStrings[i + 1]);
        }
        return result;
    }

    private long getCellPosition(int cellHash) {
        if (cellHash >= tableSize) {
            throw new IndexOutOfBoundsException("cellHash is out of table");
        }
        long cellPosition = this.startPosition;

        cellPosition += this.fullCellSize * cellHash;
        return cellPosition;
    }

    private void writeKeyValueInEmptyCell(int hash, String key, int value) throws IOException {
        raf.seek(getCellPosition(hash));
        raf.write(normalizeString(key, sizeOfKey).getBytes());
        raf.write(normalizeString(String.valueOf(value),SIZE_OF_VALUE).getBytes());
        raf.seek(getCellLastPosition(hash));
        raf.write(CELL_END.getBytes());
    }

    private long getCellLastPosition(int cellHash){
        long lastPosition = getCellPosition(cellHash);
        lastPosition += this.fullCellSize - 1;
        return lastPosition;
    }

    private String emptyString(int size){
        if(size < 1){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < size;i++){
            sb.append(FILLER);
        }
        String str = sb.toString();
        return str;
    }

    private String normalizeString(String string, int normalSize){
        if(string.length() > normalSize){
            throw new IllegalArgumentException("string is bigger then normal size");
        }
        int spaces = normalSize - string.length();
        StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(emptyString(spaces));
        return sb.toString();
    }

    private void writeValue(int cellHash, int valuesAlreadyThere, String value) throws IOException {
        if(value.length() > this.SIZE_OF_VALUE){
            throw new IllegalArgumentException("value size is greater then it is allowed");
        }
        long cellPosition = getCellPosition(cellHash);
        long nextCellPosition = -1;
        if(cellHash < tableSize - 1) {
            nextCellPosition = getCellPosition(cellHash + 1);
        }else{
            nextCellPosition = this.endPosition;
        }
        long valuePosition = cellPosition + sizeOfKey + SIZE_OF_VALUE * valuesAlreadyThere;
        if(valuePosition + value.length() >= nextCellPosition){
            throw new IllegalStateException("no more space for values of this key");
        }

        raf.seek(valuePosition);
        raf.write(normalizeString(value, SIZE_OF_VALUE).getBytes());
    }

    private int hashLy(String key){
        int hash = 0;
        for(int i = 0; i < key.length();i++){
            hash = (hash * 1664525) + Character.valueOf(key.charAt(i)).hashCode() + 1013904223 + key.hashCode();
        }
        return Math.abs(hash) % tableSize;
    }

    private int hashRs(String key){
        int b = 378551;
        int a = 63689;
        int hash = 0;
        for(int i = 0; i < key.length();i++){
            hash = hash * a + Character.valueOf(key.charAt(i)).hashCode() + key.hashCode();
            a *= b;
        }
        return Math.abs(hash) % tableSize;
    }

    private int hashFunction(String key){
        return hashLy(key);
    }

    private String[] getKeyValueByHash(int cellHash) throws Exception {
        raf.seek(getCellPosition(cellHash));
        String fullCell = raf.readLine();
        if(fullCell.length() <= EMPTY_CELL.length()){
            return null;
        }

        String[] keyValue = parseKeyValue(fullCell);
        return keyValue;
    }

    private String[] parseKeyValue(String cellValue) throws Exception {
        List<String> listKeyValue = new ArrayList<>();
        //String[] keyValues = new String[this.numberOfValues + 1];

        listKeyValue.add(cellValue.substring(0, sizeOfKey).trim());

        for(int i = 0; i < this.numberOfValues;i++){
            int from = sizeOfKey + i * SIZE_OF_VALUE;
            int to = from + SIZE_OF_VALUE;
            String substr = cellValue.substring(from, to);
            substr = substr.trim();
            try {
                long testLong = Integer.parseInt(substr);
            }catch (Exception e){
                break;
            }
            listKeyValue.add(substr);
        }
        String[] keyValues = new String[listKeyValue.size()];
        for(int i = 0;i < keyValues.length;i++){
            keyValues[i] = listKeyValue.get(i);
        }
        return keyValues;
    }

    public int getTableSize() {
        return tableSize;
    }
}
