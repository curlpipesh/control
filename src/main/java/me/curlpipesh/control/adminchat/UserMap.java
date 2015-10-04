package me.curlpipesh.control.adminchat;

import org.bukkit.entity.*;
import lombok.*;

import java.util.concurrent.*;

import org.bukkit.*;

import java.util.*;

@SuppressWarnings("unused")
public class UserMap {
    private static final List<AdminChatUser> adminChatUsers;
    
    public static void addUser(@NonNull final Player player) {
        if(player == null) {
            throw new NullPointerException("player");
        }
        if(player.hasPermission("adminchat.use")) {
            UserMap.adminChatUsers.add(new AdminChatUser(player));
        }
    }
    
    public static void sendMessageToChannel(@NonNull final Channel channel, @NonNull final String displayName, @NonNull final String message) {
        if(channel == null) {
            throw new NullPointerException("channel");
        }
        if(displayName == null) {
            throw new NullPointerException("displayName");
        }
        if(message == null) {
            throw new NullPointerException("message");
        }
        Bukkit.getConsoleSender().sendMessage(String.format(channel.formatString, displayName, message));

        UserMap.adminChatUsers.stream()
                .filter(a -> Bukkit.getPlayer(a.getUuid()) != null)
                .filter(a -> {
                    OfflinePlayer player = Bukkit.getPlayer(a.getUuid());
                    return player.isOnline() && (player.getPlayer().hasPermission(channel.getPermissionNode()) || player.isOp());
                })
                .forEach(a -> Bukkit.getPlayer(a.getUuid()).sendMessage(String.format(channel.formatString, displayName, message)));
    }
    
    public static List<AdminChatUser> getAdminChatUsers() {
        return UserMap.adminChatUsers;
    }
    
    static {
        adminChatUsers = new CopyOnWriteArrayList<>();
    }

    public enum Channel {
        ADMIN_CHAT("AdminChat", "control.channels.admin",
                ChatColor.GRAY + "[" + ChatColor.AQUA + "AdminChat" + ChatColor.GRAY + "] [" + ChatColor.RESET + "%s" + ChatColor.GRAY + "] %s"),
        OP_CHAT("OpChat", "adminchat.channels.op",
                ChatColor.DARK_GRAY + "[" + ChatColor.RED + "OpChat" + ChatColor.DARK_GRAY + "] [" + ChatColor.RESET + "%s" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " %s");

        @Getter
        private final String name;
        @Getter
        private final String permissionNode;
        @Getter
        private final String formatString;
        
        Channel(@NonNull final String name, final String permissionNode, final String formatString) {
            if(name == null) {
                throw new NullPointerException("name");
            }
            if(permissionNode == null) {
                throw new NullPointerException("permissionNode");
            }
            if(formatString == null) {
                throw new NullPointerException("formatString");
            }
            this.name = name;
            this.permissionNode = permissionNode;
            this.formatString = formatString;
        }
    }

    @Data
    public static final class AdminChatUser {
        private Channel currentChannel;
        private boolean talkingInChannel;
        private final UUID uuid;

        public AdminChatUser(@NonNull final Player player) {
            if(player == null) {
                throw new NullPointerException("player");
            }
            this.uuid = player.getUniqueId();
            this.talkingInChannel = false;
            this.currentChannel = Channel.ADMIN_CHAT;
        }
    }
}
