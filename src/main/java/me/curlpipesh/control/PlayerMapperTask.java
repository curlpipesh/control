package me.curlpipesh.control;

import me.curlpipesh.control.adminchat.UserMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author audrey
 * @since 10/3/15.
 */
public class PlayerMapperTask implements Runnable {
    @Override
    public void run() {
        for(final Player player : Bukkit.getServer().getOnlinePlayers()) {
            final boolean present = UserMap.getAdminChatUsers().stream().filter(a -> a.getUuid().equals(player.getUniqueId())).count() > 0L;
            if(!present && player.hasPermission("adminchat.use")) {
                UserMap.addUser(player);
            }
        }
    }
}
