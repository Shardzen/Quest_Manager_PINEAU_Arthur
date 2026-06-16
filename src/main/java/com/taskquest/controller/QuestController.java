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
 * Contrôleur principal gérant les opérations CRUD sur les quêtes.
 * <p>
 * Coordonne la couche modèle, le {@link QuestRepository} et le
 * {@link PlayerController} pour l'attribution d'XP à la complétion.
 * La couche vue ne doit jamais accéder au repository directement.
 * </p>
 */
public class QuestController {

    private static final Path DATA_DIR = Paths.get(
        System.getProperty("user.home"), ".taskquest", "data"
    );

    private final QuestRepository repository;
    private final PlayerController playerController;
    private List<Quest> quests;

    /**
     * @param playerController Le contrôleur joueur utilisé pour l'attribution de l'XP
     */
    public QuestController(PlayerController playerController) {
        this.repository = new QuestRepository(DATA_DIR);
        this.playerController = playerController;
        this.quests = new ArrayList<>();
    }

    /**
     * Charge toutes les quêtes depuis la persistance.
     *
     * @throws DataCorruptedException Si les données sont corrompues
     */
    public void loadAll() throws DataCorruptedException {
        this.quests = repository.loadAll();
    }

    /**
     * Sauvegarde toutes les quêtes actuelles.
     *
     * @throws DataCorruptedException En cas d'erreur d'écriture
     */
    public void saveAll() throws DataCorruptedException {
        repository.saveAll(quests);
    }

    /**
     * Crée et persiste une nouvelle quête après validation complète des données.
     *
     * @param title       Titre (non vide, max {@link Quest#MAX_TITLE_LENGTH} caractères)
     * @param description Description (max {@link Quest#MAX_DESCRIPTION_LENGTH} caractères)
     * @param xpReward    Récompense XP (1 à {@link Reward#MAX_XP_REWARD})
     * @param type        "DAILY" ou "ONETIME"
     * @throws InvalidQuestException  Si l'une des données est invalide
     * @throws DataCorruptedException En cas d'erreur de sauvegarde
     */
    public void createQuest(String title, String description, int xpReward, String type)
            throws InvalidQuestException, DataCorruptedException {
        validateInput(title, description, xpReward);

        Quest quest = switch (type) {
            case "DAILY"   -> new DailyQuest(title.trim(), description == null ? "" : description.trim(), xpReward);
            case "ONETIME" -> new OneTimeQuest(title.trim(), description == null ? "" : description.trim(), xpReward);
            default        -> throw new InvalidQuestException("Type de quête invalide : " + type);
        };

        quests.add(quest);
        repository.saveAll(quests);
    }

    /**
     * Retourne une copie de la liste de toutes les quêtes.
     *
     * @return La liste complète des quêtes
     */
    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests);
    }

    /**
     * Retourne les quêtes filtrées par statut.
     *
     * @param status Le statut souhaité, ou null pour retourner toutes les quêtes
     * @return La liste filtrée
     */
    public List<Quest> getQuestsByStatus(QuestStatus status) {
        if (status == null) return getAllQuests();
        return quests.stream()
                .filter(q -> q.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Marque une quête comme terminée et attribue l'XP au joueur.
     *
     * @param questId L'identifiant de la quête à terminer
     * @return true si le joueur a monté de niveau suite à cette action
     * @throws InvalidQuestException  Si la quête est introuvable ou déjà terminée
     * @throws PlayerNotFoundException Si le joueur n'est pas chargé
     * @throws DataCorruptedException  En cas d'erreur de sauvegarde
     */
    public boolean completeQuest(String questId)
            throws InvalidQuestException, PlayerNotFoundException, DataCorruptedException {
        Quest quest = findById(questId);
        if (quest.getStatus() == QuestStatus.DONE) {
            throw new InvalidQuestException("Cette quête est déjà terminée.");
        }
        quest.setStatus(QuestStatus.DONE);
        boolean leveledUp = playerController.addXP(quest.getXpReward());
        repository.saveAll(quests);
        return leveledUp;
    }

    /**
     * Passe une quête au statut {@link QuestStatus#IN_PROGRESS}.
     *
     * @param questId L'identifiant de la quête
     * @throws InvalidQuestException  Si la quête est introuvable ou déjà terminée
     * @throws DataCorruptedException En cas d'erreur de sauvegarde
     */
    public void startQuest(String questId) throws InvalidQuestException, DataCorruptedException {
        Quest quest = findById(questId);
        if (quest.getStatus() == QuestStatus.DONE) {
            throw new InvalidQuestException("Cette quête est déjà terminée et ne peut pas être redémarrée.");
        }
        quest.setStatus(QuestStatus.IN_PROGRESS);
        repository.saveAll(quests);
    }

    /**
     * Supprime définitivement une quête de la liste.
     *
     * @param questId L'identifiant de la quête à supprimer
     * @throws InvalidQuestException  Si la quête est introuvable
     * @throws DataCorruptedException En cas d'erreur de sauvegarde
     */
    public void deleteQuest(String questId) throws InvalidQuestException, DataCorruptedException {
        Quest quest = findById(questId);
        quests.remove(quest);
        repository.saveAll(quests);
    }

    /**
     * Recherche une quête par son identifiant.
     *
     * @param questId L'identifiant à rechercher
     * @return La quête correspondante
     * @throws InvalidQuestException Si aucune quête ne correspond
     */
    private Quest findById(String questId) throws InvalidQuestException {
        return quests.stream()
                .filter(q -> q.getId().equals(questId))
                .findFirst()
                .orElseThrow(() -> new InvalidQuestException("Quête introuvable : " + questId));
    }

    /**
     * Valide les champs d'une quête avant création.
     *
     * @throws InvalidQuestException Si une règle de validation est violée
     */
    private void validateInput(String title, String description, int xpReward)
            throws InvalidQuestException {
        if (title == null || title.isBlank()) {
            throw new InvalidQuestException("Le titre ne peut pas être vide.");
        }
        if (title.length() > Quest.MAX_TITLE_LENGTH) {
            throw new InvalidQuestException("Le titre ne peut pas dépasser " + Quest.MAX_TITLE_LENGTH + " caractères.");
        }
        if (description != null && description.length() > Quest.MAX_DESCRIPTION_LENGTH) {
            throw new InvalidQuestException("La description ne peut pas dépasser " + Quest.MAX_DESCRIPTION_LENGTH + " caractères.");
        }
        if (xpReward <= 0) {
            throw new InvalidQuestException("La récompense XP doit être strictement positive.");
        }
        if (xpReward > Reward.MAX_XP_REWARD) {
            throw new InvalidQuestException("La récompense XP ne peut pas dépasser " + Reward.MAX_XP_REWARD + ".");
        }
    }
}
