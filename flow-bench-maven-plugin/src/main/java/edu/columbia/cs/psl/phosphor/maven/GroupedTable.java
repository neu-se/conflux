package edu.columbia.cs.psl.phosphor.maven;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class GroupedTable {
    private String title = "";
    private final List<Group> groups = new ArrayList<>();
    private final List<Object[][]> rows = new ArrayList<>();
    private String floatingPointFormat = null;

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

    public GroupedTable floatingPointFormat(String floatingPointFormat) {
        this.floatingPointFormat = floatingPointFormat;
        return this;
    }

    public void printToStream(PrintStream stream) {
        validate();
        String[][][] rowStrings = createRowStrings();
        int[][] colWidths = calculateColumnWidths(rowStrings);
        int[] groupWidths = calculateGroupWidths(colWidths);
        int tableWidth = calculateTableWidth(groupWidths);
        stream.println(TablePrintUtil.divider(groupWidths, true));
        stream.println('|' + TablePrintUtil.padStringCenter(title, tableWidth - 2, ' ') + '|');
        stream.println(TablePrintUtil.divider(groupWidths, false));
        stream.println('|' + createElementString(getGroupNames(), groupWidths, '|', true)+ '|');
        stream.println('|' + createElementString(getGroupHeaderStrings(colWidths), groupWidths, '|', true)+ '|');
        stream.println(TablePrintUtil.divider(groupWidths, false));
        for(String[][] row : rowStrings) {
            stream.println('|' + createElementString(getRowGroups(row, colWidths), groupWidths, '|', true)+ '|');
        }
        stream.println(TablePrintUtil.divider(groupWidths, false));
    }

    private String[] getRowGroups(String[][] row, int[][] colWidths) {
        String[] rowGroups = new String[groups.size()];
        for(int i = 0; i < rowGroups.length; i++) {
            rowGroups[i] = createElementString(row[i], colWidths[i], ' ', true);
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
                padded[i] = TablePrintUtil.padStringCenter(elements[i], elementWidths[i], ' ');
            } else {
                padded[i] = TablePrintUtil.padStringRight(elements[i], elementWidths[i], ' ');
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
                    if(floatingPointFormat != null && (data instanceof Float || data instanceof Double)) {
                        data = String.format(floatingPointFormat, data);
                    }
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
            throw new IllegalStateException("GroupedTable must have atleast one group");
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

    private static class Group {
        String name;
        String[] headers;

        Group(String name, String[] headers) {
            this.name = name;
            this.headers = headers;
        }
    }
}
