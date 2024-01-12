package de.unknowncity.ucbans.punishment;

import java.util.Set;

public class PunishmentTemplate {
    private String reason;
    private Set<PunishmentLevel> punishmentLevels;


    public PunishmentTemplate(String reason, Set<PunishmentLevel> punishmentLevels) {
        this.reason = reason;
        this.punishmentLevels = punishmentLevels;
    }

    public Set<PunishmentLevel> punishmentLevels() {
        return punishmentLevels;
    }


    public String reason() {
        return reason;
    }
}
