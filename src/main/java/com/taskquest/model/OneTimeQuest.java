package com.taskquest.model;

/**
 * Quête à accomplir une seule fois.
 * <p>
 * Contrairement à {@link DailyQuest}, une fois terminée elle reste
 * définitivement dans l'état {@link QuestStatus#DONE}.
 * </p>
 */
public class OneTimeQuest extends Quest {

    /**
     * Crée une nouvelle quête unique.
     *
     * @param title       Titre de la quête
     * @param description Description de la quête
     * @param xpReward    Points d'XP accordés
     */
    public OneTimeQuest(String title, String description, int xpReward) {
        super(title, description, xpReward);
    }

    /**
     * Constructeur pour reconstruction depuis la persistance.
     *
     * @param id          Identifiant unique
     * @param title       Titre de la quête
     * @param description Description
     * @param xpReward    Points d'XP
     * @param status      Statut sauvegardé
     */
    public OneTimeQuest(String id, String title, String description, int xpReward, QuestStatus status) {
        super(id, title, description, xpReward, status);
    }

    /** @return "ONETIME" */
    @Override
    public String getType() { return "ONETIME"; }
}
