package de.unknowncity.ucbans.util;

import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.time.temporal.ChronoUnit.SECONDS;

public class UUIDFetcher {

    public static CompletableFuture<UUID> fetchUUID(String name) {
        var request = HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                .GET()
                .timeout(Duration.of(10, SECONDS))
                .build();
        var response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(stringHttpResponse -> UUID.fromString(JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")));
    }

    public static CompletableFuture<String> fetchName(UUID uuid) {
        var request = HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile" + uuid))
                .GET()
                .timeout(Duration.of(10, SECONDS))
                .build();
        var response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
        return response.thenApply(stringHttpResponse -> JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().get("name").getAsString());
    }
}
