package de.unknowncity.ucbans.punishment;

import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.message.Messenger;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Punishment {
    private UUID playerUniqueId;
    private UUID punisherUniqueId;
    private String playerLastName;
    private String punisherLastName;
    private LocalDateTime punishmentDateTime;
    private String reason;
    private boolean active;
    protected PunishmentType punishmentType;

    public Punishment(
            UUID playerUniqueId,
            UUID punisherUniqueId,
            String playerLastName,
            String punisherLastName,
            String reason,
            boolean active,
            LocalDateTime punishmentDateTime
    ) {
        this.playerUniqueId = playerUniqueId;
        this.punisherUniqueId = punisherUniqueId;
        this.playerLastName = playerLastName;
        this.punisherLastName = punisherLastName;
        this.reason = reason;
        this.active = active;
        this.punishmentDateTime = punishmentDateTime;
    }

    public abstract void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger);

    public UUID playerUniqueId() {
        return playerUniqueId;
    }

    public Punishment playerUniqueId(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
        return this;
    }

    public UUID punisherUniqueId() {
        return punisherUniqueId;
    }

    public Punishment punisherUniqueId(UUID punisherUniqueId) {
        this.punisherUniqueId = punisherUniqueId;
        return this;
    }

    public String playerLastName() {
        return playerLastName;
    }

    public Punishment playerLastName(String playerLastName) {
        this.playerLastName = playerLastName;
        return this;
    }

    public String punisherLastName() {
        return punisherLastName;
    }

    public Punishment punisherLastName(String punisherLastName) {
        this.punisherLastName = punisherLastName;
        return this;
    }

    public String reason() {
        return reason;
    }

    public Punishment reason(String reason) {
        this.reason = reason;
        return this;
    }

    public boolean active() {
        return active;
    }

    public Punishment active(boolean active) {
        this.active = active;
        return this;
    }

    public PunishmentType punishmentType() {
        return punishmentType;
    }

    public Punishment punishmentType(PunishmentType punishmentType) {
        this.punishmentType = punishmentType;
        return this;
    }

    public LocalDateTime punishmentDateTime() {
        return punishmentDateTime;
    }

    public Punishment punishmentDateTime(LocalDateTime punishmentDateTime) {
        this.punishmentDateTime = punishmentDateTime;
        return this;
    }
}
