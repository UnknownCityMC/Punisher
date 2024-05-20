package de.unknowncity.ucbans.configuration;

import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;
import de.unknowncity.ucbans.configuration.settings.RedisSettings;
import de.unknowncity.ucbans.configuration.settings.TemplateSettings;

public record Configuration(
        DataBaseSettings dataBaseSettings,
        TemplateSettings templateSettings,
        RedisSettings redisSettings
) {}
