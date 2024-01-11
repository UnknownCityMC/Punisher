package de.unknowncity.ucbans.punishment.types;

import de.unknowncity.ucbans.punishment.Punishment;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class InfinitePunishment extends Punishment {
    public InfinitePunishment(UUID playerUniqueId, UUID punisherUniqueId, String playerLastName, String punisherLastName, String reason, boolean active, LocalDateTime punishmentDateTime) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime);
    }
}
