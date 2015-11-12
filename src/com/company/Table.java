package com.company;

import java.util.HashMap;

/**
 * Table
 */
public class Table {

    private HashMap<String, Long> scheme;
    private long begin,end,begin_of_data;
    private String name;

    public Table(Long begin, Long end, long begin_of_data, String name, HashMap<String, Long> scheme) {
        this.begin = begin;
        this.begin_of_data = begin_of_data;
        this.end = end;
        this.name = name;
        this.scheme = scheme;
    }


}
