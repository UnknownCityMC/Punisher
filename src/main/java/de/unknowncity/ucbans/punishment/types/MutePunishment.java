package de.unknowncity.ucbans.punishment.types;

import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.data.future.BukkitFutureResult;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.PunishmentType;
import de.unknowncity.ucbans.util.UUIDFetcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class MutePunishment extends PersistentPunishment {
    public MutePunishment(
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
        this.punishmentType = PunishmentType.MUTE;
    }

    public MutePunishment(
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
        this.punishmentType = PunishmentType.MUTE;
    }

    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger, UCBansPlugin plugin) {
        var endDate = punishmentDateTime().plus(Duration.ofSeconds(durationInSeconds()));
        var duration = Duration.between(LocalDateTime.now(), endDate);

        var notifyMessage = messenger.componentFromList(NodePath.path("punishment", "mute", "notify"),
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
    }
}
