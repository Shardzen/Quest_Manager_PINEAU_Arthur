package com.taskquest.controller;

import com.taskquest.exception.DataCorruptedException;
import com.taskquest.exception.InvalidQuestException;
import com.taskquest.exception.PlayerNotFoundException;
import com.taskquest.model.Player;
import com.taskquest.repository.PlayerRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gère le joueur : chargement, initialisation, sauvegarde et attribution d'XP.
 */
public class PlayerController {

    private static final Path DATA_DIR = Paths.get(
        System.getProperty("user.home"), ".taskquest", "data"
    );

    private static final int MAX_NOM_LENGTH = 50;

    private final PlayerRepository repository;
    private Player player;

    public PlayerController() {
        this.repository = new PlayerRepository(DATA_DIR);
    }

    /**
     * Charge le joueur depuis le fichier JSON.
     * Ne fait rien si le fichier n'existe pas (première utilisation).
     *
     * @throws DataCorruptedException si le fichier est corrompu
     */
    public void loadPlayer() throws DataCorruptedException {
        this.player = repository.load();
    }

    /**
     * Crée un nouveau joueur niveau 1 avec le nom donné.
     *
     * @param nom le nom de l'aventurier
     * @throws InvalidQuestException si le nom est vide ou trop long
     */
    public void initializePlayer(String nom) throws InvalidQuestException {
        if (nom == null || nom.isBlank())
            throw new InvalidQuestException("Le nom ne peut pas être vide.");
        if (nom.length() > MAX_NOM_LENGTH)
            throw new InvalidQuestException("Le nom ne peut pas dépasser " + MAX_NOM_LENGTH + " caractères.");
        this.player = new Player(nom.trim());
    }

    /**
     * @return le joueur actif
     * @throws PlayerNotFoundException si aucun joueur n'est chargé
     */
    public Player getPlayer() throws PlayerNotFoundException {
        if (player == null)
            throw new PlayerNotFoundException("Aucun joueur chargé.");
        return player;
    }

    /**
     * @return true si un joueur est déjà chargé en mémoire
     */
    public boolean hasPlayer() {
        return player != null;
    }

    /**
     * Ajoute de l'XP au joueur et sauvegarde immédiatement.
     *
     * @param xp l'XP à ajouter
     * @return true si le joueur a monté de niveau
     * @throws PlayerNotFoundException si le joueur n'est pas chargé
     * @throws DataCorruptedException  si la sauvegarde échoue
     */
    public boolean addXP(int xp) throws PlayerNotFoundException, DataCorruptedException {
        Player p = getPlayer();
        int niveauAvant = p.getLevel();
        p.addXP(xp);
        repository.save(p);
        return p.getLevel() > niveauAvant;
    }

    /**
     * Sauvegarde le profil du joueur.
     *
     * @throws DataCorruptedException  si l'écriture échoue
     * @throws PlayerNotFoundException si le joueur n'est pas chargé
     */
    public void savePlayer() throws DataCorruptedException, PlayerNotFoundException {
        repository.save(getPlayer());
    }
}
