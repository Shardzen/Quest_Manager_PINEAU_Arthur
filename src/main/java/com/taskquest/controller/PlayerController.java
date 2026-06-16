package com.taskquest.controller;

import com.taskquest.exception.DataCorruptedException;
import com.taskquest.exception.InvalidQuestException;
import com.taskquest.exception.PlayerNotFoundException;
import com.taskquest.model.Player;
import com.taskquest.repository.PlayerRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur gérant le cycle de vie du joueur : initialisation, chargement,
 * sauvegarde et attribution d'XP.
 * <p>
 * Ce contrôleur est le seul point d'accès au {@link Player} depuis la couche vue.
 * Toute lecture ou écriture de données passe par le {@link PlayerRepository}.
 * </p>
 */
public class PlayerController {

    private static final Path DATA_DIR = Paths.get(
        System.getProperty("user.home"), ".taskquest", "data"
    );

    private static final int MAX_NAME_LENGTH = 50;

    private final PlayerRepository repository;
    private Player player;

    /** Crée le contrôleur et initialise le repository. */
    public PlayerController() {
        this.repository = new PlayerRepository(DATA_DIR);
    }

    /**
     * Charge le profil joueur depuis la persistance.
     * Ne fait rien si le fichier n'existe pas (première utilisation).
     *
     * @throws DataCorruptedException Si les données sont corrompues
     */
    public void loadPlayer() throws DataCorruptedException {
        this.player = repository.load();
    }

    /**
     * Crée un nouveau joueur avec le nom fourni.
     *
     * @param name Le nom de l'aventurier (non vide, max {@value #MAX_NAME_LENGTH} caractères)
     * @throws InvalidQuestException Si le nom est invalide
     */
    public void initializePlayer(String name) throws InvalidQuestException {
        if (name == null || name.isBlank()) {
            throw new InvalidQuestException("Le nom du joueur ne peut pas être vide.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidQuestException("Le nom ne peut pas dépasser " + MAX_NAME_LENGTH + " caractères.");
        }
        this.player = new Player(name.trim());
    }

    /**
     * Retourne le joueur actif.
     *
     * @return Le joueur actif
     * @throws PlayerNotFoundException Si aucun joueur n'a été créé ou chargé
     */
    public Player getPlayer() throws PlayerNotFoundException {
        if (player == null) {
            throw new PlayerNotFoundException("Aucun joueur chargé. Veuillez créer un personnage.");
        }
        return player;
    }

    /**
     * Indique si un joueur est actuellement chargé en mémoire.
     *
     * @return true si un joueur existe, false sinon
     */
    public boolean hasPlayer() {
        return player != null;
    }

    /**
     * Ajoute de l'XP au joueur et persiste immédiatement le résultat.
     *
     * @param xp L'XP à ajouter (doit être strictement positive)
     * @return true si le joueur a monté de niveau
     * @throws PlayerNotFoundException Si le joueur n'est pas chargé
     * @throws DataCorruptedException  En cas d'erreur de sauvegarde
     */
    public boolean addXP(int xp) throws PlayerNotFoundException, DataCorruptedException {
        Player p = getPlayer();
        int levelBefore = p.getLevel();
        p.addXP(xp);
        repository.save(p);
        return p.getLevel() > levelBefore;
    }

    /**
     * Sauvegarde le profil du joueur.
     *
     * @throws DataCorruptedException  En cas d'erreur d'écriture
     * @throws PlayerNotFoundException Si le joueur n'est pas chargé
     */
    public void savePlayer() throws DataCorruptedException, PlayerNotFoundException {
        repository.save(getPlayer());
    }
}
