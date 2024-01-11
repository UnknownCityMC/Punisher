package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.DurationArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import de.unknowncity.ucbans.data.future.BukkitFutureResult;
import de.unknowncity.ucbans.util.UUIDFetcher;

import java.time.Duration;
import java.time.LocalDateTime;

public class BanCommand extends BaseCommand {
    public BanCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("ban")
                .permission("ucbans.command.ban")
                .argument(StringArgument.single("player"))
                .argument(DurationArgument.of("duration"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::handle)
        );
    }

    private void handle(CommandContext<CommandSource> commandSourceCommandContext) {
        var playerName = (String) commandSourceCommandContext.get("player");
        var punisher = commandSourceCommandContext.getSender();

        var duration = (Duration) commandSourceCommandContext.get("duration");
        var reason = (String) commandSourceCommandContext.get("reason");

        var endDate = LocalDateTime.now().plus(duration);

        BukkitFutureResult.of(UUIDFetcher.fetchUUID(playerName)).whenComplete(plugin, uuid -> {
            plugin.punishmentService().banPlayer(uuid, playerName, reason, punisher, endDate);
        });
    }
}
