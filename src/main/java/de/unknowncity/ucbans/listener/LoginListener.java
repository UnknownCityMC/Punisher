package de.unknowncity.ucbans.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import de.unknowncity.ucbans.UCBansPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.ExecutionException;

public class LoginListener {
    private final UCBansPlugin plugin;

    public LoginListener(UCBansPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(LoginEvent event) throws ExecutionException, InterruptedException {
        var uniqueId = event.getPlayer().getUniqueId();
        var isBanned = plugin.punishmentService().isBanned(uniqueId);

        if (isBanned) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text("Banned", NamedTextColor.RED)));
        }
    }
}
