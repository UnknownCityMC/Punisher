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

public class UnbanCommand extends BaseCommand {
    public UnbanCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("unban")
                .permission("punisher.command.unban")
                .argument(StringArgument.single("player"))
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
            if (plugin.punishmentService().isBanned(uuid.get())) {
                plugin.punishmentService().unban(uuid.get());
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("command", "unban", "success"),
                        TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                );
                return;
            }
            plugin.messenger().sendMessage(sender, NodePath.path("command", "unban", "not-banned"));
        });
    }
}
