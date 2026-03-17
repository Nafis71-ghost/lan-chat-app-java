package protocol;

import com.google.gson.Gson;
import model.ChatMessage;

public final class JsonUtil {
    private static final Gson GSON = new Gson();

    private JsonUtil() {}

    public static String toJson(ChatMessage message) {
        return GSON.toJson(message);
    }

    public static ChatMessage fromJson(String json) {
        return GSON.fromJson(json, ChatMessage.class);
    }
}