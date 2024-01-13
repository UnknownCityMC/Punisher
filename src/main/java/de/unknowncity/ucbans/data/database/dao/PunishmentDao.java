package de.unknowncity.ucbans.data.database.dao;

import de.unknowncity.ucbans.punishment.Punishment;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PunishmentDao {

    CompletableFuture<List<Punishment>> getPunishments(UUID playerUniqueId);
    CompletableFuture<List<Punishment>> getAllPunishments();
    CompletableFuture<Boolean> updatePunishment(Punishment punishment);
    CompletableFuture<Boolean> deleteAllInactivePunishments(UUID playerUniqueId);
    CompletableFuture<Boolean> addPunishment(Punishment punishment);

}
