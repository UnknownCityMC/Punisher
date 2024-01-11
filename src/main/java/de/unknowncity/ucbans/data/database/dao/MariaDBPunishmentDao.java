package de.unknowncity.ucbans.data.database.dao;

import de.chojo.sadu.base.QueryFactory;
import de.chojo.sadu.wrapper.util.UpdateResult;
import de.unknowncity.ucbans.punishment.Punishment;
import de.unknowncity.ucbans.punishment.PunishmentType;
import de.unknowncity.ucbans.punishment.types.*;

import javax.sql.DataSource;
import java.time.LocalDateTime;
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
                        "player_uuid, " +
                        "player_last_name, " +
                        "active, " +
                        "punisher_uuid, " +
                        "punisher_last_name, " +
                        "punishment_type, " +
                        "punishment_infinite, " +
                        "punishment_date, " +
                        "punishment_end_date, " +
                        "punishment_reason " +
                        "FROM punishment " +
                        "WHERE player_uuid = ?"
                )
                .parameter(statement -> statement.setUuidAsString(playerUniqueId))
                .readRow(rs ->
                        switch (rs.getEnum("punishment_type", PunishmentType.class)) {
                            case TEMP_BAN -> new TempBanPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getLocalDateTime("punishment_end_date")
                            );
                            case BAN -> new BanPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case KICK -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case MUTE -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case WARN -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
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
                                "player_uuid, " +
                                "player_last_name, " +
                                "active, " +
                                "punisher_uuid, " +
                                "punisher_last_name, " +
                                "punishment_type, " +
                                "punishment_infinite, " +
                                "punishment_date, " +
                                "punishment_end_date, " +
                                "punishment_reason " +
                                "FROM punishment "
                )
                .emptyParams()
                .readRow(rs ->
                        switch (rs.getEnum("punishment_type", PunishmentType.class)) {
                            case BAN -> new BanPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case TEMP_BAN -> new TempBanPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date"),
                                    rs.getLocalDateTime("punishment_end_date")
                            );
                            case KICK -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case MUTE -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
                            );
                            case WARN -> new KickPunishment(
                                    rs.getUuidFromString("player_uuid"),
                                    rs.getUuidFromString("punisher_uuid"),
                                    rs.getString("player_last_name"),
                                    rs.getString("punisher_last_name"),
                                    rs.getString("punishment_reason"),
                                    rs.getBoolean("active"),
                                    rs.getLocalDateTime("punishment_date")
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
                                "punishment_end_date = ?, " +
                                "punishment_reason = ? " +
                                "WHERE player_uuid = ? AND " +
                                "punisher_uuid = ? AND " +
                                "punishment_type = ? " +
                                "AND punishment_date = ?"
                )
                .parameter(stmt -> {
                    stmt.setUuidAsString(punishment.playerUniqueId())
                            .setString(punishment.playerLastName())
                            .setString(punishment.punisherLastName())
                            .setBoolean(punishment instanceof InfinitePunishment)
                            .setLocalDateTime(punishment instanceof TimedPunishment persistentPunishment ? persistentPunishment.punishmentEndDateTime() : LocalDateTime.now())
                            .setString(punishment.reason())
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
    public CompletableFuture<Boolean> deleteAllPunishments(UUID playerUniqueId) {
        return builder()
                .query("DELETE FROM punishment WHERE player_uuid = ?")
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
                                "punishment_end_date," +
                                "punishment_reason" +
                                ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .parameter(stmt -> {
                    stmt.setUuidAsString(punishment.playerUniqueId())
                            .setString(punishment.playerLastName())
                            .setUuidAsString(punishment.punisherUniqueId())
                            .setString(punishment.punisherLastName())
                            .setEnum(punishment.punishmentType())
                            .setBoolean(punishment instanceof InfinitePunishment)
                            .setLocalDateTime(punishment.punishmentDateTime())
                            .setLocalDateTime(punishment instanceof TimedPunishment persistentPunishment ? persistentPunishment.punishmentEndDateTime() : LocalDateTime.now())
                            .setString(punishment.reason());
                })
                .insert()
                .send()
                .thenApply(UpdateResult::changed);
    }
}
