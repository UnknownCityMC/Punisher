package de.unknowncity.punisher.configuration.typeserializer;

import de.unknowncity.punisher.configuration.settings.TemplateSettings;
import de.unknowncity.punisher.punishment.PunishmentTemplate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;

public class TemplateSettingsTypeSerializer implements TypeSerializer<TemplateSettings> {
    @Override
    public TemplateSettings deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var templateMap = new HashMap<String, PunishmentTemplate>();
        var templateChildren =  node.childrenMap();

        for (Object key : templateChildren.keySet()) {
            templateMap.put(key.toString(), templateChildren.get(key).get(PunishmentTemplate.class));
        }

        return new TemplateSettings(templateMap);
    }

    @Override
    public void serialize(Type type, @Nullable TemplateSettings obj, ConfigurationNode node) throws SerializationException {
        // Config currently does not need to be saved (Replace with AstraLib when it's ready for Velocity)
    }
}
