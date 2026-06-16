package com.taskquest.model;

/**
 * Statut possible d'une quête dans TaskQuest.
 */
public enum QuestStatus {
    /** Quête créée mais non commencée. */
    TODO,
    /** Quête en cours de réalisation. */
    IN_PROGRESS,
    /** Quête terminée avec succès. */
    DONE
}
