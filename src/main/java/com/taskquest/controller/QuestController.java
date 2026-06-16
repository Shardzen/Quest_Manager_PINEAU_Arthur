package com.taskquest.controller;

import com.taskquest.exception.DataCorruptedException;
import com.taskquest.exception.InvalidQuestException;
import com.taskquest.exception.PlayerNotFoundException;
import com.taskquest.model.*;
import com.taskquest.repository.QuestRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur des quêtes : CRUD + attribution XP au joueur.
 * La vue passe toujours par ce contrôleur, jamais par le repository directement.
 */
public class QuestController {

    // stockage dans le dossier perso de l'utilisateur
    private static final Path DATA_DIR = Paths.get(
        System.getProperty("user.home"), ".taskquest", "data"
    );

    private final QuestRepository repository;
    private final PlayerController playerController;
    private List<Quest> quests;

    /**
     * @param playerController le contrôleur joueur, utilisé pour addXP() quand une quête est terminée
     */
    public QuestController(PlayerController playerController) {
        this.repository       = new QuestRepository(DATA_DIR);
        this.playerController = playerController;
        this.quests           = new ArrayList<>();
    }

    /**
     * Charge les quêtes depuis le fichier JSON.
     *
     * @throws DataCorruptedException si le fichier est illisible ou corrompu
     */
    public void loadAll() throws DataCorruptedException {
        this.quests = repository.loadAll();
    }

    /**
     * Sauvegarde toutes les quêtes en JSON.
     *
     * @throws DataCorruptedException si l'écriture échoue
     */
    public void saveAll() throws DataCorruptedException {
        repository.saveAll(quests);
    }

    /**
     * Crée une nouvelle quête après validation des données.
     *
     * @param title       titre non vide, max {@link Quest#MAX_TITLE_LENGTH} chars
     * @param description description optionnelle, max {@link Quest#MAX_DESCRIPTION_LENGTH} chars
     * @param xpReward    entre 1 et {@link Reward#MAX_XP_REWARD}
     * @param type        "DAILY" ou "ONETIME"
     * @throws InvalidQuestException  si une valeur est invalide
     * @throws DataCorruptedException si la sauvegarde échoue
     */
    public void createQuest(String title, String description, int xpReward, String type)
            throws InvalidQuestException, DataCorruptedException {
        valider(title, description, xpReward);

        Quest q = switch (type) {
            case "DAILY"   -> new DailyQuest(title.trim(), description == null ? "" : description.trim(), xpReward);
            case "ONETIME" -> new OneTimeQuest(title.trim(), description == null ? "" : description.trim(), xpReward);
            default        -> throw new InvalidQuestException("Type inconnu : " + type);
        };

        quests.add(q);
        repository.saveAll(quests);
    }

    /**
     * @return copie de la liste complète des quêtes
     */
    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests);
    }

    /**
     * Filtre les quêtes par statut.
     *
     * @param status le statut voulu (null = toutes)
     * @return la liste filtrée
     */
    public List<Quest> getQuestsByStatus(QuestStatus status) {
        if (status == null) return getAllQuests();
        return quests.stream()
                .filter(q -> q.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Marque une quête comme terminée et donne l'XP au joueur.
     *
     * @param questId l'id de la quête
     * @return true si le joueur a monté de niveau
     * @throws InvalidQuestException   quête introuvable ou déjà terminée
     * @throws PlayerNotFoundException joueur non initialisé
     * @throws DataCorruptedException  erreur de sauvegarde
     */
    public boolean completeQuest(String questId)
            throws InvalidQuestException, PlayerNotFoundException, DataCorruptedException {
        Quest q = trouver(questId);
        if (q.getStatus() == QuestStatus.DONE)
            throw new InvalidQuestException("Cette quête est déjà terminée.");

        q.setStatus(QuestStatus.DONE);
        boolean levelUp = playerController.addXP(q.getXpReward());
        repository.saveAll(quests);
        return levelUp;
    }

    /**
     * Passe une quête en IN_PROGRESS.
     *
     * @param questId l'id de la quête
     * @throws InvalidQuestException  quête introuvable ou déjà terminée
     * @throws DataCorruptedException erreur de sauvegarde
     */
    public void startQuest(String questId) throws InvalidQuestException, DataCorruptedException {
        Quest q = trouver(questId);
        if (q.getStatus() == QuestStatus.DONE)
            throw new InvalidQuestException("La quête est déjà terminée.");
        q.setStatus(QuestStatus.IN_PROGRESS);
        repository.saveAll(quests);
    }

    /**
     * Supprime définitivement une quête.
     *
     * @param questId l'id de la quête à supprimer
     * @throws InvalidQuestException  quête introuvable
     * @throws DataCorruptedException erreur de sauvegarde
     */
    public void deleteQuest(String questId) throws InvalidQuestException, DataCorruptedException {
        quests.remove(trouver(questId));
        repository.saveAll(quests);
    }

    // recherche par id, lève une exception si introuvable
    private Quest trouver(String id) throws InvalidQuestException {
        return quests.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new InvalidQuestException("Quête introuvable : " + id));
    }

    // validation des champs avant création
    private void valider(String title, String description, int xpReward) throws InvalidQuestException {
        if (title == null || title.isBlank())
            throw new InvalidQuestException("Le titre ne peut pas être vide.");
        if (title.length() > Quest.MAX_TITLE_LENGTH)
            throw new InvalidQuestException("Titre trop long (max " + Quest.MAX_TITLE_LENGTH + " caractères).");
        if (description != null && description.length() > Quest.MAX_DESCRIPTION_LENGTH)
            throw new InvalidQuestException("Description trop longue (max " + Quest.MAX_DESCRIPTION_LENGTH + " caractères).");
        if (xpReward <= 0)
            throw new InvalidQuestException("La récompense XP doit être positive.");
        if (xpReward > Reward.MAX_XP_REWARD)
            throw new InvalidQuestException("La récompense XP ne peut pas dépasser " + Reward.MAX_XP_REWARD + ".");
    }
}
