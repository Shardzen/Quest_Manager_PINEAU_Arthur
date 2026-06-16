package com.taskquest.model;

import java.util.UUID;

/**
 * Classe abstraite représentant une quête dans TaskQuest.
 * <p>
 * Une quête possède un identifiant unique, un titre, une description,
 * une récompense en XP et un statut de progression. Les sous-classes
 * définissent le comportement spécifique (quête quotidienne ou unique).
 * </p>
 */
public abstract class Quest {

    /** Longueur maximale autorisée pour le titre (Security by design). */
    public static final int MAX_TITLE_LENGTH = 100;

    /** Longueur maximale autorisée pour la description. */
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    protected String id;
    protected String title;
    protected String description;
    protected int xpReward;
    protected QuestStatus status;

    /**
     * Constructeur pour une nouvelle quête (génère un UUID).
     *
     * @param title       Titre de la quête
     * @param description Description de la quête
     * @param xpReward    Points d'XP accordés à la complétion
     */
    protected Quest(String title, String description, int xpReward) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.status = QuestStatus.TODO;
    }

    /**
     * Constructeur pour reconstruction depuis la persistance.
     *
     * @param id          Identifiant unique existant
     * @param title       Titre de la quête
     * @param description Description de la quête
     * @param xpReward    Points d'XP
     * @param status      Statut actuel
     */
    protected Quest(String id, String title, String description, int xpReward, QuestStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.status = status;
    }

    /**
     * Retourne le type de quête utilisé pour la sérialisation JSON.
     *
     * @return "DAILY" pour {@link DailyQuest}, "ONETIME" pour {@link OneTimeQuest}
     */
    public abstract String getType();

    /** @return L'identifiant unique de la quête */
    public String getId() { return id; }

    /** @return Le titre de la quête */
    public String getTitle() { return title; }

    /** @return La description de la quête */
    public String getDescription() { return description; }

    /** @return La récompense en XP */
    public int getXpReward() { return xpReward; }

    /** @return Le statut actuel de la quête */
    public QuestStatus getStatus() { return status; }

    /**
     * Modifie le statut de la quête.
     *
     * @param status Le nouveau statut
     */
    public void setStatus(QuestStatus status) { this.status = status; }

    /**
     * @param title Le nouveau titre
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * @param description La nouvelle description
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * @param xpReward La nouvelle récompense en XP
     */
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d XP) — %s", getType(), title, xpReward, status);
    }
}
