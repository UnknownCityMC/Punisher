package de.unknowncity.punisher.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.core.command.BaseCommand;
import de.unknowncity.punisher.data.future.BukkitFutureResult;
import de.unknowncity.punisher.punishment.PunishmentType;
import de.unknowncity.punisher.punishment.types.PersistentPunishment;
import de.unknowncity.punisher.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class WarnsCommand extends BaseCommand {
    public WarnsCommand(PunisherPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
        commandManager.command(commandManager.commandBuilder("warns")
                .permission("punisher.command.warns")
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
            var punishments = plugin.punishmentService().getCachedPunishmentsForPlayer(uuid.get()).stream()
                    .filter(punishment -> punishment.punishmentType() == PunishmentType.WARN).toList();

            plugin.messenger().sendMessage(
                    sender,
                    NodePath.path("punishment", "warns", "header"),
                    TagResolver.resolver("player", Tag.preProcessParsed(playerName))
            );

            if (punishments.isEmpty()) {
                plugin.messenger().sendMessage(sender, NodePath.path("punishment", "warns", "empty"));
            } else {
                punishments.forEach(punishment -> {
                    plugin.messenger().sendMessage(
                            sender,
                            NodePath.path("punishment", "warns", "entry"),
                            TagResolver.resolver("type", Tag.preProcessParsed(punishment.punishmentType().name())),
                            TagResolver.resolver("active", Tag.preProcessParsed(String.valueOf(punishment.active()))),
                            TagResolver.resolver("on", Tag.preProcessParsed(punishment.punishmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))),
                            TagResolver.resolver("till", Tag.preProcessParsed(
                                    (punishment instanceof PersistentPunishment persistentPunishment) ?
                                            persistentPunishment.punishmentDateTime().plus(Duration.ofSeconds(persistentPunishment.durationInSeconds()))
                                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                                            punishment.punishmentDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )),
                            TagResolver.resolver("punishment_id", Tag.preProcessParsed(String.valueOf(punishment.punishmentId()))),
                            TagResolver.resolver("by", Tag.preProcessParsed(String.valueOf(punishment.punisherLastName()))),
                            TagResolver.resolver("reason", Tag.preProcessParsed(String.valueOf(punishment.reason())))
                    );
                });
            }

            plugin.messenger().sendMessage(
                    sender,
                    NodePath.path("punishment", "warns", "footer"),
                    TagResolver.resolver("player", Tag.preProcessParsed(playerName))
            );
        });
    }
}
