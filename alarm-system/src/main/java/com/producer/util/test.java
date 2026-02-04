package com.producer.util;

public class test {
    private static String[] arr = null;
    private static String text = "haducmanh/1809/2004/lalalala";

    public static void main(String arg[]) {
        arr = text.split("/");
        System.out.println("Mảng: " + arr[0]);
    }
}
