package me.curlpipesh.control.adminchat.bot.commands;

import me.curlpipesh.control.adminchat.bot.AdminChatBot;
import me.curlpipesh.control.adminchat.bot.BotCommand;
import me.curlpipesh.control.util.GenericUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author audrey
 * @since 12/5/15.
 */
public class CommandMewnSlap extends BotCommand {
    public CommandMewnSlap() {
        super("MewnSlap", "Slaps with the force of a million mewns", "mewnslap <player>");
        // Me
        allowUuid("71a9fabe-6301-467c-85b9-694bc7783723");
        // Mewn
        allowUuid("78e4b9ec-20ff-4a57-91db-7ed5700b1671");
    }

    @Override
    public void onCommand(final CommandSender sender, final String command, final String[] args) {
        if(args.length != 1) {
            AdminChatBot.getControl().sendMessage(sender, "§7Usage: " + this.getUsage());
        } else {
            final Player player = Bukkit.getPlayer(args[0]);
            if(player != null) {
                if(player.isOnline()) {
                    final double r1 = GenericUtil.getRandomNormalizedDouble();
                    final double r2 = GenericUtil.getRandomNormalizedDouble();
                    final double r3 = GenericUtil.getRandomNormalizedDouble();
                    final boolean x = r1 <= 0.5;
                    final boolean y = r2 >= 0.5;
                    final boolean z = r3 >= 0.25 || r3 <= 0.75;
                    player.getWorld().strikeLightning(player.getLocation());
                    for(int i = 0; i < 4; ++i) {
                        player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 0, 4);
                    }
                    player.setFireTicks(40);
                    player.setVelocity(new Vector(x ? -5.0f : 5.0f, y ? 2.5f : 5.0f, z ? -5.0f : 5.0f));
                    AdminChatBot.getControl().sendMessage(sender, ChatColor.GREEN + args[0] + ChatColor.GRAY + " was mewnslapped!");
                } else {
                    AdminChatBot.getControl().sendMessage(sender, ChatColor.RED + args[0] + ChatColor.GRAY + " is not online!");
                }
            } else {
                AdminChatBot.getControl().sendMessage(sender, ChatColor.RED + args[0] + ChatColor.GRAY + " couldn't be found!");
            }
        }
    }
}
