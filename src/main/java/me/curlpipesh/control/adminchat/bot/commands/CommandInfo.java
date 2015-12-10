package me.curlpipesh.control.adminchat.bot.commands;

import me.curlpipesh.control.adminchat.bot.AdminChatBot;
import me.curlpipesh.control.adminchat.bot.BotCommand;
import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author audrey
 * @since 12/6/15.
 */
public class CommandInfo extends BotCommand {
    public CommandInfo() {
        super("Info", "Displays a pile of server info", "info");
        // Me
        allowUuid("71a9fabe-6301-467c-85b9-694bc7783723");
        // Mewn
        allowUuid("78e4b9ec-20ff-4a57-91db-7ed5700b1671");
    }

    @Override
    public void onCommand(final CommandSender sender, final String command, final String[] args) {
        final String freeMemory = "§7Free Memory: §e" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory());
        final String maxMemory = "§7Max Memory: §e" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory());
        final String totalMemory = "§7Total Memory: §e" + FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory());

        final List<String> flagStringList = ManagementFactory.getRuntimeMXBean().getInputArguments();
        final StringBuilder sb = new StringBuilder();
        for(final String e : flagStringList) {
            sb.append(e).append(" ");
        }
        final String flags = "§7Flags: §e" + sb.toString().trim();
        final String runtimeInfo = "§7Runtime: §e" + System.getProperty("java.runtime.name")
                + " v" + System.getProperty("java.runtime.version")
                + "(" + System.getProperty("java.vm.name") + ")";
        final String osInfo = "§7OS: §e" + System.getProperty("os.name") + ": " + System.getProperty("os.arch") + "/" + System.getProperty("sun.arch.data.model");
        final String cpuThreads = "§7CPU Threads: §e" + Runtime.getRuntime().availableProcessors();

        final List<String> messages = new ArrayList<>();
        messages.add("§7Server info: ");
        messages.add(freeMemory);
        messages.add(maxMemory);
        messages.add(totalMemory);
        messages.add(flags);
        messages.add(runtimeInfo);
        messages.add(osInfo);
        messages.add(cpuThreads);
        AdminChatBot.getControl().sendMessage(sender, messages.stream().toArray(String[]::new));
    }
}
