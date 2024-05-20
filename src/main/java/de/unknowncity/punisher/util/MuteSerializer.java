package de.unknowncity.punisher.util;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


public class MuteSerializer {
    public static String serialize(UUID uuid, LocalDateTime endDate) {
        var jsonObject = new JsonObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        jsonObject.addProperty("uuid", uuid.toString());
        jsonObject.addProperty("end-date", endDate.format(formatter));
        return jsonObject.toString();
    }
}
