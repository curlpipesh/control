package me.curlpipesh.control.fixes;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * @author audrey
 * @since 12/5/15.
 */
public class NetherTopFix implements Fix {
    @Override
    public void fix(final Control control) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onBlockPlaceEvent(final BlockPlaceEvent event) {
                if(event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)
                        && event.getBlock().getLocation().getY() > 126) {
                    event.setCancelled(true);
                    control.sendMessage(event.getPlayer(), "§cYou can't build on top of the Nether!");
                }
            }
        }, control);
    }
}
