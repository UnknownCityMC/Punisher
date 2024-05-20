package de.unknowncity.punisher.configuration.settings;

import de.unknowncity.punisher.punishment.PunishmentTemplate;

import java.util.Map;

public record TemplateSettings(Map<String, PunishmentTemplate> punishmentTemplates) {
}
