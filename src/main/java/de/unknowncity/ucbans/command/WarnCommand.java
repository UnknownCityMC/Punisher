package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import de.unknowncity.ucbans.data.future.BukkitFutureResult;
import de.unknowncity.ucbans.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;

public class WarnCommand extends BaseCommand {
    public WarnCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("warn")
                .permission("ucbans.command.warn")
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
