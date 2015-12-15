package me.curlpipesh.control.commands;

import me.curlpipesh.control.Control;
import org.apache.commons.io.FileUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author audrey
 * @since 12/14/15.
 */
public class CommandInfo extends CCommand {
    public CommandInfo(final Control control) {
        super(control);
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
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
        getControl().sendMessage(commandSender, messages.stream().toArray(String[]::new));
        return true;
    }
}
