package de.unknowncity.ucbans.core.command;

import cloud.commandframework.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucbans.UCBansPlugin;

public abstract class BaseCommand {
    protected final UCBansPlugin plugin;

    public BaseCommand(UCBansPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void register(CommandManager<CommandSource> commandManager);
}
