package de.unknowncity.punisher.data.future;

import de.unknowncity.punisher.PunisherPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BukkitFutureResult<T> {
    private final CompletableFuture<T> completableFuture;

    private BukkitFutureResult(CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
    }

    public static <T> BukkitFutureResult<T> of(CompletableFuture<T> completableFuture) {
        return new BukkitFutureResult<>(completableFuture);
    }

    public void whenComplete(PunisherPlugin plugin, Consumer<? super T> callback, Consumer<Throwable> throwableConsumer) {
        var executor = (Executor) r -> plugin.server().getScheduler().buildTask(plugin, r).schedule();
        this.completableFuture.thenAcceptAsync(callback, executor).exceptionally(throwable -> {
            throwableConsumer.accept(throwable);
            return null;
        });
    }

    public void whenComplete(PunisherPlugin plugin, Consumer<? super T> callback) {
        whenComplete(plugin, callback, throwable ->
                plugin.logger().log(Level.SEVERE, "Exception in Future Result", throwable));
    }
}
