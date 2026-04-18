package manager;

import com.google.gson.*;
import commands.LocalDateTimeAdapter;
import model.HumanBeing;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.UUID;


public class CollectionLoader {
    private final CollectionManager collectionManager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    public CollectionLoader(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public void loadCollection(String fileName) throws FileNotFoundException {
        try (FileReader reader = new FileReader(fileName)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                try {
                    normalizeId(element);
                    HumanBeing humanBeing = gson.fromJson(element, HumanBeing.class);
                    if (humanBeing == null) continue;
                    collectionManager.addLoadedHumanBeing(humanBeing);
                } catch (Exception e) {
                    System.out.println("Skipped invalid element: " + element);
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private void normalizeId(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement id = object.get("id");
        if (id == null || id.isJsonNull() || !id.isJsonPrimitive() || !isUUID(id.getAsString())) {
            object.addProperty("id", UUID.randomUUID().toString());
        }
    }

    private boolean isUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
