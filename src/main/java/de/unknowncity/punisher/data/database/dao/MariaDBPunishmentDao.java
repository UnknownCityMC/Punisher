package de.unknowncity.punisher.data.database.dao;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.UpdateResult;
import de.unknowncity.punisher.punishment.Punishment;
import de.unknowncity.punisher.punishment.PunishmentType;
import de.unknowncity.punisher.punishment.types.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MariaDBPunishmentDao extends QueryFactory implements PunishmentDao {

    public MariaDBPunishmentDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public CompletableFuture<List<Punishment>> getPunishments(UUID playerUniqueId) {
        return builder(Punishment.class)
                .query(
                        "SELECT " +
                        "punishment_id, " +
                        "player_uuid, " +
                        "player_last_name, " +
                        "active, " +
                        "punisher_uuid, " +
                        "punisher_last_name, " +
                        "punishment_type, " +
                        "punishment_infinite, " +
                        "punishment_date, " +
                        "punishment_duration, " +
                        "punishment_reason " +
                        "FROM punishment " +
                        "WHERE player_uuid = ?"
                )
                .parameter(statement -> statement.setUuidAsString(playerUniqueId))
                .readRow(rs ->
                        switch (rs.getEnum("punishment_type", PunishmentType.class)) {
                            case BAN -> new BanPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                            case KICK -> new KickPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case MUTE -> new MutePunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                            case WARN -> new WarnPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                        }
                )
                .all();
    }

    @Override
    public CompletableFuture<List<Punishment>> getAllPunishments() {
        return builder(Punishment.class)
                .query(
                        "SELECT " +
                                "punishment_id, " +
                                "player_uuid, " +
                                "player_last_name, " +
                                "active, " +
                                "punisher_uuid, " +
                                "punisher_last_name, " +
                                "punishment_type, " +
                                "punishment_infinite, " +
                                "punishment_date, " +
                                "punishment_duration, " +
                                "punishment_reason " +
                                "FROM punishment "
                )
                .emptyParams()
                .readRow(rs ->
                        switch (rs.getEnum("punishment_type", PunishmentType.class)) {
                            case BAN -> new BanPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                            case KICK -> new KickPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case MUTE -> new MutePunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                            case WARN -> new WarnPunishment(
                                    rs.getInt("punishment_id"),
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getInt("punishment_duration")
                            );
                        }
                )
                .all();
    }

    @Override
    public CompletableFuture<Boolean> updatePunishment(Punishment punishment) {
        return builder()
                .query(
                        "UPDATE punishment SET " +
                                "player_last_name = ?, " +
                                "punisher_last_name = ?, " +
                                "punishment_infinite = ?, " +
                                "punishment_duration = ?, " +
                                "punishment_reason = ?, " +
                                "active = ? " +
                                "WHERE player_uuid = ? AND " +
                                "punisher_uuid = ? AND " +
                                "punishment_type = ? " +
                                "AND punishment_date = ?"
                )
                .parameter(stmt -> {
                    stmt.setString(punishment.playerLastName())
                            .setString(punishment.punisherLastName())
                            .setBoolean(punishment instanceof PersistentPunishment persistentPunishment && persistentPunishment.durationInSeconds() == -1)
                            .setInt(punishment instanceof PersistentPunishment persistentPunishment ? persistentPunishment.durationInSeconds() : 0)
                            .setString(punishment.reason())
                            .setBoolean(punishment.active())
                            .setUuidAsString(punishment.playerUniqueId())
                            .setUuidAsString(punishment.punisherUniqueId())
                            .setEnum(punishment.punishmentType())
                            .setLocalDateTime(punishment.punishmentDateTime());
                })
                .update()
                .send()
                .thenApply(UpdateResult::changed);
    }

    @Override
    public CompletableFuture<Boolean> deletePunishment(int id) {
        return builder()
                .query("DELETE FROM punishment WHERE punishment_id = ?")
                .parameter(stmt -> stmt.setInt(id))
                .delete()
                .send()
                .thenApply(UpdateResult::changed);
    }

    @Override
    public CompletableFuture<Boolean> deleteAllInactivePunishments(UUID playerUniqueId) {
        return builder()
                .query("DELETE FROM punishment WHERE player_uuid = ? AND active = 0")
                .parameter(stmt -> stmt.setUuidAsString(playerUniqueId))
                .delete()
                .send()
                .thenApply(UpdateResult::changed);
    }

    @Override
    public CompletableFuture<Boolean> addPunishment(Punishment punishment) {
        return builder()
                .query(
                        "INSERT INTO punishment(" +
                                "player_uuid, " +
                                "player_last_name, " +
                                "punisher_uuid, " +
                                "punisher_last_name, " +
                                "punishment_type, " +
                                "punishment_infinite, " +
                                "punishment_date, " +
                                "punishment_duration," +
                                "punishment_reason" +
                                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .parameter(stmt -> {
                    stmt.setUuidAsString(punishment.playerUniqueId())
                            .setString(punishment.playerLastName())
                            .setUuidAsString(punishment.punisherUniqueId())
                            .setString(punishment.punisherLastName())
                            .setEnum(punishment.punishmentType())
                            .setBoolean(punishment instanceof PersistentPunishment persistentPunishment && persistentPunishment.durationInSeconds() == -1)
                            .setLocalDateTime(punishment.punishmentDateTime())
                            .setInt(punishment instanceof PersistentPunishment persistentPunishment ? persistentPunishment.durationInSeconds() : 0)
                            .setString(punishment.reason());
                })
                .insert()
                .send()
                .thenApply(UpdateResult::changed);
    }
}
