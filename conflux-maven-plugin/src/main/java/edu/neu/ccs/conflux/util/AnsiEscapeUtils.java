package edu.neu.ccs.conflux.util;


/**
 * Utility class for formatting information printed to the command line.
 */
public final class AnsiEscapeUtils {

    /**
     * True if ansi escape sequences should not be used to emphasize messages.
     */
    public static boolean noAnsiEscapeSeqs = Boolean.parseBoolean(System.getProperty("phosphor.disableAnsiEscape", "false"));

    private AnsiEscapeUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Surrounds the specified text with the needed ANSI escape sequences to color the text's foreground the specified
     * color.
     */
    public static String colorText(String text, Color color) {
        return noAnsiEscapeSeqs ? text : String.format("%c[%dm%s%c[0m", (char) 27, color.getForegroundCode(), text, (char) 27);
    }

    /**
     * Surrounds the specified text with the needed ANSI escape sequences to bold it.
     */
    public static String boldText(String text) {
        return noAnsiEscapeSeqs ? text : String.format("%c[1m%s%c[0m", (char) 27, text, (char) 27);
    }
}

