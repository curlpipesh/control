package me.curlpipesh.control.adblock;

import net.md_5.bungee.api.ChatColor;
import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.permissions.ServerOperator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author audrey
 * @since 8/30/15.
 */
public class Adblocker implements Listener {
    /**
     * This is magic. Don't question this regex. Test it in regexr if you don't believe me.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final Pattern adRegex =
            Pattern.compile("(http(s)*://\\s*)*([a-z0-9\\-]+(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,))*([a-z0-9\\-]+)(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,)(\\w+)(\\s*:\\s*\\d+)*(/)*",
            Pattern.CASE_INSENSITIVE);

    private Control control;
    private String punishmentType;
    private String adCheckMessage;
    private String notAnAdMessage;
    private String punishmentLength;

    public Adblocker(Control control) {
        this.control = control;
        this.punishmentType = control.getConfig().getString("punishment-type");
        this.adCheckMessage = control.getConfig().getString("ad-check-message");
        this.notAnAdMessage = control.getConfig().getString("not-ad-message");
        this.punishmentLength = control.getConfig().getString("punishment-length");
    }


    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage().toLowerCase().trim();
        Matcher matcher = adRegex.matcher(message);
        if(matcher.find()) {
            control.sendMessage(e.getPlayer(), adCheckMessage);
            String match = matcher.group().replaceAll("(?i)(http(s)*://)*", "");
            String[] finalCheck;
            int port = -1;
            // TODO This seems really inefficient?
            if(matcher.group(matcher.groupCount()) != null && match.contains(":")) {
                String ip = match.substring(0, match.lastIndexOf(":"));
                int i1 = matcher.start(matcher.groupCount());
                int i2 = matcher.start(matcher.groupCount());
                String portString = message.substring(i1 + 1, i2);
                try {
                    port = Integer.parseInt(portString);
                } catch(Exception ignored) {}
                finalCheck = (match.replaceAll("(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,)+", ".") + (port > -1 ? ":" + port : ":25565")).split(":");
            } else {
                finalCheck = (match.replaceAll("(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,)+", ".") + ":25565").split(":");
            }
            Server server = new Server(control, finalCheck[0], finalCheck[1]);
            if(server.isOnline()) {
                e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(control, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        punishmentType + " " + e.getPlayer().getName() + " t:" + punishmentLength + " Advertising | Automated punishment"), 0L);
                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp)
                        .forEach(player -> control.sendMessage(player, ChatColor.RED + e.getPlayer().getName() +
                                ChatColor.GRAY + " tried to advertise: " + server.getIp()));
            } else {
                control.sendMessage(e.getPlayer(), notAnAdMessage);
            }
        }
    }
}
