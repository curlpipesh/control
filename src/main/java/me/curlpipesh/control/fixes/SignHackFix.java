package me.curlpipesh.control.fixes;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * Blocks the sign-related exploit that was used in <1.8.7 to crash
 * clients/servers
 *
 * @author audrey
 * @since 12/5/15.
 */
public class SignHackFix implements Fix {
    @Override
    public void fix(final Control control) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void onChunkLoad(final ChunkLoadEvent event) {
                for(final BlockState state : event.getChunk().getTileEntities()) {
                    if(!(state instanceof Sign)) {
                        continue;
                    }

                    final Sign sign = (Sign) state;

                    for(int x = 0; x < sign.getLines().length; x++) {
                        // if the line isn't super long, move on
                        if(!(sign.getLine(x).length() > 25)) {
                            continue;
                        }

                        // otherwise, replace the long string with "???"
                        sign.setLine(x, "???");

                        // Force-update the sign, without calling a physics update
                        // UNSURE IF NEEDED //
                        sign.update(true, false);
                        Bukkit.getOperators().stream().filter(OfflinePlayer::isOnline)
                                .forEach(o -> o.getPlayer().sendMessage(String.format("§c###§7 Removed signhack at [%s] (%s, %s, %s) §c###§r",
                                        event.getWorld().getName(), sign.getLocation().getBlockX(), sign.getLocation().getBlockY(), sign.getLocation().getBlockZ())));
                    }
                }
            }
        }, control);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
            public void blockSignHack(final SignChangeEvent sign) {
                for(int x = 0; x < sign.getLines().length; x++) {
                    // if the line isn't super long, move on
                    if(!(sign.getLine(x).length() > 25)) {
                        continue;
                    }

                    // otherwise, replace the long string with "???"
                    sign.setLine(x, "???");
                    //sign.setCancelled(true);

                    Bukkit.getOperators().stream().filter(OfflinePlayer::isOnline)
                            .forEach(o -> o.getPlayer().sendMessage(String.format("§c###§7 Blocked signhack at [%s] (%s, %s, %s) by §c%s ###§r",
                                    sign.getPlayer().getWorld().getName(), sign.getPlayer().getLocation().getBlockX(),
                                    sign.getPlayer().getLocation().getBlockY(), sign.getPlayer().getLocation().getBlockZ(),
                                    sign.getPlayer().getName())));
                    /*Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            String.format("kick %s Attempting to place a crashy sign", sign.getPlayer().getName()));*/
                }
            }
        }, control);
    }
}
