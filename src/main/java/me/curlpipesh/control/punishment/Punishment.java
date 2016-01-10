package me.curlpipesh.control.punishment;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.curlpipesh.util.plugin.SkirtsPlugin;

/**
 * @author audrey
 * @since 8/23/15.
 */
@Data
@ToString
public class Punishment {
    private final SkirtsPlugin control;
    private final int id;
    private final PunishmentType type;
    private final String issuer;
    private final String target;
    private final String reason;
    private final int length;
    private final long start;
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

    public boolean over() {
        return end != Integer.MAX_VALUE && end <= System.currentTimeMillis();
        //!end.equalsIgnoreCase("forever") && Control.getFormat().parse(end).before(new Date());
    }

    public enum PunishmentType {
        BAN("Ban"), MUTE("Mute"), COMMAND_MUTE("Command mute"), IP_BAN("IP ban"), WARN("Warning");

        @Getter
        private final String type;

        PunishmentType(final String type) {
            this.type = type;
        }

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
