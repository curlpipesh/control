package me.curlpipesh.control.commands;

import lombok.Getter;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.control.util.TimeUtil;
import org.bukkit.command.CommandExecutor;

/**
 * @author audrey
 * @since 8/23/15.
 */
public abstract class CCommand implements CommandExecutor {
    @Getter
    private final Control control;


    public CCommand(final Control control) {
        this.control = control;
    }

    protected final void announcePunishment(final String issuer, final String player, final PunishmentType punishment,
                                            final String reason, final String length) {
        final String m = String.format("§7%s §c%s§7 %s for %s:", issuer,
                PunishmentType.english(punishment), player, TimeUtil.english(length));
        control.broadcastImportantMessage(m, "§c" + reason);
    }
}
