package manager;

import com.google.gson.*;
import commands.LocalDateTimeAdapter;
import model.HumanBeing;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;


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
        int maxId = 0;
        try (FileReader reader = new FileReader(fileName)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                try {
                    HumanBeing humanBeing = gson.fromJson(element, HumanBeing.class);
                    if (humanBeing == null) continue;
                    collectionManager.addLoadedHumanBeing(humanBeing);
                    if (humanBeing.getId() > maxId) {
                        maxId = humanBeing.getId();
                    }
                } catch (Exception e) {
                    System.out.println("Skipped invalid element: " + element);
                }
            }
            collectionManager.setNextId(maxId + 1);
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
