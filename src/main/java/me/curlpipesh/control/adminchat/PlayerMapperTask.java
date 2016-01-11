package me.curlpipesh.control.adminchat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Maps adminchat users.
 *
 * @author audrey
 * @since 10/3/15.
 */
public class PlayerMapperTask implements Runnable {
    @Override
    public void run() {
        Bukkit.getLogger().info("Mapping AdminChat users...");
        for(final Player player : Bukkit.getServer().getOnlinePlayers()) {
            final boolean present = UserMap.getAdminChatUsers().stream().filter(a -> a.getUuid().equals(player.getUniqueId())).count() > 0L;
            if(!present && (player.hasPermission("control.channels.use") || player.isOp())) {
                UserMap.addUser(player);
                Bukkit.getLogger().info("Added AdminChat user: " + player.getName());
            }
        }
    }
}
