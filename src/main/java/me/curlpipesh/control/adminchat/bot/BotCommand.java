package me.curlpipesh.control.adminchat.bot;

import lombok.Data;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author audrey
 * @since 12/5/15.
 */
@Data
public abstract class BotCommand {
    private final String name;
    private final String desc;
    private final String usage;
    private final List<UUID> allowedUuids;

    public BotCommand(final String name, final String desc, final String usage) {
        this.name = name;
        this.desc = desc;
        this.usage = usage;
        allowedUuids = new ArrayList<>();
    }

    public abstract void onCommand(CommandSender sender, String command, String[] args);

    protected final void allowUuid(UUID uuid) {
        if(!allowedUuids.contains(uuid)) {
            allowedUuids.add(uuid);
        }
    }

    protected final void allowUuid(String uuid) {
        allowUuid(UUID.fromString(uuid));
    }
}
