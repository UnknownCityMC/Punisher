package de.unknowncity.ucbans.punishment.types;

import de.unknowncity.ucbans.punishment.Punishment;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class PersistentPunishment extends Punishment {

    private LocalDateTime punishmentEndDateTime;
    private boolean infinite;
    public PersistentPunishment(UUID playerUniqueId, UUID punisherUniqueId, String playerLastName, String punisherLastName, String reason, boolean active, LocalDateTime punishmentDateTime, LocalDateTime punishmentEndDateTime, boolean infinite) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime);
        this.punishmentEndDateTime = punishmentEndDateTime;
        this.infinite = infinite;
    }

    public LocalDateTime punishmentEndDateTime() {
        return punishmentEndDateTime;
    }

    public PersistentPunishment punishmentEndDateTime(LocalDateTime punishmentEndDateTime) {
        this.punishmentEndDateTime = punishmentEndDateTime;
        return this;
    }

    public boolean infinite() {
        return infinite;
    }

    public PersistentPunishment infinite(boolean infinite) {
        this.infinite = infinite;
        return this;
    }
}
