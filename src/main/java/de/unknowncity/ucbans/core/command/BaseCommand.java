package dev.thezexquex.yasmpp.core.command;

import cloud.commandframework.CommandManager;
import dev.thezexquex.yasmpp.YasmpPlugin;
import org.bukkit.command.CommandSender;

public abstract class BaseCommand {
    protected final YasmpPlugin plugin;

    public BaseCommand(YasmpPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void register(CommandManager<CommandSender> commandManager);
}
