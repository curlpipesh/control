package net.spacemc.control.adblock;

import net.spacemc.control.SpaceControl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

/**
 * @author audrey
 * @since 8/30/15.
 */
@SuppressWarnings("unused")
public class Adblocker implements Listener {
    /**
     * This is magic. Don't question this regex. Test it in regexr if you don't believe me.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final Pattern adRegex =
            Pattern.compile("(http(s)*://\\s*)*([a-z0-9\\-]+(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.))*([a-z0-9\\-]+)(\\s*\\W*\\s*dot\\s*\\W*\\s*|\\.)(\\w+)(\\s*:\\s*\\d+)*(/)*",
            Pattern.CASE_INSENSITIVE);

    private SpaceControl control;
    private String punishmentType;
    private String home;

    public Adblocker(SpaceControl control) {
        this.control = control;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {

    }
}
