package de.unknowncity.ucbans.configuration;

import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;

public record Configuration(
        DataBaseSettings dataBaseSettings
) {}
