package de.unknowncity.punisher.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDFetcherTest {

    @Test
    void fetchUUID() {
        UUIDFetcher.fetchUUID("TheZexquex")
                .thenAccept(uuid -> assertEquals(uuid.toString(), "d094a1b3-0527-499e-8e6f-1363918b319c"));
    }

    @Test
    void fetchName() {
        UUIDFetcher.fetchName(
                UUID.fromString("d094a1b3-0527-499e-8e6f-1363918b319c"))
                .thenAccept(name -> assertEquals(name, "TheZexquex"));
    }
}