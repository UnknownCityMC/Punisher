package de.unknowncity.punisher.punishment.types;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.message.Messenger;
import de.unknowncity.punisher.punishment.PunishmentType;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class BanPunishment extends PersistentPunishment {
    public BanPunishment(
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime,
            int duration) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, duration);
        this.punishmentType = PunishmentType.BAN;
    }

    public BanPunishment(
            int punishmentId,
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime,
            int duration) {
        super(punishmentId, playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, duration);
        this.punishmentType = PunishmentType.BAN;
    }

    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger, PunisherPlugin plugin) {
        Optional<Player> playerOpt = proxyServer.getPlayer(playerUniqueId());
        if (playerOpt.isEmpty()) {
            return;
        }

        var player = playerOpt.get();

        var endDate = punishmentDateTime().plus(Duration.ofSeconds(durationInSeconds()));
        var duration = Duration.between(LocalDateTime.now(), endDate);

        var notifyMessage = messenger.componentFromList(NodePath.path("punishment", durationInSeconds() == -1 ? "ban" : "tempban", "notify"),
                TagResolver.resolver("reason", Tag.preProcessParsed(reason())),
                TagResolver.resolver("player", Tag.preProcessParsed(playerLastName())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(punisherLastName())),
                TagResolver.resolver("end_date", Tag.preProcessParsed(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))),
                TagResolver.resolver("remaining", Tag.preProcessParsed(
                        String.format(
                                messenger.getString(NodePath.path("format", "time")),
                                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()
                        )
                ))
        );

        proxyServer.getAllPlayers().forEach(
                audience -> audience.sendMessage(notifyMessage)
        );

        var kickMessage = messenger.componentFromList(NodePath.path("punishment", durationInSeconds() == -1 ? "ban" : "tempban", "disconnect"),
                TagResolver.resolver("reason", Tag.preProcessParsed(reason())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(playerLastName())),
                TagResolver.resolver("player", Tag.preProcessParsed(punisherLastName())),
                TagResolver.resolver("end_date", Tag.preProcessParsed(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))),
                TagResolver.resolver("remaining", Tag.preProcessParsed(
                        String.format(
                                messenger.getString(NodePath.path("format", "time")),
                                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()
                        )
                ))
        );

        player.disconnect(kickMessage);
    }
}
