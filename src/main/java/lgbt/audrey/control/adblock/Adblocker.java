package lgbt.audrey.control.adblock;

import lgbt.audrey.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Will not try to catch every advertisement, simply because we end up having
 * to filter almost every possible chat message and that becomes annoying.
 *
 * @author audrey
 * @since 8/30/15.
 */
@SuppressWarnings("Duplicates")
public class Adblocker implements Listener {
    /**
     * Matches proper URLs.
     */
    private final Pattern urlRegex = Pattern.compile("(http(s)*://)*(([A-Za-z_0-9-]+)(\\.|,))*([A-Za-z_0-9-]+)(\\.|,)([A-Za-z]+)(:[0-9]{1,5})*");
    /**
     * Matches IP addresses
     */
    private final Pattern ipRegex = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,)([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,)([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\.|,)([01]?\\d\\d?|2[0-4]\\d|25[0-5])(:[0-9]{1,5})*");

    private final Control control;

    /**
     * Punishment to apply
     */
    private final String punishmentType;

    /**
     * Message sent to say that we're checking to see if it's an ad
     */
    private final String adCheckMessage;

    /**
     * Message sent when it's determined that the message is clean
     */
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String notAnAdMessage;

    /**
     * How long the punishment should be apploed for
     */
    private final String punishmentLength;

    public Adblocker(final Control control) {
        this.control = control;
        punishmentType = control.getConfig().getString("punishment-type");
        adCheckMessage = control.getConfig().getString("ad-check-message");
        notAnAdMessage = control.getConfig().getString("not-ad-message");
        punishmentLength = control.getConfig().getString("punishment-length");
    }

    /**
     * Does the magic
     * @param event The event to magick
     */
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        if(control.getMutes().contains(event.getPlayer().getUniqueId().toString())
                || control.getMutes().contains(event.getPlayer().getAddress().getAddress().toString())) {
            // Player is already muted, so we shouldn't care
            return;
        }

        final Player player = event.getPlayer();
        final String message = event.getMessage().trim().toLowerCase();
        if(event.isCancelled()) {
            return;
        }
        Result result = Result.NOT_FOUND;
        Matcher matcher = urlRegex.matcher(message);
        if(matcher.find()) {
            result = Result.FOUND;
        } else {
            matcher = ipRegex.matcher(message);
            if(matcher.find()) {
                result = Result.FOUND;
            }
        }
        final Map<String, String> foundMap = new ConcurrentHashMap<>();
        if(result == Result.FOUND) {
            final String match = matcher.group();
            if(matcher.group(matcher.groupCount()) != null) {
                final String IP = match.substring(0, match.lastIndexOf(':'));
                final int in_1 = matcher.start(matcher.groupCount());
                final int in_2 = matcher.end(matcher.groupCount());
                final String PORT = message.substring(in_1 + 1, in_2);
                boolean success = true;
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Integer.parseInt(PORT);
                } catch(final NumberFormatException ex) {
                    success = false;
                }
                foundMap.put(IP.replaceAll(",", "."), success ? PORT : "25565");
            } else {
                foundMap.put(match.replaceAll(",", "."), "25565");
            }
            player.sendMessage(adCheckMessage);
            for(final Entry<String, String> entr : foundMap.entrySet()) {
                final Server server = new Server(control, entr.getKey(), entr.getValue());
                if(server.isOnline()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(control,
                            () -> {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), punishmentType + ' '
                                        + event.getPlayer().getName() + " t:" + punishmentLength
                                        + " Advertising | Automated punishment");
                                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("control.notify.ads"))
                                        .forEach(p -> control.sendMessage(p, ChatColor.RED + event.getPlayer().getName() +
                                                ChatColor.GRAY + " potentially tried to advertise: '"
                                                + event.getMessage() + "'!"));
                            }, 0L);
                    event.setCancelled(true);
                    return;
                } else {
                    player.sendMessage(notAnAdMessage);
                }
            }
        }
    }

    private enum Result {
        FOUND, NOT_FOUND
    }
}