package lgbt.audrey.control.util;

import java.util.regex.Pattern;

/**
 * Utilities related to parsing time from
 * {@link lgbt.audrey.control.commands.GenericPunishmentCommand}.
 *
 * @author audrey
 * @since 8/23/15.
 */
public final class TimeUtil {
    /**
     * Used for recognizing whether or not something is a valid time.
     */
    private static final Pattern TIME_PATTERN = Pattern.compile("^(?i)(t:)*(\\d+)(m|h|d|w)*$");

    private TimeUtil() {
    }

    /**
     * Takes a given input and converts it into minutes. In the case of a
     * failed parse (invalid time unit, not an integer, ...), it returns
     * {@link Integer#MAX_VALUE}, which is generally recognized as "this
     * will last until the end of time."
     *
     * @param t The time string to parse
     * @return The number of minutes represented by the time string. May be
     *         a very large number
     */
    public static int parseTimeIntoMinutes(String t) {
        t = t.toLowerCase();
        if(t.startsWith("t:")) {
            t = t.replaceFirst("t:", "");
        }
        if(t.endsWith("m")) {
            try {
                return Integer.parseInt(t.replaceAll("m", ""));
            } catch(final Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("h")) {
            try {
                return Integer.parseInt(t.replaceAll("h", "")) * 60;
            } catch(final Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("d")) {
            try {
                return Integer.parseInt(t.replaceAll("d", "")) * 60 * 24;
            } catch(final Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("w")) {
            try {
                return Integer.parseInt(t.replaceAll("w", "")) * 60 * 24 * 7;
            } catch(final Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Returns whether or not the given input is a valid time string, as
     * determined by {@link #TIME_PATTERN}.
     *
     * @param string The string to validate
     * @return Whether or not the string is a valid time string
     */
    public static boolean isValidTime(final CharSequence string) {
        return TIME_PATTERN.matcher(string).matches();
    }

    /**
     * Converts a time string into an English representation of itself. Failure
     * to parse a time string is assumed to mean that the time string is meant
     * to signify an infinite length of time.
     *
     * @param t The time string to convert
     * @return The converted time string
     */
    public static String english(String t) {
        t = t.toLowerCase();
        if(t.startsWith("t:")) {
            t = t.replaceFirst("t:", "");
        }
        if(Integer.parseInt(t.replaceAll("[a-zA-Z]*", "")) == Integer.MAX_VALUE) {
            return "forever";
        }
        if(t.endsWith("m")) {
            try {
                return Integer.parseInt(t.replaceAll("m", "")) + " minute(s)";
            } catch(final Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("h")) {
            try {
                return Integer.parseInt(t.replaceAll("h", "")) + " hour(s)";
            } catch(final Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("d")) {
            try {
                return Integer.parseInt(t.replaceAll("d", "")) + " day(s)";
            } catch(final Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("w")) {
            try {
                return Integer.parseInt(t.replaceAll("w", "")) + " week(s)";
            } catch(final Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else {
            return "forever";
        }
    }
}
