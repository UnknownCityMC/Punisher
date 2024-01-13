package de.unknowncity.ucbans.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.core.command.BaseCommand;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

public class DeleteHistoryEntryCommand extends BaseCommand {
    public DeleteHistoryEntryCommand(UCBansPlugin plugin) {
        super(plugin);
    }

    @Override
    public void register(CommandManager<CommandSource> commandManager) {
       commandManager.command(commandManager.commandBuilder("deletehistoryentry")
               .permission("ucbans.command.deletehistoryentry")
               .argument(IntegerArgument.of("punishmentId"))
               .handler(this::handle)
       );
    }

    private void handle(CommandContext<CommandSource> commandSourceCommandContext) {
        var sender = commandSourceCommandContext.getSender();
        var id = (int) commandSourceCommandContext.get("punishmentId");

        plugin.punishmentService().deletePunishmentHistoryEntry(id);
        plugin.messenger().sendMessage(
                sender,
                NodePath.path("command", "deletehistoryentry", "success"),
                TagResolver.resolver("id", Tag.preProcessParsed(String.valueOf(id)))
        );
    }
}
