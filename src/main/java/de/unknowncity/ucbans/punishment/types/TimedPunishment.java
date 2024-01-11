package de.unknowncity.ucbans.punishment.types;

import de.unknowncity.ucbans.punishment.Punishment;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class TimedPunishment extends Punishment {

    private LocalDateTime punishmentEndDateTime;
    public TimedPunishment(UUID playerUniqueId, UUID punisherUniqueId, String playerLastName, String punisherLastName, String reason, boolean active, LocalDateTime punishmentDateTime, LocalDateTime punishmentEndDateTime) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime);
        this.punishmentEndDateTime = punishmentEndDateTime;
    }

    public LocalDateTime punishmentEndDateTime() {
        return punishmentEndDateTime;
    }

    public TimedPunishment punishmentEndDateTime(LocalDateTime punishmentEndDateTime) {
        this.punishmentEndDateTime = punishmentEndDateTime;
        return this;
    }
}
