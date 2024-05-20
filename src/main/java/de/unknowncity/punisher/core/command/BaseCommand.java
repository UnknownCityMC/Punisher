package de.unknowncity.punisher.core.command;

import cloud.commandframework.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.punisher.PunisherPlugin;

public abstract class BaseCommand {
    protected final PunisherPlugin plugin;

    public BaseCommand(PunisherPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void register(CommandManager<CommandSource> commandManager);
}
