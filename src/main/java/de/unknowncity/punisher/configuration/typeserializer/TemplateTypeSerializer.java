package de.unknowncity.punisher.configuration.typeserializer;

import de.unknowncity.punisher.punishment.PunishmentLevel;
import de.unknowncity.punisher.punishment.PunishmentTemplate;
import de.unknowncity.punisher.punishment.PunishmentType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class TemplateTypeSerializer implements TypeSerializer<PunishmentTemplate> {
    @Override
    public PunishmentTemplate deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var reason = node.node("reason").getString();
        var levelNodes = node.node("levels").childrenMap();

        var levels = levelNodes.keySet().stream().map(key -> new PunishmentLevel(
                Integer.parseInt(key.toString()),
                levelNodes.get(key).node("duration").getInt(),
                PunishmentType.valueOf(levelNodes.get(key).node("type").getString()))).collect(Collectors.toSet());

        return new PunishmentTemplate(reason, levels);
    }

    @Override
    public void serialize(Type type, @Nullable PunishmentTemplate punishmentTemplate, ConfigurationNode node) throws SerializationException {
        // Config currently does not need to be saved (Replace with AstraLib when it's ready for Velocity)
    }
}
