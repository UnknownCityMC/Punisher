package de.unknowncity.punisher.configuration;

import de.unknowncity.punisher.configuration.settings.DataBaseSettings;
import de.unknowncity.punisher.configuration.settings.TemplateSettings;

public record Configuration(
        DataBaseSettings dataBaseSettings,
        TemplateSettings templateSettings
) {}
