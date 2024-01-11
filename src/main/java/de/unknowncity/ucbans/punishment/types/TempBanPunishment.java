package de.unknowncity.ucbans.punishment.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.PunishmentType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class TempBanPunishment extends TimedPunishment {
    public TempBanPunishment(UUID playerUniqueId, UUID punisherUniqueId, String playerLastName, String punisherLastName, String reason, boolean active, LocalDateTime punishmentDateTime, LocalDateTime punishmentEndDateTime) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, punishmentEndDateTime);
        this.punishmentType = PunishmentType.TEMP_BAN;
    }

    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger) {
        Optional<Player> playerOpt = proxyServer.getPlayer(playerUniqueId());
        if (playerOpt.isEmpty()) {
            return;
        }

        var player = playerOpt.get();

        var kickMessage = messenger.componentFromList(NodePath.path("punishment", "ban", "disconnect"),
                TagResolver.resolver("reason", Tag.preProcessParsed(reason())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(playerLastName())),
                TagResolver.resolver("player", Tag.preProcessParsed(punisherLastName())),
                TagResolver.resolver("till", Tag.preProcessParsed(punishmentEndDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        );

        player.disconnect(kickMessage);
    }
}
