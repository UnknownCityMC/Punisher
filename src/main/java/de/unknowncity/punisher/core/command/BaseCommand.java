package de.unknowncity.punisher.core.command;

import cloud.commandframework.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.punisher.PunisherPlugin;

public abstract class BaseCommand {
    protected final PunisherPlugin plugin;

    public BaseCommand(PunisherPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the command to a command manager
     * Also used to apply the initial command structure like arguments, permissions and sender types
     * @param commandManager the command manager teh command should be registered to
     */
    public abstract void register(CommandManager<CommandSource> commandManager);
}
