package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import de.unknowncity.ucbans.data.future.BukkitFutureResult;
import de.unknowncity.ucbans.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

public class ClearHistoryCommand extends BaseCommand {
    public ClearHistoryCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("clearhistory")
                .permission("ucbans.command.clearhistory")
                .argument(StringArgument.<CommandSource>builder("player").single().build())
                .handler(this::handle)
        );
    }

    private void handle(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();
        var playerName = (String) commandSourceCommandContext.get("player");

        BukkitFutureResult.of(UUIDFetcher.fetchUUID(playerName)).whenComplete(plugin, uuid -> {
            if (uuid.isEmpty()) {
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("fetch", "uuid", "not-exists"),
                        TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                );
                return;
            }
            plugin.punishmentService().clearPunishmentHistory(uuid.get());
            plugin.messenger().sendMessage(
                    sender,
                    NodePath.path("command", "clearhistory", "success"),
                    TagResolver.resolver("player", Tag.preProcessParsed(playerName))
            );
        });
    }
}
