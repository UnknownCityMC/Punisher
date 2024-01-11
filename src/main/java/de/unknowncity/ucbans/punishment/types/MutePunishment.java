package de.unknowncity.ucbans.punishment.types;

import com.velocitypowered.api.proxy.ProxyServer;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.PunishmentType;

import java.time.LocalDateTime;
import java.util.UUID;

public class MutePunishment extends TimedPunishment {
    public MutePunishment(UUID playerUniqueId, UUID punisherUniqueId, String playerLastName, String punisherLastName, String reason, boolean active, LocalDateTime punishmentDateTime, LocalDateTime punishmentEndDateTime) {
        super(playerUniqueId, punisherUniqueId, playerLastName, punisherLastName, reason, active, punishmentDateTime, punishmentEndDateTime);
        this.punishmentType = PunishmentType.MUTE;
    }

    @Override
    public void executeInitialPunishmentAction(ProxyServer proxyServer, Messenger messenger) {

    }
}
