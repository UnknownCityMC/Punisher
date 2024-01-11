package de.unknowncity.ucbans.data.service;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.UCBansPlugin;
import de.unknowncity.ucbans.data.database.dao.PunishmentDao;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.Punishment;
import de.unknowncity.ucbans.punishment.PunishmentType;
import de.unknowncity.ucbans.punishment.types.*;
import de.unknowncity.ucbans.util.UUIDFetcher;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PunishmentService {
    private final PunishmentDao punishmentDao;
    private final ProxyServer proxyServer;
    private final Messenger messenger;
    private final UCBansPlugin plugin;

    private final Set<Punishment> cachedPunishments;

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

    public void banPlayer(UUID targetUUID, String targetName, String reason, CommandSource punisher, LocalDateTime banEnd) {
        var banPunishment = new TempBanPunishment(
                targetUUID,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                targetName,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                reason,
                true,
                LocalDateTime.now(),
                banEnd
        );

        applyPunishment(banPunishment);
    }

    public void banPlayerPermanent(UUID targetUUID, String targetName, String reason, CommandSource punisher) {
        var banPunishment = new BanPunishment(
                targetUUID,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                targetName,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                reason,
                true,
                LocalDateTime.now()
        );

        applyPunishment(banPunishment);
    }

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

    public CompletableFuture<Boolean> isMuted(UUID uuid) {
        return punishmentDao.getPunishments(uuid).thenApply(punishments -> punishments.stream()
                .filter(punishment -> punishment.punishmentType() == PunishmentType.MUTE)
                .anyMatch(Punishment::active));
    }

    public boolean isBanned(UUID uuid) {
        var userPunishments = cachedPunishments.stream()
                .filter(punishment -> punishment.playerUniqueId().equals(uuid))
                .filter(punishment -> punishment.punishmentType() == PunishmentType.TEMP_BAN || punishment.punishmentType() == PunishmentType.BAN)
                .filter(Punishment::active).toList();


        var banned = userPunishments.stream()
                .anyMatch(punishment -> ((punishment instanceof TimedPunishment timedPunishment) &&
                        timedPunishment.punishmentEndDateTime().isAfter(LocalDateTime.now())) ||
                        (punishment instanceof InfinitePunishment));

        plugin.server().getScheduler().buildTask(plugin, () -> {
            userPunishments.stream()
                    .filter(punishment -> punishment instanceof TimedPunishment)
                    .filter(punishment -> ((TimedPunishment) punishment).punishmentEndDateTime().isBefore(LocalDateTime.now()))
                    .forEach(timedPunishment-> {
                        var punishment = timedPunishment.active(false);
                        punishmentDao.updatePunishment(punishment);
                    });
        });

        return banned;
    }

    public Set<Punishment> cachedPunishments() {
        return cachedPunishments;
    }

    public void cachePunishments() {
        punishmentDao.getAllPunishments().thenAcceptAsync(cachedPunishments::addAll);
    }
}
