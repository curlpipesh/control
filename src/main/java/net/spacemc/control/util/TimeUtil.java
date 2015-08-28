package net.spacemc.control.util;

import java.util.regex.Pattern;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class TimeUtil {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(?i)(t:)*(\\d+)(m|h|d|w)*$");

    public static int parseTimeIntoMinutes(String t) {
        t = t.toLowerCase();
        if(t.startsWith("t:")) {
            t = t.replaceFirst("t:", "");
        }
        if(t.endsWith("m")) {
            try {
                return Integer.parseInt(t.replaceAll("m", ""));
            } catch(Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("h")) {
            try {
                return Integer.parseInt(t.replaceAll("h", "")) * 60;
            } catch(Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("d")) {
            try {
                return Integer.parseInt(t.replaceAll("d", "")) * 60 * 24;
            } catch(Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else if(t.endsWith("w")) {
            try {
                return Integer.parseInt(t.replaceAll("w", "")) * 60 * 24 * 7;
            } catch(Exception e) {
                e.printStackTrace();
                return Integer.MAX_VALUE;
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public static boolean isValidTime(String string) {
        return TIME_PATTERN.matcher(string).matches();
    }

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
            } catch(Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("h")) {
            try {
                return Integer.parseInt(t.replaceAll("h", "")) + " hour(s)";
            } catch(Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("d")) {
            try {
                return Integer.parseInt(t.replaceAll("d", "")) + " day(s)";
            } catch(Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else if(t.endsWith("w")) {
            try {
                return Integer.parseInt(t.replaceAll("w", "")) + " week(s)";
            } catch(Exception e) {
                e.printStackTrace();
                return "forever";
            }
        } else {
            return "forever";
        }
    }
}
