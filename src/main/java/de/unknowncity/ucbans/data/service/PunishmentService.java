package de.unknowncity.ucbans.data.service;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.data.database.dao.PunishmentDao;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.Punishment;
import de.unknowncity.ucbans.punishment.PunishmentLevel;
import de.unknowncity.ucbans.punishment.PunishmentTemplate;
import de.unknowncity.ucbans.punishment.PunishmentType;
import de.unknowncity.ucbans.punishment.types.*;
import de.unknowncity.ucbans.util.UUIDFetcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PunishmentService {
    private final PunishmentDao punishmentDao;
    private final ProxyServer proxyServer;
    private final Messenger messenger;
    private final UCBansPlugin plugin;

    private Set<Punishment> cachedPunishments;

    public PunishmentService(PunishmentDao punishmentDao, ProxyServer proxyServer, Messenger messenger, UCBansPlugin plugin) {
        this.punishmentDao = punishmentDao;
        this.proxyServer = proxyServer;
        this.messenger = messenger;
        this.cachedPunishments = new HashSet<>();
        this.plugin = plugin;
    }

    public CompletableFuture<List<Punishment>> getPunishmentHistory(UUID playerUniqueId) {
        return punishmentDao.getPunishments(playerUniqueId);
    }

    public void clearPunishmentHistory(UUID playerUniqueId) {
        punishmentDao.deleteAllPunishments(playerUniqueId);
        cachedPunishments.removeIf(punishment -> punishment.playerUniqueId().equals(playerUniqueId));
    }

    public void unPunishPlayer(UUID playerUniqueId, PunishmentType punishmentType) {
        punishmentDao.getPunishments(playerUniqueId).thenAcceptAsync(punishments -> {
            var punishmentToUpdate = punishments.stream()
                    .filter(punishment -> punishment.punishmentType() == punishmentType)
                    .filter(Punishment::active)
                    .findFirst();
            if (punishmentToUpdate.isEmpty()) {
                return;
            }
            var finalPunishment = punishmentToUpdate.get().active(false);
            punishmentDao.updatePunishment(finalPunishment);
        });
    }

    private void applyPunishment(Punishment punishment) {
        punishment.executeInitialPunishmentAction(proxyServer, messenger);
        punishmentDao.addPunishment(punishment);
        cachedPunishments.add(punishment);
    }

    public void kickPlayer(Player target, String reason, CommandSource punisher) {
        var kickPunishment = new KickPunishment(
                target.getUniqueId(),
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                target.getUsername(),
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                reason,
                true,
                LocalDateTime.now()
        );

        applyPunishment(kickPunishment);
    }

    public void banPlayer(UUID targetUUID, String targetName, CommandSource punisher, PunishmentTemplate punishmentTemplate) {
        var punishmentLevels = punishmentTemplate.punishmentLevels();
        var previousConvicts = previousConvicts(targetUUID, punishmentTemplate.reason());
        var punishmentLevel = decidePunishmentLevel(targetUUID, previousConvicts, punishmentLevels);

        var banPunishment = new BanPunishment(
                targetUUID,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                targetName,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                punishmentTemplate.reason(),
                true,
                LocalDateTime.now(),
                punishmentLevel.durationInSeconds()

        );

        applyPunishment(banPunishment);
    }

    public PunishmentLevel decidePunishmentLevel(UUID uuid, long previousConvicts, Set<PunishmentLevel> punishmentLevels) {
        var possibleExactPunishmentLevel = punishmentLevels.stream().filter(punishmentLevel -> punishmentLevel.level() == previousConvicts + 1).findFirst();
        return possibleExactPunishmentLevel.orElseGet(() -> punishmentLevels.stream().max(Comparator.comparingInt(PunishmentLevel::level)).get());
    }

    public long previousConvicts(UUID targetUUID, String reason) {
        return getCachedPunishmentsForPlayer(targetUUID).stream()
                .filter(punishment -> punishment.reason().equals(reason))
                .count();
    }

    /*
    public void mutePlayer(UUID targetUUID, String reason, CommandSource punisher, LocalDateTime muteEnd) {
        UUIDFetcher.fetchName(targetUUID).thenAcceptAsync(playerName -> {
            var mutePunishment = new MutePunishment(
                    targetUUID,
                    punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    playerName,
                    punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                    reason,
                    true,
                    LocalDateTime.now(),
                    muteEnd
            );

            applyPunishment(mutePunishment);
        });
    }

     */

    public boolean isMuted(UUID uuid) {
        return isPunished(uuid, PunishmentType.MUTE);
    }

    public boolean isBanned(UUID uuid) {
        return isPunished(uuid, PunishmentType.BAN);
    }

    public void unban(UUID uuid) {
        unPunish(uuid, PunishmentType.BAN);
    }

    public void unPunish(UUID uuid, PunishmentType punishmentType) {
        var userPunishments = getCachedPunishmentsForPlayer(uuid).stream()
                .filter(punishment -> punishment.punishmentType() == punishmentType)
                .filter(Punishment::active).toList();

        userPunishments.forEach(punishment -> {
            punishment.active(false);
            punishmentDao.updatePunishment(punishment).thenAcceptAsync(result -> {
                cachePunishments();
            });
        });
    }

    public boolean isPunished(UUID uuid, PunishmentType punishmentType) {
        var userPunishments = getCachedPunishmentsForPlayer(uuid).stream()
                .filter(punishment -> punishment.punishmentType() == punishmentType)
                .filter(Punishment::active).toList();

        userPunishments.stream()
                .filter(punishment -> punishment instanceof PersistentPunishment)
                .filter(punishment -> ((PersistentPunishment) punishment).durationInSeconds() != -1)
                .filter(punishment -> punishment.punishmentDateTime().plus(Duration.ofSeconds(((PersistentPunishment) punishment).durationInSeconds())).isBefore(LocalDateTime.now()))
                .forEach(persistantPunishment-> {
                    persistantPunishment.active(false);
                    punishmentDao.updatePunishment(persistantPunishment).thenAcceptAsync(result -> {
                        cachePunishments();
                    });
                });

        userPunishments = getCachedPunishmentsForPlayer(uuid);

        return userPunishments.stream().filter(punishment -> punishment.punishmentType() == punishmentType).anyMatch(Punishment::active);
    }



    public Set<Punishment> cachedPunishments() {
        return cachedPunishments;
    }

    public List<Punishment> getCachedPunishmentsForPlayer(UUID uuid) {
        return cachedPunishments.stream()
                .filter(punishment -> punishment.playerUniqueId().equals(uuid))
                .sorted(Comparator.comparing(Punishment::punishmentDateTime))
                .toList();
    }

    public void cachePunishments() {
        cachedPunishments = new HashSet<>();
        punishmentDao.getAllPunishments().thenAcceptAsync(cachedPunishments::addAll);
    }
}
