package net.spacemc.control.punishment;

/**
 * @author audrey
 * @since 8/23/15.
 */
@SuppressWarnings("unused")
public interface Punishments {
    String BAN = "Ban", MUTE = "Mute", COMMAND_MUTE = "Command mute", IP_BAN = "IP ban", WARN = "Warning";

    static String punishmentToEnglish(String punishment) {
        switch(punishment) {
            case BAN:
                return "banned";
            case MUTE:
                return "muted";
            case COMMAND_MUTE:
                return "command-muted";
            case WARN:
                return "warned";
        }
        return "punished";
    }
}
