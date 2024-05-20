package de.unknowncity.punisher.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

public class KickCommand extends BaseCommand {
    public KickCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("kick")
                .permission("punisher.command.kick")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::handleKick)
        );
    }

    private void handleKick(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();
        var player = (Player) commandSourceCommandContext.get("player");
        var punisher = commandSourceCommandContext.getSender();

        var reason = (String) commandSourceCommandContext.get("reason");

        plugin.punishmentService().kickPlayer(player, reason, punisher);
        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "kick", "success"),
                TagResolver.resolver("player", Tag.preProcessParsed(player.getUsername()))
        );
    }
}
