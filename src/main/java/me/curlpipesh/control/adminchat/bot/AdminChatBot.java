package me.curlpipesh.control.adminchat.bot;

import lombok.Getter;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.adminchat.UserMap;
import me.curlpipesh.control.adminchat.bot.commands.CommandHelp;
import me.curlpipesh.control.adminchat.bot.commands.CommandInfo;
import me.curlpipesh.control.adminchat.bot.commands.CommandMewnSlap;
import me.curlpipesh.control.util.GenericUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.List;

/**
 * @author audrey
 * @since 12/5/15.
 */
public final class AdminChatBot {
    @Getter
    private static Control control;

    private static String prefix;

    @Getter
    private static List<BotCommand> commands;

    private AdminChatBot() {
    }

    public static void register(Control c) {
        control = c;
        prefix = c.getConfig().getString("admin-chat-prefix");
        commands = Arrays.<BotCommand>asList(
                new CommandHelp(),
                new CommandInfo(),
                new CommandMewnSlap()
        );
        Bukkit.getPluginManager().registerEvents(new AdminChatListener(), control);
    }

    private static class AdminChatListener implements Listener {
        @EventHandler
        @SuppressWarnings("unused")
        public void onAsyncPlayerChatEvent(final AsyncPlayerChatEvent event) {
            UserMap.getAdminChatUsers().stream().filter(p -> p.getUuid().equals(event.getPlayer().getUniqueId()))
                    .filter(UserMap.AdminChatUser::isTalkingInChannel).forEach(u -> {
                if(event.getMessage().startsWith(prefix)) {
                    final String command = event.getMessage().replaceFirst(prefix, "");
                    final String[] split = command.split(" ");
                    commands.stream().filter(c -> c.getName().equalsIgnoreCase(split[0]))
                            .filter(c -> c.getAllowedUuids().isEmpty() || c.getAllowedUuids().contains(event.getPlayer().getUniqueId()))
                            .forEach(c -> {
                                c.onCommand(event.getPlayer(), command, GenericUtil.removeFirst(split));
                                if(!c.getAllowedUuids().isEmpty()) {
                                    event.setCancelled(true);
                                }
                            });
                }
            });
        }
    }
}
