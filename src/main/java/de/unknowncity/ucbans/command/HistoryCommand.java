package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import de.unknowncity.ucbans.data.future.BukkitFutureResult;
import de.unknowncity.ucbans.punishment.types.TimedPunishment;
import de.unknowncity.ucbans.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.format.DateTimeFormatter;

public class HistoryCommand extends BaseCommand {
    public HistoryCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("punishmenthistory")
                .permission("ucbans.command.history")
                .argument(StringArgument.of("player"))
                .handler(this::handle)
        );
    }

    private void handle(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();
        var playerName = (String) commandSourceCommandContext.get("player");

        plugin.messenger().sendMessage(
                sender,
                NodePath.path("punishment", "history", "header"),
                TagResolver.resolver("player", Tag.preProcessParsed(playerName))
        );
        BukkitFutureResult.of(UUIDFetcher.fetchUUID(playerName)).whenComplete(
                plugin,
                uuid -> BukkitFutureResult.of(plugin.punishmentService().getPunishmentHistory(uuid)).whenComplete(
                        plugin,
                        punishments -> {
                            if (!punishments.isEmpty()) {
                                punishments.forEach(punishment -> {
                                    plugin.messenger().sendMessage(
                                            sender,
                                            NodePath.path("punishment", "history", "entry"),
                                            TagResolver.resolver("type", Tag.preProcessParsed(punishment.punishmentType().name())),
                                            TagResolver.resolver("active", Tag.preProcessParsed(String.valueOf(punishment.active()))),
                                            TagResolver.resolver("on", Tag.preProcessParsed(punishment.punishmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))),
                                            TagResolver.resolver("till", Tag.preProcessParsed(
                                                    (punishment instanceof TimedPunishment persistentPunishment) ?
                                                            persistentPunishment.punishmentEndDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                                                            punishment.punishmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                            )),

                                            TagResolver.resolver("by", Tag.preProcessParsed(String.valueOf(punishment.punisherLastName()))),
                                            TagResolver.resolver("reason", Tag.preProcessParsed(String.valueOf(punishment.reason())))
                                    );
                                });
                                plugin.server().getScheduler().buildTask(plugin, () -> {
                                    plugin.messenger().sendMessage(
                                            sender,
                                            NodePath.path("punishment", "history", "footer"),
                                            TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                                    );


                                }).schedule();
                            } else {
                                plugin.server().getScheduler().buildTask(plugin, () -> {
                                    plugin.messenger().sendMessage(sender, NodePath.path("punishment", "history", "empty"));
                                    plugin.messenger().sendMessage(
                                            sender,
                                            NodePath.path("punishment", "history", "footer"),
                                            TagResolver.resolver("player", Tag.preProcessParsed(playerName))
                                    );
                                }).schedule();
                            }

                        })
        );
    }
}



