package de.unknowncity.ucbans.configuration.settings;

import de.unknowncity.ucbans.punishment.PunishmentTemplate;

import java.util.Map;

public record TemplateSettings(Map<String, PunishmentTemplate> punishmentTemplates) {
}
