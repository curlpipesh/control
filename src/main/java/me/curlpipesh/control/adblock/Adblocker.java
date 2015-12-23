package me.curlpipesh.control.adblock;

import net.md_5.bungee.api.ChatColor;
import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.ServerOperator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author audrey
 * @since 8/30/15.
 */
@SuppressWarnings("Duplicates")
public class Adblocker implements Listener {
    /**
     * This is magic. Don't question this regex. Test it in regexr if you don't believe me.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final Pattern adRegex =
            //Pattern.compile("(http(s)*://\\s*)*([a-z0-9\\-]+(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,))*([a-z0-9\\-]+)(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,)(\\w+)(\\s*:\\s*\\d+)*(/)*",
            //Pattern.compile("(?i)(http(s)*://\\s*)*([a-z0-9\\-]+(\\s*\\W*\\s*(dot|\\.|,)\\s*\\W*\\s*))*([a-z0-9\\-]+)(\\s*\\W*\\s*(dot|\\.|,)\\s*\\W*\\s*)(\\w+)(\\s*:\\s*\\d+)*(/)*",
            Pattern.compile("(?i)(http(s)*://\\s*)*([a-z0-9\\-]+(\\s*\\W*\\s*(d0t|dot|\\.)\\s*\\W*\\s*))*([a-z0-9\\-]+)(\\s*\\W*\\s*(d0t|dot|\\.)\\s*\\W*\\s*)(\\w+)(\\s*:\\s*\\d+)*(/)*",
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
        if(control.getMutes().contains(e.getPlayer().getUniqueId().toString())
                || control.getMutes().contains(e.getPlayer().getAddress().getAddress().toString())) {
            // Player is already muted, so we shouldn't care
            return;
        }
        String message = e.getMessage().toLowerCase().trim();
        Matcher matcher = adRegex.matcher(message);
        if(matcher.find()) {
            control.sendMessage(e.getPlayer(), adCheckMessage);
            String match = matcher.group().replaceAll("(?i)(http(s)*://)*", "");
            String[] finalCheck;
            int port = 25565;
            if(match.contains(":")) {
                String portString = match.split(":")[1].trim();
                if(!portString.equals("25565")) {
                    try {
                        port = Integer.parseInt(portString);
                    } catch(Exception ignored) {}
                }
            }
            finalCheck = (match.trim().replaceAll("(\\s*\\W*\\s*(d0t|dot|d0t0)+\\s*\\W*\\s*|\\.|,)+", ".") + ":" + port).split(":");
            Server server = new Server(control, finalCheck[0], finalCheck[1]);
            if(server.isOnline()) {
                e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(control, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        punishmentType + " " + e.getPlayer().getName() + " t:" + punishmentLength + " Advertising | Automated punishment"), 0L);
                int p = port;
                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp)
                        .forEach(player -> control.sendMessage(player, ChatColor.RED + e.getPlayer().getName() +
                                ChatColor.GRAY + " tried to advertise: " + finalCheck[0] + (p != 25565 ? ":" + finalCheck[1] : "")));
            } else {
                control.sendMessage(e.getPlayer(), notAnAdMessage);
            }
        }
    }

    @SuppressWarnings("unused")
    //@EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if(control.getMutes().contains(e.getPlayer().getUniqueId().toString())
                || control.getMutes().contains(e.getPlayer().getAddress().getAddress().toString())) {
            // Player is already muted, so we shouldn't care
            return;
        }
        String message = e.getMessage().toLowerCase().trim();
        Matcher matcher = adRegex.matcher(message);
        if(matcher.find()) {
            control.sendMessage(e.getPlayer(), adCheckMessage);
            String match = matcher.group().replaceAll("(?i)(http(s)*://)*", "");
            String[] finalCheck;
            int port = 25565;
            if(match.contains(":")) {
                String portString = match.split(":")[1].trim();
                if(!portString.equals("25565")) {
                    try {
                        port = Integer.parseInt(portString);
                    } catch(Exception ignored) {}
                }
            }
            finalCheck = (match.replaceAll("(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.|,)+", ".") + ":" + port).split(":");
            Server server = new Server(control, finalCheck[0], finalCheck[1]);
            if(server.isOnline()) {
                e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(control, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        punishmentType + " " + e.getPlayer().getName() + " t:" + punishmentLength + " Advertising | Automated punishment"), 0L);
                int p = port;
                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp)
                        .forEach(player -> control.sendMessage(player, ChatColor.RED + e.getPlayer().getName() +
                                ChatColor.GRAY + " tried to advertise: " + finalCheck[0] + (p != 25565 ? ":" + finalCheck[1] : "")));
            } else {
                control.sendMessage(e.getPlayer(), notAnAdMessage);
            }
        }
    }
}