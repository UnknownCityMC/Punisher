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
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.LocalDateTime;

public class BanCommand extends BaseCommand {
    public BanCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        /*
        commandManager.command(commandManager.commandBuilder("ban")
                .permission("ucbans.command.ban.admin")
                .argument(StringArgument.single("player"))
                .argument(DurationArgument.of("duration"))
                .argument(StringArgument.greedy("reason"))
                .handler(this::handle)
        );
         */

        commandManager.command(commandManager.commandBuilder("ban")
                .permission("ucbans.command.ban")
                .argument(StringArgument.single("player"))
                .handler(this::handleHelp)
        );

        commandManager.command(commandManager.commandBuilder("ban")
                .permission("ucbans.command.ban")
                .argument(StringArgument.single("player"))
                .argument(
                        StringArgument.<CommandSource>builder("template")
                                .single()
                                .withSuggestionsProvider((objectCommandContext, s) -> plugin.configuration().templateSettings().punishmentTemplates().keySet().stream().toList()).build()
                )
                .handler(this::handleTemplate)
        );
    }

    private void handleHelp(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();

        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "ban", "overview", "header")
        );
        plugin.configuration().templateSettings().punishmentTemplates().forEach((key, punishmentTemplate) -> {
            if (sender.hasPermission("ucbans.bantemplate." + key)) {
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("command", "ban", "overview", "entry"),
                        TagResolver.resolver("id", Tag.preProcessParsed(key)),
                        TagResolver.resolver("reason", Tag.preProcessParsed(punishmentTemplate.reason()))
                );
            }
        });
        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "ban", "overview", "footer")
        );
    }

    private void handleTemplate(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();

        var playerName = (String) commandSourceCommandContext.get("player");
        var punisher = commandSourceCommandContext.getSender();
        var templateName = (String) commandSourceCommandContext.get("template");

        BukkitFutureResult.of(UUIDFetcher.fetchUUID(playerName)).whenComplete(plugin, uuid -> {
            plugin.punishmentService().banPlayer(
                    uuid,
                    playerName,
                    punisher,
                    plugin.configuration().templateSettings().punishmentTemplates().get(templateName));
        });
        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "ban", "success"),
                TagResolver.resolver("player", Tag.preProcessParsed(playerName))
        );
    }

    /*
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

     */
}
