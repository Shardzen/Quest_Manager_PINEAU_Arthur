package com.taskquest.repository;

import com.google.gson.*;
import com.taskquest.exception.DataCorruptedException;
import com.taskquest.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gère la persistance des quêtes dans un fichier JSON.
 * <p>
 * Utilise Gson pour la sérialisation et gère la désérialisation polymorphe
 * du type abstrait {@link Quest} via un champ discriminant "type".
 * Toutes les données sont validées avant lecture et avant écriture.
 * </p>
 */
public class QuestRepository {

    private static final String FILE_NAME = "quests.json";

    private final Path dataDir;
    private final Gson gson;

    /**
     * @param dataDir Le répertoire où stocker le fichier quests.json
     */
    public QuestRepository(Path dataDir) {
        this.dataDir = dataDir;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Sauvegarde la liste complète des quêtes dans le fichier JSON.
     *
     * @param quests La liste à persister (ne doit pas être null)
     * @throws DataCorruptedException En cas d'erreur d'écriture sur disque
     */
    public void saveAll(List<Quest> quests) throws DataCorruptedException {
        try {
            Files.createDirectories(dataDir);
            Path file = dataDir.resolve(FILE_NAME);
            JsonArray array = new JsonArray();
            for (Quest q : quests) {
                array.add(toJson(q));
            }
            Files.writeString(file, gson.toJson(array), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataCorruptedException("Impossible d'écrire le fichier de quêtes : " + e.getMessage(), e);
        }
    }

    /**
     * Charge toutes les quêtes depuis le fichier JSON.
     * Retourne une liste vide si le fichier est absent.
     *
     * @return La liste des quêtes chargées
     * @throws DataCorruptedException Si le fichier existe mais est corrompu
     */
    public List<Quest> loadAll() throws DataCorruptedException {
        Path file = dataDir.resolve(FILE_NAME);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) return new ArrayList<>();

            JsonArray array = JsonParser.parseString(content).getAsJsonArray();
            List<Quest> quests = new ArrayList<>();
            for (JsonElement el : array) {
                Quest q = fromJson(el.getAsJsonObject());
                if (q != null) quests.add(q);
            }
            return quests;
        } catch (JsonParseException | IllegalStateException e) {
            throw new DataCorruptedException("Fichier de quêtes corrompu : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataCorruptedException("Impossible de lire le fichier de quêtes : " + e.getMessage(), e);
        }
    }

    /** Sérialise une quête en JsonObject avec le champ discriminant "type". */
    private JsonObject toJson(Quest q) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", q.getType());
        obj.addProperty("id", q.getId());
        obj.addProperty("title", q.getTitle());
        obj.addProperty("description", q.getDescription() != null ? q.getDescription() : "");
        obj.addProperty("xpReward", q.getXpReward());
        obj.addProperty("status", q.getStatus().name());
        if (q instanceof DailyQuest dq && dq.getLastCompletedDate() != null) {
            obj.addProperty("lastCompletedDate", dq.getLastCompletedDate().toString());
        }
        return obj;
    }

    /**
     * Désérialise un JsonObject en Quest en utilisant le champ "type".
     * Retourne null si l'objet est mal formé (quête ignorée silencieusement).
     */
    private Quest fromJson(JsonObject obj) {
        try {
            String type = obj.get("type").getAsString();
            String id = obj.get("id").getAsString();
            String title = obj.get("title").getAsString();
            String description = obj.has("description") ? obj.get("description").getAsString() : "";
            int xpReward = obj.get("xpReward").getAsInt();
            QuestStatus status = QuestStatus.valueOf(obj.get("status").getAsString());

            if ("DAILY".equals(type)) {
                LocalDate lastDate = null;
                if (obj.has("lastCompletedDate") && !obj.get("lastCompletedDate").isJsonNull()) {
                    lastDate = LocalDate.parse(obj.get("lastCompletedDate").getAsString());
                }
                DailyQuest dq = new DailyQuest(id, title, description, xpReward, status, lastDate);
                dq.checkAndReset();
                return dq;
            } else {
                return new OneTimeQuest(id, title, description, xpReward, status);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
