package edu.neu.ccs.conflux.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GroupedTable {
    private String title = "";
    private final List<Group> groups = new ArrayList<>();
    private final List<Object[][]> rows = new ArrayList<>();

    public GroupedTable(String title) {
        this.title = title;
    }

    public GroupedTable addRow(Object[]... dataGroups) {
        if(dataGroups == null || dataGroups.length == 0) {
            throw new IllegalArgumentException();
        }
        rows.add(dataGroups);
        return this;
    }

    public GroupedTable addGroup(String name, String... headers) {
        if(name == null) {
            name = "";
        }
        if(headers == null || headers.length == 0) {
            throw new IllegalArgumentException();
        }
        groups.add(new Group(name, headers));
        return this;
    }

    public void printToStream(PrintStream stream) {
        validate();
        String[][][] rowStrings = createRowStrings();
        int[][] colWidths = calculateColumnWidths(rowStrings);
        int[] groupWidths = calculateGroupWidths(colWidths);
        int tableWidth = calculateTableWidth(groupWidths);
        stream.println(divider(groupWidths, true));
        stream.println('|' + padStringCenter(title, tableWidth - 2, ' ') + '|');
        stream.println(divider(groupWidths, false));
        stream.println('|' + createElementString(getGroupNames(), groupWidths, '|', true)+ '|');
        stream.println('|' + createElementString(getGroupHeaderStrings(colWidths), groupWidths, '|', true)+ '|');
        stream.println(divider(groupWidths, false));
        for(String[][] row : rowStrings) {
            stream.println('|' + createElementString(getRowGroups(row, colWidths), groupWidths, '|', true)+ '|');
        }
        stream.println(divider(groupWidths, false));
    }

    private String[] getRowGroups(String[][] row, int[][] colWidths) {
        String[] rowGroups = new String[groups.size()];
        for(int i = 0; i < rowGroups.length; i++) {
            rowGroups[i] = createElementString(row[i], colWidths[i], ' ', false);
        }
        return rowGroups;
    }

    private String[] getGroupNames() {
        String[] names = new String[groups.size()];
        for(int i = 0; i < names.length; i++) {
            names[i] = groups.get(i).name;
        }
        return names;
    }

    private String[] getGroupHeaderStrings(int[][] colWidths) {
        String[] headerStrings = new String[groups.size()];
        for(int i = 0; i < headerStrings.length; i++) {
            headerStrings[i] = createElementString(groups.get(i).headers, colWidths[i], ' ', true);
        }
        return headerStrings;
    }

    private String createElementString(String[] elements, int[] elementWidths, char separator, boolean center) {
        String[] padded = new String[elements.length];
        for(int i = 0; i < padded.length; i++) {
            if(center) {
                padded[i] = padStringCenter(elements[i], elementWidths[i], ' ');
            } else {
                padded[i] = padStringRight(elements[i], elementWidths[i], ' ');
            }
        }
        return String.join(separator + "", padded);
    }

    private int calculateTableWidth(int[] groupWidths) {
        int tableWidth = title.length();
        int sum = 0;
        for(int groupWidth : groupWidths) {
            sum += groupWidth;
        }
        sum += groupWidths.length + 1; // divider between groups and also at edges
        return Math.max(tableWidth, sum);
    }

    private int[] calculateGroupWidths(int[][] colWidths) {
        int[] groupWidths = new int[groups.size()];
        for(int i = 0; i < groupWidths.length; i++) {
            groupWidths[i] = groups.get(i).name.length();
            int sum = 0;
            for(int colWidth : colWidths[i]) {
                sum += colWidth;
            }
            sum += colWidths[i].length - 1; // padding between columns
            groupWidths[i] = Math.max(groupWidths[i], sum);
        }
        return groupWidths;
    }

    private String[][][] createRowStrings() {
        String[][][] rowStrings = new String[rows.size()][groups.size()][];
        for(int i = 0; i < rowStrings.length; i++) {
            for(int j = 0; j < rowStrings[i].length; j++) {
                rowStrings[i][j] = new String[groups.get(j).headers.length];
                for(int k = 0; k < rowStrings[i][j].length; k++) {
                    Object data = rows.get(i)[j][k];
                    rowStrings[i][j][k] = data == null ? "" : data.toString();
                }
            }
        }
        return rowStrings;
    }

    private int[][] calculateColumnWidths(String[][][] rowStrings) {
        int[][] widths = new int[groups.size()][];
        for(int i = 0; i < widths.length; i++) {
            widths[i] = new int[groups.get(i).headers.length];
            for(int j = 0; j < widths[i].length; j++) {
                widths[i][j] = groups.get(i).headers[j].length();
                for(String[][] row : rowStrings) {
                    widths[i][j] = Math.max(widths[i][j], row[i][j].length());
                }
            }
        }
        return widths;
    }

    private void validate() {
        if(groups.size() == 0) {
            throw new IllegalStateException("GroupedTable must have at least one group");
        }
        for(Object[][] row : rows) {
            if(row.length < groups.size()) {
                throw new IllegalStateException("Each row matrix must have an element for each group");
            }
        }
        for(int i = 0; i < groups.size(); i++) {
            for(Object[][] row : rows) {
                if(row[i].length != groups.get(i).headers.length) {
                    throw new IllegalStateException("Each row matrix must have an element for each header of each group");
                }
            }
        }
    }

    public static String padStringCenter(String s, int width, char padding) {
        if(s.length() > width) {
            throw new IllegalArgumentException();
        }
        int extra = width - s.length();
        int left = (extra + 1)/2;
        int right = extra/2;
        return repeat(padding, left) + s + repeat(padding, right);
    }

    public static String padStringRight(String s, int width, char padding) {
        if(s.length() > width) {
            throw new IllegalArgumentException();
        }
        int extra = width - s.length();
        return repeat(padding, extra) + s;
    }

    public static String repeat(char c, int length) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    public static String divider(int[] maxWidths, boolean top) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(int width : maxWidths) {
            builder.append(top && !first ? '-' : '+').append(repeat('-', width));
            first = false;
        }
        return builder.append('+').toString();
    }

    private static class Group {
        String name;
        String[] headers;

        Group(String name, String[] headers) {
            this.name = name;
            this.headers = headers;
        }
    }
}
