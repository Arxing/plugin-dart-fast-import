package org.arxing;

public class Printer {

    public static void print(String format, Object... params){
        System.out.println(String.format(format, params));
    }
}
