package me.curlpipesh.control.fixes;

import me.curlpipesh.control.Control;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Prevent Freecam from being used to open chests and whatnot.
 *
 * TODO: Test more!
 *
 * @author audrey
 * @since 1/10/16.
 */
public class FreecamFix implements Fix {
    
    private static final Set<Material> TRANSPARENT;
    private static final Material[] materialArray;
    
    static {
        //noinspection SetReplaceableByEnumSet
        TRANSPARENT = new HashSet<>();
        // TODO: Replace with actual materials...
        //noinspection deprecation
        materialArray = new Material[] {Material.getMaterial(44), Material.getMaterial(50), Material.getMaterial(65),
                Material.getMaterial(75), Material.getMaterial(100), Material.getMaterial(76),
                Material.getMaterial(-108), Material.getMaterial(-105), Material.getMaterial(69),
                Material.getMaterial(77), Material.getMaterial(0), Material.getMaterial(8), Material.getMaterial(9),
                Material.getMaterial(10), Material.getMaterial(11), Material.getMaterial(78), Material.getMaterial(70),
                Material.getMaterial(72), Material.getMaterial(68), Material.getMaterial(68)};
        Collections.addAll(TRANSPARENT, materialArray);
    }
    
    
    @Override
    public void fix(final Control control) {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @SuppressWarnings("unused")
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onChestInteract(final PlayerInteractEvent event) {
                final Player player = event.getPlayer();
                final Block block = event.getClickedBlock();
                final Action action = event.getAction();
                if(action != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }
                if(block == null || block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
                    return;
                }
                final Block found = player.getTargetBlock(TRANSPARENT, 5);
                if(found == null) {
                    return;
                }
                if(found.getType() != Material.CHEST && found.getType() != Material.TRAPPED_CHEST) {
                    event.setCancelled(true);
                }
            }
        }, control);
    }
}
