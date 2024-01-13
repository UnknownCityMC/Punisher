package de.unknowncity.ucbans.punishment.types;

import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.PunishmentType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;
import java.util.UUID;

public class WarnPunishment extends PersistentPunishment {
    public WarnPunishment(
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime,
            int duration
    ) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, duration);
        this.punishmentType = PunishmentType.WARN;
    }

    public WarnPunishment(
            int punishmentId,
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime,
            int duration
    ) {
        super(punishmentId, playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, duration);
        this.punishmentType = PunishmentType.WARN;
    }


    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger, UCBansPlugin plugin) {
        var notifyMessage = messenger.componentFromList(NodePath.path("punishment", "warn", "notify"),
                TagResolver.resolver("reason", Tag.preProcessParsed(reason())),
                TagResolver.resolver("player", Tag.preProcessParsed(playerLastName())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(punisherLastName()))
        );

        proxyServer.getAllPlayers().forEach(
                audience -> audience.sendMessage(notifyMessage)
        );
    }
}
