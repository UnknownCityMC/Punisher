package de.unknowncity.ucbans.util;

import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.time.temporal.ChronoUnit.SECONDS;

public class UUIDFetcher {

    public static CompletableFuture<Optional<UUID>> fetchUUID(String name) {
        var request = HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.of(10, SECONDS))
                .build();
        var response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(stringHttpResponse -> {
            var json = JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().get("id");
            return json == null ? Optional.empty() : Optional.of(
                    UUID.fromString(json.getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"))
            );
        });
    }

    public static CompletableFuture<Optional<String>> fetchName(UUID uuid) {
        var request = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile" + uuid))
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.of(10, SECONDS))
                .build();
        var response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(stringHttpResponse -> {
            var json = JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().get("name");
            return json == null ? Optional.empty() : Optional.of(json.getAsString());
        });
    }
}