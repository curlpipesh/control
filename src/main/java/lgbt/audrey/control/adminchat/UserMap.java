package lgbt.audrey.control.adminchat;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Map of users for adminchat
 */
@SuppressWarnings("unused")
public final class UserMap {
    /**
     * List of all users in the adminchat
     */
    private static final List<AdminChatUser> adminChatUsers;

    private UserMap() {
    }

    /**
     * Add a user to the adminchat
     *
     * @param player User to add
     */
    public static void addUser(@NonNull final Player player) {
        if(player == null) {
            throw new NullPointerException("player");
        }
        if(player.hasPermission("control.channels.use")) {
            adminChatUsers.add(new AdminChatUser(player));
        }
    }

    /**
     * Sends a message to the given channel, from the given display name
     *
     * @param channel Channel to send to
     * @param displayName Name of the sender
     * @param message Message to send
     */
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

        adminChatUsers.stream()
                .filter(a -> Bukkit.getPlayer(a.getUuid()) != null)
                .filter(a -> {
                    OfflinePlayer player = Bukkit.getPlayer(a.getUuid());
                    return player.isOnline() && (player.getPlayer().hasPermission(channel.getPermissionNode()) || player.isOp());
                })
                .forEach(a -> Bukkit.getPlayer(a.getUuid()).sendMessage(String.format(channel.formatString, displayName, message)));
    }

    /**
     * Returns the list of adminchat users
     *
     * @return The list of adminchat users
     */
    public static List<AdminChatUser> getAdminChatUsers() {
        return adminChatUsers;
    }
    
    static {
        adminChatUsers = new CopyOnWriteArrayList<>();
    }

    /**
     * Channel for admin-related chat. May be expanded in the future.
     */
    public enum Channel {
        ADMIN_CHAT("AdminChat", "control.channels.adminchat",
                ChatColor.GRAY + "[" + ChatColor.AQUA + "AdminChat" + ChatColor.GRAY + "] [" + ChatColor.RESET + "%s" + ChatColor.GRAY + "] %s");
        /*,
        OP_CHAT("OpChat", "adminchat.channels.op",
                ChatColor.DARK_GRAY + "[" + ChatColor.RED + "OpChat" + ChatColor.DARK_GRAY + "] [" + ChatColor.RESET + "%s" + ChatColor.DARK_GRAY + "]" + ChatColor.GRAY + " %s");*/

        /**
         * Channel name
         */
        @Getter
        private final String name;

        /**
         * Channel permission node
         */
        @Getter
        private final String permissionNode;

        /**
         * Format of messages sent to the channel
         */
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

    /**
     * User of the adminchat
     */
    @Data
    public static final class AdminChatUser {
        /**
         * Channel the user is currently in
         */
        @SuppressWarnings("FieldMayBeFinal")
        private Channel currentChannel;

        /**
         * Whether or not the user is talking in their current channel
         */
        @SuppressWarnings("FieldMayBeFinal")
        private boolean talkingInChannel;

        /**
         * UUID of the user
         */
        private final UUID uuid;

        public AdminChatUser(@SuppressWarnings("TypeMayBeWeakened") @NonNull final Player player) {
            if(player == null) {
                throw new NullPointerException("player");
            }
            uuid = player.getUniqueId();
            talkingInChannel = false;
            currentChannel = Channel.ADMIN_CHAT;
        }
    }
}
