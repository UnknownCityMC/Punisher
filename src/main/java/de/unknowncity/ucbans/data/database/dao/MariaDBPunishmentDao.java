package de.unknowncity.ucbans.data.database.dao;

import de.unknowncity.ucbans.punishment.Punishment;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLPunishmentDao implements PunishmentDao {
    @Override
    public CompletableFuture<List<Punishment>> getPunishments(UUID playerUniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updatePunishment(Punishment punishment) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deletePunishment(Punishment punishment) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteAllPunishment(UUID playerUniqueId) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> addPunishment(Punishment punishment) {
        return null;
    }
}
