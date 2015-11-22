package com.company.database_classes;

import com.company.file_access.TableAccess;

import java.io.IOException;
import java.util.*;

/**
 * Class-interface for working with table inside the database
 */
public class Table {

    private static final int MIN_WORD_LENGTH = 3;

    //============================= private fields ===================================
    private TableAccess accessor;       // get access to the table according to given start/end positions in file
    private PrimaryIndex keyIndex;      // get access to the index according to given start/end positions in file

    private ColumnIndex[] columnIndexes;
    //================================================================================

    /**
     * Takes already defined accessor and keyIndex (they are defined in SimpleDB)
     *
     * @param accessor
     * @param keyIndex
     * @param columnIndexes
     */
    public Table(TableAccess accessor, PrimaryIndex keyIndex, ColumnIndex[] columnIndexes) {
        if (accessor == null) {
            return;
        }
        this.accessor = accessor;
        this.keyIndex = keyIndex;
        if (columnIndexes != null && columnIndexes.length != accessor.getCountOfColumns()) {
            throw new IllegalArgumentException("count of columnIndexes must be the same as count of columns themselves");
        }
        this.columnIndexes = columnIndexes;
    }

    /**
     * add record into table block by using accessor ...
     * ... and indexing this new value
     *
     * @param values
     * @throws Exception
     */
    public void addRecord(String[] values, int[] multiWordIndex) throws Exception {
        /**
         * checks if this key already in the table
         * BUT makes addition slowly
         *
         */
        /*
        int key = accessor.getKeyColumn();
        String[] keyAlreadyWas = this.searchRecordByKeyColumn(values[key]);
        if(keyAlreadyWas != null){
            throw new Exception("Record with this primary key has been already added.");
        }
        */
        int nextNumber = this.accessor.getTableSize();
        if (this.keyIndex != null) {
            this.keyIndex.put(values[accessor.getKeyColumn()], nextNumber);
        }
        if (columnIndexes != null) {
            for (int i = 0; i < columnIndexes.length; i++) {
                if (this.columnIndexes[i] != null) {
                    if (multiWordIndex[i] > 0) {
                        indexingAllWordsInValue(i, values[i], nextNumber);
                    } else {
                        this.columnIndexes[i].put(values[i], nextNumber);
                    }
                }
            }
        }
        int number = this.accessor.addRecord(values);
        if (nextNumber != number) {
            throw new Exception("Next number and previous size are different.");
        }
    }


/*
    private String[] getUniqWords(String text, int longerThen) {
        StringBuilder sb = new StringBuilder();
        sb.append(text);
        for (int i = 0; i < unsignificantChars.length; i++) {
            int del = sb.indexOf(String.valueOf(unsignificantChars[i]));
            while (del > -1) {
                sb.delete(del, del + 1);
                del = sb.indexOf(String.valueOf(unsignificantChars[i]));
            }
        }
        int del = sb.indexOf("  ");
        while (del > -1) {
            sb.replace(del, del + 2, " ");
            del = sb.indexOf("  ");
        }
        String[] result = sb.toString().split(" ");
        Set<String> list = new HashSet<>();
        for (int i = 0; i < result.length; i++) {
            if (result[i].length() > longerThen) {
                if (!list.contains(result[i]) && !Arrays.asList(ESCAPE_WORDS).contains(result[i])) {
                    list.add(result[i]);
                }
            }
        }
        Iterator<String> iterator = list.iterator();
        result = new String[list.size()];
        int i = 0;
        while (iterator.hasNext()) {
            String crtSting = iterator.next();
            result[i] = crtSting.toLowerCase();
            i++;
        }
        return result;
    }
*/

    private Set<Character> unsignificantChars = new LinkedHashSet<>();
    {unsignificantChars.add('!');unsignificantChars.add('@');unsignificantChars.add('#');unsignificantChars.add('$');
        unsignificantChars.add('%');unsignificantChars.add('^');unsignificantChars.add('&');unsignificantChars.add('*');
        unsignificantChars.add('(');unsignificantChars.add(')');unsignificantChars.add('=');unsignificantChars.add('+');
        unsignificantChars.add('|');unsignificantChars.add(']');unsignificantChars.add('}');unsignificantChars.add('[');
        unsignificantChars.add('{');unsignificantChars.add(':');unsignificantChars.add(';');unsignificantChars.add('\'');
        unsignificantChars.add('\"');unsignificantChars.add('?');unsignificantChars.add('\\');unsignificantChars.add('/');
        unsignificantChars.add(',');unsignificantChars.add('<');unsignificantChars.add('>');unsignificantChars.add('.');
        unsignificantChars.add('`');unsignificantChars.add('~');unsignificantChars.add(' ');
    }

    private static final Set<String> ESCAPE_WORDS = new LinkedHashSet<>();
    {ESCAPE_WORDS.add("about");ESCAPE_WORDS.add("above");ESCAPE_WORDS.add("across");ESCAPE_WORDS.add("after");
        ESCAPE_WORDS.add("against");ESCAPE_WORDS.add("along");ESCAPE_WORDS.add("among");ESCAPE_WORDS.add("around");
        ESCAPE_WORDS.add("as");ESCAPE_WORDS.add("aside");ESCAPE_WORDS.add("at");ESCAPE_WORDS.add("before");
        ESCAPE_WORDS.add("behind");ESCAPE_WORDS.add("below");ESCAPE_WORDS.add("between");ESCAPE_WORDS.add("beside");
        ESCAPE_WORDS.add("besides");ESCAPE_WORDS.add("beyond");ESCAPE_WORDS.add("but");ESCAPE_WORDS.add("by");
        ESCAPE_WORDS.add("despite");ESCAPE_WORDS.add("down");
        ESCAPE_WORDS.add("except"); ESCAPE_WORDS.add("for");ESCAPE_WORDS.add("from");ESCAPE_WORDS.add("given");
        ESCAPE_WORDS.add("in");ESCAPE_WORDS.add("inside");ESCAPE_WORDS.add("into");ESCAPE_WORDS.add("like");
        ESCAPE_WORDS.add("near");ESCAPE_WORDS.add("of");ESCAPE_WORDS.add("off");ESCAPE_WORDS.add("on");
        ESCAPE_WORDS.add("opposite");ESCAPE_WORDS.add("out");ESCAPE_WORDS.add("outside");ESCAPE_WORDS.add("over");
        ESCAPE_WORDS.add("per");ESCAPE_WORDS.add("since");ESCAPE_WORDS.add("than");ESCAPE_WORDS.add("through");
        ESCAPE_WORDS.add("till");ESCAPE_WORDS.add("to");ESCAPE_WORDS.add("under");ESCAPE_WORDS.add("unlike");
        ESCAPE_WORDS.add("until");ESCAPE_WORDS.add("up");ESCAPE_WORDS.add("with");ESCAPE_WORDS.add("without");
    }


    private Set<String> getUniqWords(String text, int longerThen){
        text = text.trim().toLowerCase();
        Set<String> set = new LinkedHashSet<>();
        StringBuilder crtWord = null;
        Character space = ' ';
        for (int i = 0; i < text.length(); i++) {
            char crtChar = text.charAt(i);

            if(!space.equals(crtChar)) {
                if(i == 0 || space.equals(text.charAt(i - 1))){
                    crtWord = new StringBuilder();
                    if(!unsignificantChars.contains(crtChar)) {
                        crtWord.append(crtChar);
                    }
                }else{
                    if(!unsignificantChars.contains(crtChar)) {
                        crtWord.append(crtChar);
                    }
                }
            }else{
                if(!space.equals(text.charAt(i - 1))){
                    String crt = crtWord.toString();
                    if(crt.length() > longerThen && !ESCAPE_WORDS.contains(crt)) {
                        set.add(crtWord.toString());
                    }
                }else{
                    //nothing
                }
            }
        }
        if(crtWord.length() != 0){
            String crt = crtWord.toString();
            if(crt.length() > longerThen && !ESCAPE_WORDS.contains(crt)) {
                set.add(crtWord.toString());
            }
        }
        return set;
    }

    private void indexingAllWordsInValue(int columnNumber, String value, int keyValue) throws Exception {
        Set<String> words = getUniqWords(value, MIN_WORD_LENGTH);
        Iterator<String> iterator = words.iterator();
        while (iterator.hasNext()){
            String crt = iterator.next();
            this.columnIndexes[columnNumber].put(crt, keyValue);
        }
    }

    /**
     * Search records by column with given value (exact matching)
     *
     * @param columnName
     * @param value
     * @param searchingLimit constrain for not freezing the system.
     *                       Allows not to run through all records - only from 0 to searchingLimit
     * @return
     * @throws Exception
     */
    public List<String[]> searchRecord(String columnName, String value, int searchingLimit) throws Exception {
        HashMap<String, Integer> columns = accessor.getColNamesHash();
        Integer colNumber = columns.get(columnName);
        if (colNumber == null) {
            return null;
        }

        List<String[]> result = null;
        if (colNumber == accessor.getKeyColumn()) {
            result = new ArrayList<>();
            String[] keyRecord = searchRecordByKeyColumn(value);
            if (keyRecord != null) {
                result.add(keyRecord);
            }
        } else {
            if (columnIndexes == null) {
                result = searchRecordLinear(colNumber, value, searchingLimit);
            } else {
                if (columnIndexes[colNumber] != null) {
                    result = searchRecordByIndex(colNumber, value);
                } else {
                    result = searchRecordLinear(colNumber, value, 0);
                }
            }
        }
        return result;
    }

    public String getName() {
        return this.accessor.getTableName();
    }

    public int getSize() {
        return this.accessor.getTableSize();
    }

    public int getMaxSize() {
        return this.accessor.getMaxTableSize();
    }

    public int getCountOfColumns() {
        return this.accessor.getCountOfColumns();
    }

    public String getKeyColumn() {
        return accessor.getColumnName(accessor.getKeyColumn());
    }

    //=========================== SERVICE METHODS ===============================

    /**
     * gets record by simple order number in table in file
     *
     * @param i
     * @return
     * @throws IOException
     */
    private String[] getRecord(int i) throws IOException {
        return this.accessor.getRecord(i);
    }

    /**
     * search ONE matching record by using primary key index
     *
     * @param value
     * @return
     * @throws Exception
     */
    private String[] searchRecordByKeyColumn(String value) throws Exception {
        String[] result = null;
        int[] recordNumbers = keyIndex.get(value);
        if (recordNumbers == null) {
            return null;
        }
        if (recordNumbers.length != 1) {
            throw new IllegalStateException("Index must keep only one value for primary key");
        }
        return getRecord(recordNumbers[0]);
    }



    private int[] getResultbyMultiWordIndex(int columnNumber,String value) throws Exception {
        Set<String> words = getUniqWords(value, MIN_WORD_LENGTH);
        Set<Integer> list = new HashSet<>();
        if(words == null || words.size() < 1){
            return null;
        }
        Iterator<String> iterator = words.iterator();
        String crt = iterator.next();
        int[] crtResult = this.columnIndexes[columnNumber].get(crt);
        for (int j = 0; j < crtResult.length; j++) {
            list.add(crtResult[j]);
        }

        while(iterator.hasNext()){
            crt = iterator.next();
            Set<Integer> local = new HashSet<>();
            crtResult = this.columnIndexes[columnNumber].get(crt);
            if(crtResult == null){
                continue;
            }
            for (int j = 0; j < crtResult.length; j++) {
                if(list.contains(crtResult[j])){
                    local.add(crtResult[j]);
                }
            }
            list = local;
        }

        Iterator<Integer> integerIterator = list.iterator();
        int i = 0;
        int[] result = new int[list.size()];
        while (integerIterator.hasNext()){
            Integer crtInt = integerIterator.next();
            result[i] = crtInt;
            i++;
        }
        return result;
    }

    private List<String[]> searchRecordByIndex(int colNumber, String value) throws Exception {
        if (this.columnIndexes == null) {
            throw new IllegalStateException("Can't use searching by index for non-indexed column");
        }

        int[] recordNumbers = null;
        if (this.columnIndexes[colNumber].getCountOfUniqWordsInRecordColumn() > 0) {
            recordNumbers = getResultbyMultiWordIndex(colNumber, value);

        } else {
            recordNumbers = this.columnIndexes[colNumber].get(value);
        }
        List<String[]> results = new ArrayList<>();
        if (recordNumbers == null) {
            return results;
        }
        for (int i = 0; i < recordNumbers.length; i++) {
            String[] crtRecord = getRecord(recordNumbers[i]);
            results.add(crtRecord);
        }
        return results;
    }

    /**
     * LINEAR search of matching records by number of column (not name)
     *
     * @param colNumber
     * @param value
     * @param searchingLimit constrain for not freezing the system.
     *                       Allows not to run through all records - only from 0 to searchingLimit
     * @return
     * @throws IOException
     */
    private List<String[]> searchRecordLinear(int colNumber, String value, int searchingLimit) throws IOException {
        List<String[]> results = new ArrayList<>();
        if (searchingLimit < 1) {
            searchingLimit = getSize();
        }
        for (int i = 0; i < searchingLimit; i++) {
            String[] crtRec = this.accessor.getRecord(i);
            if (crtRec[colNumber].toLowerCase().equals(value.toLowerCase())) {
                results.add(crtRec);
            }
        }
        return results;
    }

}
