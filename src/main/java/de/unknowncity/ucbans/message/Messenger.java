package de.unknowncity.ucbans.message;

import com.velocitypowered.api.command.CommandSource;
import de.unknowncity.ucbans.UCBansPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class Messenger {
    private final ConfigurationNode rootNode;
    private final MiniMessage miniMessage;
    private final UCBansPlugin plugin;

    public Messenger(UCBansPlugin plugin, ConfigurationNode rootNode) {
        this.plugin = plugin;
        this.rootNode = rootNode;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public String getString(NodePath path) {
        return rootNode.node(path).getString() == null ? "N/A " + path : rootNode.node(path).getString();
    }

    public Component prefix() {
        var prefixString = rootNode.node("prefix").getString();
        return prefixString == null ? Component.text("N/A prefix") : miniMessage.deserialize(prefixString);
    }

    public Component componentFromList(NodePath path, TagResolver... resolvers) {
        List<String> messageStringList = null;
        try {
            messageStringList = rootNode.node(path).getList(String.class);
        } catch (SerializationException e) {
            return Component.text("N/A " + path);
        }

        if (messageStringList == null) {
            return Component.text("N/A " + path);
        }

        var component = Component.text();
        plugin.logger().info(messageStringList.toString());

        messageStringList.forEach(messageString -> {
            component.appendNewline();
            component.append(miniMessage.deserialize(
                    messageString,
                    TagResolver.resolver(resolvers),
                    TagResolver.resolver(Placeholder.component("prefix", prefix()))
            ));
        });
        return component.build();
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
                pathTitle == null ? Component.empty() : component(pathTitle,tagResolvers),
                pathSubTitle == null ? Component.empty() : component(pathSubTitle, tagResolvers)
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
        source.sendActionBar(this.component(path, tagResolvers));
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
