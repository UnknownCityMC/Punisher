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

public class BanCommand extends BaseCommand {
    public BanCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("ban")
                .permission("punisher.command.ban")
                .argument(StringArgument.single("player"))
                .handler(this::handleHelp)
        );

        commandManager.command(commandManager.commandBuilder("ban")
                .permission("punisher.command.ban")
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
            if (sender.hasPermission("punisher.bantemplate." + key)) {
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
            if (uuid.isEmpty()) {
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("fetch", "uuid", "not-exists"),
                        TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                );
                return;
            }
            if (plugin.punishmentService().isBanned(uuid.get())) {
                plugin.messenger().sendMessage(
                        sender,
                        NodePath.path("command", "ban", "already-banned"),
                        TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                );
                return;
            }
            plugin.punishmentService().banPlayer(
                    uuid.get(),
                    playerName,
                    punisher,
                    plugin.configuration().templateSettings().punishmentTemplates().get(templateName));
            plugin.messenger().sendMessage(
                    sender,
                    NodePath.path("command", "ban", "success"),
                    TagResolver.resolver("player", Tag.preProcessParsed(playerName))
            );
        });
    }
}
