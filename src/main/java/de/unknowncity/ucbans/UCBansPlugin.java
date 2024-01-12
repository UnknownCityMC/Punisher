package de.unknowncity.ucbans;

import cloud.commandframework.CommandManager;
import cloud.commandframework.captions.CaptionRegistry;
import cloud.commandframework.captions.FactoryDelegatingCaptionRegistry;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.zaxxer.hikari.HikariDataSource;
import de.unknowncity.ucbans.command.*;
import de.unknowncity.ucbans.configuration.Configuration;
import de.unknowncity.ucbans.configuration.ConfigurationLoader;
import de.unknowncity.ucbans.configuration.settings.DataBaseSettings;
import de.unknowncity.ucbans.configuration.settings.TemplateSettings;
import de.unknowncity.ucbans.configuration.typeserializer.ConfigurationTypeSerializer;
import de.unknowncity.ucbans.configuration.typeserializer.DatabaseSettingsTypeSerializer;
import de.unknowncity.ucbans.configuration.typeserializer.TemplateSettingsTypeSerializer;
import de.unknowncity.ucbans.configuration.typeserializer.TemplateTypeSerializer;
import de.unknowncity.ucbans.data.database.DataBaseProvider;
import de.unknowncity.ucbans.data.database.DataBaseUpdater;
import de.unknowncity.ucbans.data.database.dao.MariaDBPunishmentDao;
import de.unknowncity.ucbans.data.service.PunishmentService;
import de.unknowncity.ucbans.listener.LoginListener;
import de.unknowncity.ucbans.message.Messenger;
import de.unknowncity.ucbans.punishment.PunishmentTemplate;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Plugin(
        id = "ucbans",
        name = "UCBans",
        version = "0.1.0"
)
public class UCBansPlugin {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private CommandManager<CommandSource> commandManager;
    private Messenger messenger;
    private PluginContainer pluginContainer;
    private PunishmentService punishmentService;
    private ConfigurationLoader configurationLoader;
    private Configuration configuration;
    private DataBaseProvider dataBaseProvider;
    private DataBaseUpdater dataBaseUpdater;
    private HikariDataSource dataSource;

    private Path dataDirectory;

    public static final boolean IS_DEV_BUILD = true;

    @Inject
    public UCBansPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        enablePlugin();
    }

    private void enablePlugin() {
        pluginContainer = proxyServer.getPluginManager().ensurePluginContainer(this);

        reloadConfig();

        updateAndConnectToDatabase(configuration.dataBaseSettings());
        punishmentService.cachePunishments();

        initCommandManager();
        registerCommands();
        registerListeners();
    }

    private void updateAndConnectToDatabase(DataBaseSettings dataBaseSettings) {

        this.dataBaseProvider = new DataBaseProvider(dataBaseSettings);
        this.dataSource = this.dataBaseProvider.createDataSource();

        this.dataBaseUpdater = new DataBaseUpdater(dataSource, dataBaseSettings);
        try {
            dataBaseUpdater.update();
        } catch (IOException | SQLException e) {
            this.logger.log(Level.SEVERE, "Failed to update database", e);
        }

        var punishmentDao = new MariaDBPunishmentDao(dataSource);
        punishmentService = new PunishmentService(punishmentDao, proxyServer, messenger, this);
    }

    public void reloadConfig() {
        configurationLoader = new ConfigurationLoader(this);
        var configPath = Path.of("config.yml");
        var messagePath = Path.of("messages.yml");

        configurationLoader.saveDefaultConfigFile(configPath);
        configurationLoader.saveDefaultConfigFile(messagePath);

        var mainConfigurationLoader = YamlConfigurationLoader.builder()
                .path(dataDirectory.resolve(configPath))
                .defaultOptions(opts -> opts.serializers(build -> build.register(Configuration.class, new ConfigurationTypeSerializer())))
                .defaultOptions(opts -> opts.serializers(build -> build.register(PunishmentTemplate.class, new TemplateTypeSerializer())))
                .defaultOptions(opts -> opts.serializers(build -> build.register(TemplateSettings.class, new TemplateSettingsTypeSerializer())))
                .defaultOptions(opts -> opts.serializers(build -> build.register(DataBaseSettings.class, new DatabaseSettingsTypeSerializer())))
                .build();

        var messageConfigurationLoader = YamlConfigurationLoader.builder()
                .path(dataDirectory.resolve(messagePath)).build();

        try {
            var rooConfigNode = mainConfigurationLoader.load();
            var messageRootNode = messageConfigurationLoader.load();

            configuration = rooConfigNode.get(Configuration.class);
            messenger = new Messenger(this, messageRootNode);
        } catch (ConfigurateException e) {
            logger.log(Level.SEVERE, "Failed to load configuration", e);
        }
    }

    public void registerCommands() {
        new BanCommand(this).register(commandManager);
        new MuteCommand(this).register(commandManager);
        new KickCommand(this).register(commandManager);
        new UnbanCommand(this).register(commandManager);

        new HistoryCommand(this).register(commandManager);
        new ClearHistoryCommand(this).register(commandManager);
    }

    public void registerListeners() {
        var eventManager = proxyServer.getEventManager();
        eventManager.register(this, new LoginListener(this));
    }

    private void initCommandManager() {
        try {
            this.commandManager = new VelocityCommandManager<>(
                    pluginContainer,
                    proxyServer,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity());
        } catch (Exception e) {
            this.logger.log(Level.SEVERE, "Failed to initialize command manager", e);
        }

        final CaptionRegistry<CommandSource> registry = commandManager.captionRegistry();

        if (registry instanceof FactoryDelegatingCaptionRegistry<CommandSource> factoryRegistry) {
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_CHAR,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "char"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_COLOR,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "color"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_DURATION,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "duration"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "enum"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NUMBER,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "number"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "string"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "regex"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_UUID,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "uuid"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_NO_INPUT_PROVIDED,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "no-input"))
            );
            factoryRegistry.registerMessageFactory(
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_BOOLEAN,
                    (context, key) -> messenger.getString(NodePath.path("exception", "argument-parse", "boolean"))
            );
        }

        new MinecraftExceptionHandler<CommandSource>()
                .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, (o, exception) ->
                        messenger.component(NodePath.path("exception", "no-permission"), TagResolver.resolver(
                                "missing-permission",
                                Tag.preProcessParsed(((NoPermissionException) exception).getMissingPermission())
                        )))
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, (o, exception) ->
                        messenger.component(NodePath.path("exception", "invalid-sender"), TagResolver.resolver(
                                "valid-sender",
                                Tag.preProcessParsed(((InvalidCommandSenderException) exception).getRequiredSender().getSimpleName())
                        )))
                .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, (o, exception) ->
                        messenger.component(NodePath.path("exception", "invalid-syntax"), TagResolver.resolver(
                                "valid-syntax",
                                Tag.preProcessParsed(((InvalidSyntaxException) exception).getCorrectSyntax())
                        )))
                .withHandler(MinecraftExceptionHandler.ExceptionType.ARGUMENT_PARSING, (exception) ->
                        messenger.component(NodePath.path("exception", "invalid-argument"), TagResolver.resolver(
                                "cause",
                                Tag.preProcessParsed(exception.getCause().getMessage())
                        )))
                .withDecorator(component -> messenger.prefix().append(component))
                .apply(commandManager, commandSender -> commandSender);
    }

    public Messenger messenger() {
        return messenger;
    }
    public Logger logger() {
        return logger;
    }

    public ProxyServer server() {
        return proxyServer;
    }

    public Path dataDirectory() {
        return dataDirectory;
    }

    public PunishmentService punishmentService() {
        return punishmentService;
    }

    public Configuration configuration() {
        return configuration;
    }
}
