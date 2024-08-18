package de.unknowncity.punisher.data.service;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.unknowncity.punisher.PunisherPlugin;
import de.unknowncity.punisher.data.database.dao.PunishmentDao;
import de.unknowncity.punisher.message.Messenger;
import de.unknowncity.punisher.punishment.Punishment;
import de.unknowncity.punisher.punishment.PunishmentLevel;
import de.unknowncity.punisher.punishment.PunishmentTemplate;
import de.unknowncity.punisher.punishment.PunishmentType;
import de.unknowncity.punisher.punishment.types.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PunishmentService {
    private final PunishmentDao punishmentDao;
    private final ProxyServer proxyServer;
    private final Messenger messenger;
    private final PunisherPlugin plugin;

    public static final MinecraftChannelIdentifier PUNISHMENT_MUTE_CHANNEL = MinecraftChannelIdentifier.from("punisher:mute");

    private Set<Punishment> cachedPunishments;

    public PunishmentService(PunishmentDao punishmentDao, ProxyServer proxyServer, Messenger messenger, PunisherPlugin plugin) {
        this.punishmentDao = punishmentDao;
        this.proxyServer = proxyServer;
        this.messenger = messenger;
        this.cachedPunishments = new HashSet<>();
        this.plugin = plugin;
    }

    /**
     * Gets the punishment history for a certain player (is empty if there is no record of punishments)
     * @param playerUniqueId the uuid of the player whose history is to be looked up
     * @return a list of punishments the player has received in the past
     */
    public CompletableFuture<List<Punishment>> getPunishmentHistory(UUID playerUniqueId) {
        return punishmentDao.getPunishments(playerUniqueId);
    }

    /**
     * Completely whips all data for expired punishments
     * @param playerUniqueId the uuid of the player whose history is to be cleaned up
     */
    public void clearPunishmentHistory(UUID playerUniqueId) {
        punishmentDao.deleteAllInactivePunishments(playerUniqueId);
        cachedPunishments.removeIf(punishment -> punishment.playerUniqueId().equals(playerUniqueId) && !punishment.active());
    }

    /**
     * Deletes a specific punishment entry with the given (unique) punishment id
     * @param punishmentId the punishment id of the punishment that should be deleted from database
     */
    public void deletePunishmentHistoryEntry(int punishmentId) {
        punishmentDao.deletePunishment(punishmentId);
        cachedPunishments.removeIf(punishment -> punishment.punishmentId() == punishmentId);
    }

    /**
     * Saves a punishment to database after executing initial action like kicking a player
     * from the server if currently online
     * @param punishment the punishment to apply
     */
    private void applyPunishment(Punishment punishment) {
        punishment.executeInitialPunishmentAction(proxyServer, messenger, plugin);
        punishmentDao.addPunishment(punishment).thenAcceptAsync(result -> cachePunishments());

    }

    /**
     * Determines the level of the applied punishment based on the player's criminal record
     * and the configured punishment levels for this specific template
     * @param previousConvicts the amount of previous punishments for the same reason
     * @param punishmentLevels the possible punishment levels for this reason
     * @return the punishment level that should be applied for the current punishment
     */
    public PunishmentLevel decidePunishmentLevel(long previousConvicts, Set<PunishmentLevel> punishmentLevels) {
        var possibleExactPunishmentLevel = punishmentLevels.stream().filter(punishmentLevel -> punishmentLevel.level() == previousConvicts + 1).findFirst();
        return possibleExactPunishmentLevel.orElseGet(() -> punishmentLevels.stream().max(Comparator.comparingInt(PunishmentLevel::level)).get());
    }

    /**
     * Counts the amount of punishments the player has received for the same reason
     * @param targetUUID the player to look up the punishments for
     * @param reason the reason to check against
     * @return amount of punishments the player has received for the same reason
     */
    public long previousConvicts(UUID targetUUID, String reason) {
        return getCachedPunishmentsForPlayer(targetUUID).stream()
                .filter(punishment -> punishment.reason().equals(reason))
                .count();
    }

    /**
     * Applies a warn punishment to a specific player
     * @param targetUUID the uuid of player to warn
     * @param targetName the name of player to warn
     * @param reason the reason the player is warned for
     * @param punisher the instance that punishes the player
     */
    public void warnPlayer(UUID targetUUID, String targetName, String reason, CommandSource punisher) {
        var warnPunishment = new WarnPunishment(
                targetUUID,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                targetName,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                reason,
                true,
                LocalDateTime.now(),
                -1
        );

        applyPunishment(warnPunishment);
    }

    /**
     * Applies a kick punishment to a specific player
     * @param target the player to kick (Players have to be online to be kicked)
     * @param reason the reason the player is warned for
     * @param punisher the instance that punishes the player
     */
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

    /**
     * Applies a ban punishment to a specific player
     * @param targetUUID the uuid of player to ban
     * @param targetName the name of player to ban
     * @param punishmentTemplate the template the player is banned for
     * @param punisher the instance that punishes the player
     */
    public void banPlayer(UUID targetUUID, String targetName, CommandSource punisher, PunishmentTemplate punishmentTemplate) {
        var punishmentLevels = punishmentTemplate.punishmentLevels();
        var previousConvicts = previousConvicts(targetUUID, punishmentTemplate.reason());
        var punishmentLevel = decidePunishmentLevel(previousConvicts, punishmentLevels);

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

    /**
     * Applies a mute punishment to a specific player
     * @param targetUUID the uuid of player to mute
     * @param targetName the name of player to mute
     * @param reason teh reason the player is muted for
     * @param punisher the instance that punishes the player
     */
    public void mutePlayer(UUID targetUUID, String targetName, String reason, CommandSource punisher, int duration) {
        var mutePunishment = new MutePunishment(
                targetUUID,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUniqueId() : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                targetName,
                punisher instanceof Player punisherPlayer ? punisherPlayer.getUsername() : "CONSOLE",
                reason,
                true,
                LocalDateTime.now(),
                duration
        );

        applyPunishment(mutePunishment);

        //TODO: Find a better solution that does not require specific integration with other plugins
        plugin.muteToChat().muteToChat(mutePunishment);
    }


    /**
     * Checks if a player is muted
     * @param uuid the uuid of the player to check for
     * @return if the player is muted
     */
    public boolean isMuted(UUID uuid) {
        return isPunished(uuid, PunishmentType.MUTE);
    }

    /**
     * Checks if a player is banned
     * @param uuid the uuid of the player to check for
     * @return if the player is banned
     */
    public boolean isBanned(UUID uuid) {
        return isPunished(uuid, PunishmentType.BAN);
    }

    /**
     * Sets all punishments of the type BAN to inactive
     * allowing the player to join the server again but still keep them in history
     * @param uuid the uuid of the player to unban
     */
    public void unban(UUID uuid) {
        unPunish(uuid, PunishmentType.BAN);
    }

    /**
     * Sets all punishments of the type MUTE to inactive
     * allowing the player to chat again but still keep them in history
     * @param uuid the uuid of the player to unmute
     */
    public void unmute(UUID uuid) {
        unPunish(uuid, PunishmentType.MUTE);
    }

    /**
     * Removes a warn punishment from a players account
     * IMPORTANT: Also removes the punishment from the history
     * @param id the id of the warn punishment to remove
     */
    public void removeWarn(int id) {
        var warnPunishment = cachedPunishments().stream()
                .filter(punishment -> punishment.punishmentType() == PunishmentType.WARN)
                .filter(punishment -> punishment.punishmentId() == id)
                .findFirst();

        if (warnPunishment.isPresent()) {
            punishmentDao.deletePunishment(id).thenAcceptAsync(result -> {
                cachePunishments();
            });
        }
    }

    /**
     * Sets any punishment of given type inactive
     * but keeps them in history
     * @param uuid the uuid of the player the punishments should be set inactive
     * @param punishmentType the type of punishments that should be set inactive
     */
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

    /**
     * Checks if there are any active punishments for a player with a specific type
     * @param uuid the uuid of the player to check for
     * @param punishmentType the punishment type to check for
     * @return if there are active punishments
     */
    public boolean isPunished(UUID uuid, PunishmentType punishmentType) {
        var userPunishments = getCachedPunishmentsForPlayer(uuid).stream()
                .filter(punishment -> punishment.punishmentType() == punishmentType)
                .filter(Punishment::active).toList();

        userPunishments.stream()
                .filter(punishment -> punishment instanceof PersistentPunishment)
                .filter(punishment -> ((PersistentPunishment) punishment).durationInSeconds() != -1)
                .filter(punishment -> punishment.punishmentDateTime().plus(Duration.ofSeconds(((PersistentPunishment) punishment).durationInSeconds())).isBefore(LocalDateTime.now()))
                .forEach(persistantPunishment -> {
                    persistantPunishment.active(false);
                    punishmentDao.updatePunishment(persistantPunishment).thenAcceptAsync(result -> {
                        cachePunishments();
                    });
                });

        userPunishments = getCachedPunishmentsForPlayer(uuid);

        return userPunishments.stream().filter(punishment -> punishment.punishmentType() == punishmentType).anyMatch(Punishment::active);
    }

    /**
     * Gets all cached punishments for easier access
     * @return all cached punishments
     */
    public Set<Punishment> cachedPunishments() {
        return cachedPunishments;
    }

    /**
     * Gets all cached punishments for a specific player
     * @param uuid the uuid of the player to get the punishments for
     * @return all cached punishments for the player
     */
    public List<Punishment> getCachedPunishmentsForPlayer(UUID uuid) {
        return cachedPunishments.stream()
                .filter(punishment -> punishment.playerUniqueId().equals(uuid))
                .sorted(Comparator.comparing(Punishment::punishmentDateTime))
                .toList();
    }
    /**
     * Gets all cached active mute punishments for a specific player
     * @param uuid the uuid of the player to get the punishments for
     * @return all cached active mute punishments for the player
     */
    public Optional<Punishment> getCachedActiveMutePlayer(UUID uuid) {
        return cachedPunishments.stream()
                .filter(punishment -> punishment.playerUniqueId().equals(uuid))
                .filter(punishment -> punishment.punishmentType().equals(PunishmentType.MUTE))
                .filter(Punishment::active).min(Comparator.comparing(Punishment::punishmentDateTime));
    }

    /**
     * Caches all punishments that are currently present in the database
     * TODO: Replace with more effective caching
     */
    public void cachePunishments() {
        cachedPunishments = new HashSet<>();
        punishmentDao.getAllPunishments().thenAcceptAsync(punishments -> cachedPunishments.addAll(punishments));
    }

}
