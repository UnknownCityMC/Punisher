package de.unknowncity.punisher.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

public class RemoveWarnCommand extends BaseCommand {
    public RemoveWarnCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("removewarn")
                .permission("punisher.command.removewarn")
                .argument(IntegerArgument.of("punishmentId"))
                .handler(this::handle)
        );
    }

    private void handle(CommandContext<CommandSource> commandSourceCommandContext) {var sender = commandSourceCommandContext.getSender();
        var warnId = (int) commandSourceCommandContext.get("punishmentId");

        plugin.punishmentService().removeWarn(warnId);
        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "removewarn", "success"),
                TagResolver.resolver("id", Tag.preProcessParsed(String.valueOf(warnId)))
        );
    }
}
