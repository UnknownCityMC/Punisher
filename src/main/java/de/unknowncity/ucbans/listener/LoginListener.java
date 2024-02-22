package de.unknowncity.ucbans.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.punishment.Punishment;
import de.unknowncity.ucbans.punishment.PunishmentType;
import de.unknowncity.ucbans.punishment.types.PersistentPunishment;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.NodePath;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class LoginListener {
    private final UCBansPlugin plugin;

    public LoginListener(UCBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) {
        var uniqueId = event.getPlayer().getUniqueId();
        var isBanned = plugin.punishmentService().isBanned(uniqueId);
        var isMuted = plugin.punishmentService().isMuted(uniqueId);

        if (isMuted) {
            plugin.muteToChat().muteToChat(uniqueId);
        }

        if (!isBanned) {
            return;
        }

        var currentPunishment = plugin.punishmentService().getCachedPunishmentsForPlayer(uniqueId).stream()
                .filter(punishment -> punishment.punishmentType() == PunishmentType.BAN)
                .filter(Punishment::active).min(Comparator.comparing(Punishment::punishmentDateTime)).get();

        var endDate = currentPunishment.punishmentDateTime().plus(Duration.ofSeconds(((PersistentPunishment) currentPunishment).durationInSeconds()));
        var duration = Duration.between(LocalDateTime.now(), endDate);

        var disconnectMessage = plugin.messenger().componentFromList(NodePath.path("punishment",
                        ((PersistentPunishment) currentPunishment).durationInSeconds() == -1 ? "ban" : "tempban", "connection-attempt"),
                TagResolver.resolver("reason", Tag.preProcessParsed(currentPunishment.reason())),
                TagResolver.resolver("punisher", Tag.preProcessParsed(currentPunishment.playerLastName())),
                TagResolver.resolver("player", Tag.preProcessParsed(currentPunishment.punisherLastName())),
                TagResolver.resolver("end_date", Tag.preProcessParsed(endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))),
                TagResolver.resolver("punishment_id", Tag.preProcessParsed(String.valueOf(currentPunishment.punishmentId()))),
                TagResolver.resolver("remaining", Tag.preProcessParsed(
                        String.format(
                                plugin.messenger().getString(NodePath.path("format", "time")),
                                duration.toDaysPart(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()
                        )
                ))
        );

        event.setResult(ResultedEvent.ComponentResult.denied(disconnectMessage));
    }
}
