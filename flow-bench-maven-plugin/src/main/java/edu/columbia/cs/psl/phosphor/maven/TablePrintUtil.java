package edu.columbia.cs.psl.phosphor.maven;

public class TablePrintUtil {
    public static void printTable(String title, String[] columnNames, Object[][] data) {
        String[][] dataStrings = new String[data.length][columnNames.length];
        int[] maxWidths = new int[columnNames.length];
        for(int j = 0; j < columnNames.length; j++) {
            maxWidths[j] = columnNames[j].length();
        }
        for(int i = 0; i < data.length; i++) {
            if(data[i].length != columnNames.length) {
                throw new IllegalArgumentException("Data matrix width does not equal column names length");
            }
            for(int j = 0; j < data[i].length; j++) {
                dataStrings[i][j] = data[i][j].toString();
                maxWidths[j] = Math.max(maxWidths[j], dataStrings[i][j].length());
            }
        }
        int totalLength = maxWidths.length - 1;
        for(int width : maxWidths) {
            totalLength += width;
        }
        String divider = divider(maxWidths, false);
        System.out.println(divider(maxWidths, true));
        System.out.println("|" + padStringCenter(title, totalLength, ' ') + "|");
        System.out.println(divider);
        System.out.println(getRowString(columnNames, maxWidths, ' ', true));
        System.out.println(divider);
        for(int i = 0; i < dataStrings.length; i++) {
            System.out.println(getRowString(dataStrings[i], maxWidths, ' ', false));
        }
        System.out.println(divider);
    }

    private static String getRowString(String[] data, int[] maxWidths, char padding, boolean center) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < data.length; i ++) {
            builder.append('|').append(center ? padStringCenter(data[i], maxWidths[i], padding) :  padStringRight(data[i], maxWidths[i], padding));
        }
        return builder.append('|').toString();
    }

    private static String padStringCenter(String s, int width, char padding) {
        if(s.length() > width) {
            throw new IllegalArgumentException();
        }
        int extra = width - s.length();
        int left = (extra + 1)/2;
        int right = extra/2;
        return repeat(padding, left) + s + repeat(padding, right);
    }

    private static String padStringRight(String s, int width, char padding) {
        if(s.length() > width) {
            throw new IllegalArgumentException();
        }
        int extra = width - s.length();
        return repeat(padding, extra) + s;
    }

    private static String repeat(char c, int length) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    private static String divider(int[] maxWidths, boolean top) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(int width : maxWidths) {
            builder.append(top && !first ? '-' : '+').append(repeat('-', width));
            first = false;
        }
        return builder.append('+').toString();
    }
}
