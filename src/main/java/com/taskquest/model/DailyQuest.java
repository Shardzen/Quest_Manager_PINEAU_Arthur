package com.taskquest.model;

import java.time.LocalDate;

/**
 * Quête quotidienne qui se réinitialise automatiquement chaque jour.
 * <p>
 * Si la quête a été terminée un jour précédent, son statut repasse à
 * {@link QuestStatus#TODO} au prochain démarrage de l'application.
 * </p>
 */
public class DailyQuest extends Quest {

    private LocalDate lastCompletedDate;

    /**
     * Crée une nouvelle quête quotidienne.
     *
     * @param title       Titre de la quête
     * @param description Description de la quête
     * @param xpReward    Points d'XP accordés
     */
    public DailyQuest(String title, String description, int xpReward) {
        super(title, description, xpReward);
        this.lastCompletedDate = null;
    }

    /**
     * Constructeur pour reconstruction depuis la persistance.
     *
     * @param id                Identifiant unique
     * @param title             Titre de la quête
     * @param description       Description
     * @param xpReward          Points d'XP
     * @param status            Statut sauvegardé
     * @param lastCompletedDate Date de dernière complétion (peut être null)
     */
    public DailyQuest(String id, String title, String description, int xpReward,
                      QuestStatus status, LocalDate lastCompletedDate) {
        super(id, title, description, xpReward, status);
        this.lastCompletedDate = lastCompletedDate;
    }

    /**
     * Vérifie si la quête doit être réinitialisée et remet son statut à TODO si c'est le cas.
     * Doit être appelée au chargement des données.
     */
    public void checkAndReset() {
        if (status == QuestStatus.DONE
                && lastCompletedDate != null
                && lastCompletedDate.isBefore(LocalDate.now())) {
            status = QuestStatus.TODO;
        }
    }

    /** Marque la quête comme terminée et enregistre la date du jour. */
    @Override
    public void setStatus(QuestStatus status) {
        this.status = status;
        if (status == QuestStatus.DONE) {
            this.lastCompletedDate = LocalDate.now();
        }
    }

    /** @return "DAILY" */
    @Override
    public String getType() { return "DAILY"; }

    /** @return La date de dernière complétion, ou null si jamais accomplie */
    public LocalDate getLastCompletedDate() { return lastCompletedDate; }

    /**
     * @param lastCompletedDate La date de dernière complétion
     */
    public void setLastCompletedDate(LocalDate lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }
}
