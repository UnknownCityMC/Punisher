package de.unknowncity.punisher.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.core.command.BaseCommand;
import de.unknowncity.punisher.data.future.BukkitFutureResult;
import de.unknowncity.punisher.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

public class WarnCommand extends BaseCommand {
    public WarnCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("warn")
                .permission("punisher.command.warn")
                .argument(StringArgument.single("player"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::handleWarn)
        );
    }

    private void handleWarn(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();
        var playerName = (String) commandSourceCommandContext.get("player");

        var reason = (String) commandSourceCommandContext.get("reason");

        BukkitFutureResult.of(UUIDFetcher.fetchUUID(playerName)).whenComplete(plugin, uuid -> {
            if (uuid.isEmpty()) {
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("fetch", "uuid", "not-exists"),
                        TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                );
                return;
            }

            plugin.punishmentService().warnPlayer(uuid.get(), playerName, reason, sender);
            plugin.messenger().sendMessage(
                    sender,
                    NodePath.path("command", "warn", "success"),
                    TagResolver.resolver("player", Tag.preProcessParsed(playerName))
            );
        });
    }
}
