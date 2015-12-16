package me.curlpipesh.control.adblock;

import me.curlpipesh.control.Control;
import net.md_5.bungee.api.ChatColor;
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
@SuppressWarnings("Duplicates")
public class Adblocker implements Listener {
    private final Pattern urlRegex = Pattern.compile("(?i)(([a-z0-9])+([\\.|\\s])+){1,2}([a-z]{1,10})", Pattern.CASE_INSENSITIVE);
    private final Pattern ipRegex = Pattern.compile("(?i)(([0-9]){1,3}([\\.|\\s])){3}([0-9]){1,3}", Pattern.CASE_INSENSITIVE);

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


    // TODO: Consider checking PMs
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        if(punishmentType.equalsIgnoreCase("mute")) {
            if(control.getMutes().contains(e.getPlayer().getUniqueId().toString())
                    || control.getMutes().contains(e.getPlayer().getAddress().getAddress().toString())) {
                // Player is already muted, so we shouldn't care
                return;
            }
        }
        String message = e.getMessage().toLowerCase().trim();
        Matcher matcher = urlRegex.matcher(message);
        if(!matcher.find()) {
            control.getLogger().info("Couldn't find URL, trying IP...");
            matcher = ipRegex.matcher(message);
        } else {
            control.getLogger().info("Resetting URL matcher....");
            matcher.reset();
        }
        if(matcher.find()) {
            control.getLogger().info("Checking match: " + matcher.toString());

            //control.sendMessage(e.getPlayer(), adCheckMessage);
            String match = matcher.group().replaceAll("(?i)(http(s)*://)*", "").trim().replaceAll(" ", ".").trim();
            String[] finalCheck;
            int port = 25565;
            if(match.contains(":")) {
                String portString = match.split(":")[1].trim();
                if(!portString.equals("25565")) {
                    try {
                        port = Integer.parseInt(portString);
                    } catch(Exception ignored) {
                    }
                }
            }
            finalCheck = (match + ":" + port).split(":");
            Server server = new Server(control, finalCheck[0].trim(), finalCheck[1].trim());
            control.getLogger().info("Final check: " + finalCheck[0].trim() + ":" + finalCheck[1].trim());
            if(server.isOnline()) {
                e.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(control, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        punishmentType + " " + e.getPlayer().getName() + " t:" + punishmentLength + " Advertising | Automated punishment"), 0L);
                int p = port;
                Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp)
                        .forEach(player -> control.sendMessage(player, ChatColor.RED + e.getPlayer().getName() +
                                ChatColor.GRAY + " tried to advertise: " + ChatColor.RED + finalCheck[0] + (p != 25565 ? ":" + finalCheck[1] : "")));
            } /*else {
                control.sendMessage(e.getPlayer(), notAnAdMessage);
            }*/
        }
    }
}
