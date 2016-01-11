package me.curlpipesh.control.punishment;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.curlpipesh.util.plugin.SkirtsPlugin;

/**
 * A single punishment. Punishments contain large amounts of information, and
 * are what get stored in the punishment databases.
 *
 * @author audrey
 * @since 8/23/15.
 */
@Data
@ToString
public class Punishment {
    /**
     * The main plugin.
     */
    private final SkirtsPlugin control;

    /**
     * ID of the punishment. Every punishment has an id that is guaranteed to
     * be unique across
     */
    private final int id;

    /**
     * The type of the punishment.
     */
    private final PunishmentType type;

    /**
     * UUID of the player who issued the punishment. If the issuer was the
     * console, then this says "CONSOLE" instead.
     */
    private final String issuer;

    /**
     * UUID of the player targeted by this punishment
     */
    private final String target;

    /**
     * The reason for this punishment. If no reason was specified when the
     * punishment was issued, then this is just the type of the punishment in
     * a more readable form.
     */
    private final String reason;

    /**
     * Length of the punishment, in minutes.
     */
    private final int length;

    /**
     * When the punishment started. This is a UNIX timestamp.
     */
    private final long start;

    /**
     * When the punishment will end. This is also a UNIX timestamp.
     */
    private final long end;

    public Punishment(@NonNull final SkirtsPlugin control, @NonNull final int id, @NonNull final PunishmentType type,
                      @NonNull final String issuer, @NonNull final String target, @NonNull final String reason,
                      @NonNull final int length, @NonNull final long start, @NonNull final long end) {
        this.control = control;
        this.id = id;
        this.type = type;
        this.issuer = issuer;
        this.target = target;
        this.reason = reason;
        this.length = length;
        this.start = start;
        this.end = end;
    }

    /**
     * Whether or not this punishment is over. If this punishment lasts until
     * the end of time, this will always be false.
     *
     * @return <code>true</code> if the punishment is over, <code>false</code>
     *         otherwise.
     */
    public boolean over() {
        return end != Integer.MAX_VALUE && end <= System.currentTimeMillis();
    }

    /**
     * Different types of punishments. Enum value names should be
     * self-explanatory.
     */
    public enum PunishmentType {
        BAN("Ban"),
        MUTE("Mute"),
        COMMAND_MUTE("Command mute"),
        IP_BAN("IP ban"),
        WARN("Warning");

        /**
         * More English-friendly descriptoy
         */
        @Getter
        private final String type;

        PunishmentType(final String type) {
            this.type = type;
        }

        /**
         * String to punishment type. Used for loading from the databases.
         *
         * @param type The punishment type as a string
         * @return The actual punishment type
         * @throws IllegalArgumentException If the type isn't recognized
         */
        public static PunishmentType get(final String type) {
            if(type.equals(BAN.getType())) {
                return BAN;
            }
            if(type.equals(MUTE.getType())) {
                return MUTE;
            }
            if(type.equals(COMMAND_MUTE.getType())) {
                return COMMAND_MUTE;
            }
            if(type.equals(IP_BAN.getType())) {
                return IP_BAN;
            }
            if(type.equals(WARN.getType())) {
                return WARN;
            }
            throw new IllegalArgumentException(type);
        }

        /**
         * For punishment announcements
         *
         * @param punishmentType Type to convert
         * @return Englished type.
         */
        public static String english(final PunishmentType punishmentType) {
            switch(punishmentType) {
                case BAN:
                    return "banned";
                case MUTE:
                    return "muted";
                case COMMAND_MUTE:
                    return "command-muted";
                case IP_BAN:
                    return "IP-banned";
                case WARN:
                    return "warned";
            }
            return "punished";
        }
    }
}
