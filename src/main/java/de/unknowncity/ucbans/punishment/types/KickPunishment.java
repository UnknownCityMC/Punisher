package de.unknowncity.ucbans.punishment.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.Punishment;
import de.unknowncity.ucbans.punishment.PunishmentType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class KickPunishment extends Punishment {

    public KickPunishment(
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime
    ) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, false, punishmentDateTime);
        this.punishmentType = PunishmentType.KICK;
    }

    public KickPunishment(
            int punishmentId,
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime
    ) {
        super(punishmentId, playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, false, punishmentDateTime);
        this.punishmentType = PunishmentType.KICK;
    }

    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger) {
        Optional<Player> playerOpt = proxyServer.getPlayer(playerUniqueId());
        if (playerOpt.isEmpty()) {
            return;
        }

        var player = playerOpt.get();

        var kickMessage = messenger.componentFromList(NodePath.path("punishment", "kick", "disconnect"),
                TagResolver.resolver("reason", Tag.preProcessParsed(reason())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(playerLastName())),
                TagResolver.resolver("player", Tag.preProcessParsed(punisherLastName()))
        );

        player.disconnect(kickMessage);
    }
}
