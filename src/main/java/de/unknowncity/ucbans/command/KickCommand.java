package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import de.unknowncity.ucbans.punishment.types.KickPunishment;

import java.time.LocalDateTime;
import java.util.UUID;

public class KickCommand extends BaseCommand {
    public KickCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("kick")
                .permission("ucbans.command.kick")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::handleKick)
        );
    }

    private void handleKick(CommandContext<CommandSource> commandSourceCommandContext) {
        var player = (Player) commandSourceCommandContext.get("player");
        var punisher = commandSourceCommandContext.getSender();

        var reason = (String) commandSourceCommandContext.get("reason");

        plugin.punishmentService().kickPlayer(player, reason, punisher);
    }
}
