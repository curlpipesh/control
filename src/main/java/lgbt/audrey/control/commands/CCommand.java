package lgbt.audrey.control.commands;

import lombok.Getter;
import lgbt.audrey.control.Control;
import lgbt.audrey.control.punishment.Punishment.PunishmentType;
import lgbt.audrey.control.util.TimeUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

/**
 * Base of all commands. It works.
 *
 * @author audrey
 * @since 8/23/15.
 */
public abstract class CCommand implements CommandExecutor {
    @Getter
    private final Control control;


    public CCommand(final Control control) {
        this.control = control;
    }

    /**
     * Broadcasts application of a punishment
     *
     * @param issuer     Issuer of the punishment
     * @param player     Target of the punishment
     * @param punishment Punishment type
     * @param reason     Punishment reason
     * @param length     Punishment length
     */
    protected final void announcePunishment(final String issuer, final String player, final PunishmentType punishment,
                                            final String reason, final String length) {
        String notifyPerm = "";
        switch(punishment) {
            case WARN:
                notifyPerm = "control.notify.warn";
                break;
            case MUTE:
                notifyPerm = "control.notify.mute";
                break;
            case COMMAND_MUTE:
                notifyPerm = "control.notify.cmute";
                break;
            case IP_BAN:
            case BAN:
                notifyPerm = "control.notify.ban";
                break;
        }
        if(punishment == PunishmentType.WARN) {
            final String m = String.format("§7%s §c%s§7 %s:", issuer, PunishmentType.english(punishment), player);
            control.broadcastImportantMessageWithPerm(notifyPerm, new String[] {m, "§c" + reason});
        } else {
            final String m = String.format("§7%s §c%s§7 %s for %s:", issuer, PunishmentType.english(punishment), player,
                    TimeUtil.english(length));
            control.broadcastImportantMessageWithPerm(notifyPerm, new String[] {m, "§c" + reason});
        }
        if(punishment == PunishmentType.WARN || punishment == PunishmentType.MUTE || punishment == PunishmentType.COMMAND_MUTE) {
            final Player target = control.getServer().getPlayer(player);
            if(target != null) {
                if(punishment == PunishmentType.WARN) {
                    final String m = String.format("§7%s §c%s§7 you:", issuer, PunishmentType.english(punishment));
                    control.sendImportantMessage(target, m, "§c" + reason);
                } else {
                    final String m = String.format("§7%s §c%s§7 you for %s:", issuer, PunishmentType.english(punishment),
                            TimeUtil.english(length));
                    control.sendImportantMessage(target, m, "§c" + reason);
                }
            }
        }
    }
}
