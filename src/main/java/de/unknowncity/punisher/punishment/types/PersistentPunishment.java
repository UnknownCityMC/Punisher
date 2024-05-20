package de.unknowncity.punisher.punishment.types;

import de.unknowncity.punisher.punishment.Punishment;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class PersistentPunishment extends Punishment {

    private int duration;
    public PersistentPunishment(
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime,
            int duration
    ) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime);
        this.duration = duration;
    }

    public PersistentPunishment(
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
        super(punishmentId, playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime);
        this.duration = duration;
    }

    public int durationInSeconds() {
        return duration;
    }

    public PersistentPunishment duration(int duration) {
        this.duration = duration;
        return this;
    }
}
