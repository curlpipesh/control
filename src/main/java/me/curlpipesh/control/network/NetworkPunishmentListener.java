package me.curlpipesh.control.network;

import com.ikeirnez.communicationsframework.api.packets.PacketHandler;
import com.ikeirnez.communicationsframework.api.packets.PacketListener;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.control.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class NetworkPunishmentListener implements PacketListener {
    private final Control control;
    private final String banFormatString;

    public NetworkPunishmentListener(final Control control) {
        this.control = control;
        banFormatString = this.control.getConfig().getString("ban-message");
    }

    @PacketHandler
    public void onPacketTest(final PunishmentPacket packet) { // this will be run when we receive a reply from the server
        control.getLogger().info(String.format("Received network-punishment: %s", packet.toString()));
        final Optional<Punishment> p = control.getActivePunishments()
                .insertPunishment(PunishmentType.get(packet.getPunishmentType()),packet.getIssuer(), packet.getTarget(),
                        packet.getReason(), packet.getLengthInMinutes());
        if(PunishmentType.get(packet.getPunishmentType()) == PunishmentType.IP_BAN) {
            for(final Player player : Bukkit.getOnlinePlayers()) {
                kickForBan(player, formatBan(packet.getReason(),
                        packet.getLengthInMinutes() != Integer.MAX_VALUE
                                ? TimeUtil.english("t:" + packet.getLengthInMinutes() + 'm') : "Forever",
                        p.isPresent() ? new Date(p.get().getEnd()).toString() : "Some point in the future"),
                        () -> player.getAddress().getAddress().toString().replaceAll("/", "").equals(packet.getTarget()));
            }
        }
        // TODO: Broadcast
        // TODO: Announce what server it was on
    }

    /**
     * Kicks user if supplied condition evaluates to true
     *
     * @param player    Player to kick
     * @param message   Message to kick with
     * @param condition Condition that needs to be satisfied before the kick is done
     */
    private void kickForBan(final Player player, final String message, final BooleanSupplier condition) {
        if(condition.getAsBoolean()) {
            player.kickPlayer(message);
        }
    }

    /**
     * Hides the last two numbers in an IP to prevent problems.
     *
     * @param ip Probably the IP that we're hiding, unless I screwed up
     *           somewhere
     *
     * @return Censored IP
     */
    private String hideIP(final String ip) {
        return ip.trim().replaceFirst("\\.[0-9]{1,3}\\.[0-9]{1,3}$", ".XXX.XXX");
    }

    /**
     * Format a ban with the given info
     *
     * @param reason Reason for ban
     * @param time   Length of ban
     * @param end    End of ban
     *
     * @return Formatted ban
     */
    private String formatBan(final String reason, final String time, final String end) {
        return banFormatString.replaceAll("<reason>", reason).replaceAll("<time>", time).replaceAll("<end>", end);
    }
}
