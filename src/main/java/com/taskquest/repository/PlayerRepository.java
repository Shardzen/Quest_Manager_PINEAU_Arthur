package com.taskquest.repository;

import com.google.gson.*;
import com.taskquest.exception.DataCorruptedException;
import com.taskquest.model.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Gère la persistance du profil joueur dans un fichier JSON.
 * <p>
 * Vérifie l'intégrité minimale du fichier au chargement et signale
 * toute corruption via {@link DataCorruptedException}.
 * </p>
 */
public class PlayerRepository {

    private static final String FILE_NAME = "player.json";

    private final Path dataDir;
    private final Gson gson;

    /**
     * @param dataDir Le répertoire où stocker le fichier player.json
     */
    public PlayerRepository(Path dataDir) {
        this.dataDir = dataDir;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Sauvegarde le profil du joueur dans le fichier JSON.
     *
     * @param player Le joueur à persister (ne doit pas être null)
     * @throws DataCorruptedException En cas d'erreur d'écriture sur disque
     */
    public void save(Player player) throws DataCorruptedException {
        try {
            Files.createDirectories(dataDir);
            Path file = dataDir.resolve(FILE_NAME);
            Files.writeString(file, gson.toJson(player), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataCorruptedException("Impossible de sauvegarder le joueur : " + e.getMessage(), e);
        }
    }

    /**
     * Charge le profil du joueur depuis le fichier JSON.
     * Retourne null si aucun fichier n'existe (première utilisation).
     *
     * @return Le joueur chargé, ou null si absent
     * @throws DataCorruptedException Si le fichier existe mais est corrompu ou invalide
     */
    public Player load() throws DataCorruptedException {
        Path file = dataDir.resolve(FILE_NAME);
        if (!Files.exists(file)) return null;

        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            if (content.isBlank()) return null;

            Player player = gson.fromJson(content, Player.class);
            if (player == null || player.getName() == null || player.getName().isBlank()) {
                throw new DataCorruptedException("Profil joueur corrompu : le champ 'name' est absent.");
            }
            if (player.getLevel() < 1 || player.getLevel() > Player.MAX_LEVEL) {
                throw new DataCorruptedException("Profil joueur corrompu : niveau invalide (" + player.getLevel() + ").");
            }
            return player;
        } catch (JsonParseException e) {
            throw new DataCorruptedException("Fichier joueur corrompu : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new DataCorruptedException("Impossible de lire le fichier joueur : " + e.getMessage(), e);
        }
    }
}
