package de.unknowncity.ucvelocity.core.message;

import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucvelocity.UCVelocityPlugin;
import de.unknowncity.ucvelocity.core.hooks.PluginHookService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

import java.util.Collection;

public class Messenger {
    private final ConfigurationNode rootNode;
    private final MiniMessage miniMessage;
    private final UCVelocityPlugin plugin;
    private final PluginHookService pluginHookService;

    public Messenger(UCVelocityPlugin plugin, ConfigurationNode rootNode) {
        this.plugin = plugin;
        this.rootNode = rootNode;
        this.miniMessage = MiniMessage.miniMessage();
        this.pluginHookService = plugin.pluginHookService();
    }

    public String getString(NodePath path) {
        return rootNode.node(path).getString() == null ? "N/A " + path : rootNode.node(path).getString();
    }

    public Component prefix() {
        var prefixString = rootNode.node("prefix").getString();
        return prefixString == null ? Component.text("N/A prefix") : miniMessage.deserialize(prefixString);
    }

    public Component component(NodePath path, CommandSource source, TagResolver... resolvers) {
        var messageString = rootNode.node(path).getString();
        if (messageString == null) {
            return Component.text("N/A " + path);
        }
        return miniMessage.deserialize(
                messageString,
                TagResolver.resolver(resolvers),
                TagResolver.resolver(Placeholder.component("prefix", prefix()))
        );
    }

    public Component component(NodePath path, TagResolver... resolvers) {
        var messageString = rootNode.node(path).getString();
        if (messageString == null) {
            return Component.text("N/A " + path);
        }
        return miniMessage.deserialize(
                messageString,
                TagResolver.resolver(resolvers),
                TagResolver.resolver(Placeholder.component("prefix", prefix()))
        );
    }

    public void sendTitle(CommandSource source, NodePath pathTitle, NodePath pathSubTitle, TagResolver... tagResolvers) {
        var title = Title.title(
                pathTitle == null ? Component.empty() : component(pathTitle, source, tagResolvers),
                pathSubTitle == null ? Component.empty() : component(pathSubTitle, source, tagResolvers)
        );
        source.showTitle(title);
    }

    public void broadcastTitleToProxy(NodePath pathTitle, NodePath pathSubTitle, TagResolver... tagResolvers) {
        broadcastTitleToSources(plugin.server().getAllPlayers(), pathTitle, pathSubTitle, tagResolvers);
    }

    public void broadcastTitleToSources(Collection<? extends CommandSource> sources, NodePath pathTitle, NodePath pathSubTitle, TagResolver... tagResolvers) {
        sources.forEach(source -> sendTitle(source, pathTitle, pathSubTitle, tagResolvers));
    }

    public void sendActionBar(CommandSource source, NodePath path, TagResolver... tagResolvers) {
        source.sendActionBar(this.component(path, source, tagResolvers));
    }

    public void sendMessage(CommandSource sender, NodePath path, TagResolver... tagResolvers) {
        sender.sendMessage(this.component(path, tagResolvers));
    }

    public void broadcastActionBarToSources(Collection<? extends CommandSource> sources, NodePath path, TagResolver... tagResolvers) {
        sources.forEach(source -> this.sendActionBar(source, path, tagResolvers));
    }

    public void broadcastActionBarToProxy(NodePath path, TagResolver... tagResolvers) {
        broadcastActionBarToSources(plugin.server().getAllPlayers(), path, tagResolvers);
    }

    public void broadcastTosources(Collection<? extends CommandSource> sources, NodePath path, TagResolver... tagResolvers) {
        sources.forEach(source -> sendMessage(source, path, tagResolvers));
    }

    public void broadcastToServer(NodePath path, TagResolver... tagResolvers) {
        this.broadcastTosources(plugin.server().getAllPlayers(), path, tagResolvers);
    }
}
