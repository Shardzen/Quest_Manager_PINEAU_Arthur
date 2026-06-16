package com.taskquest.exception;

/**
 * Levée lorsque les données d'une quête sont invalides.
 * <p>
 * Exemples : titre vide, XP négative ou nulle, XP supérieure au maximum
 * autorisé, type de quête inconnu, champ obligatoire manquant.
 * </p>
 */
public class InvalidQuestException extends Exception {

    /**
     * @param message Description de l'erreur de validation
     */
    public InvalidQuestException(String message) {
        super(message);
    }

    /**
     * @param message Description de l'erreur
     * @param cause   La cause sous-jacente
     */
    public InvalidQuestException(String message, Throwable cause) {
        super(message, cause);
    }
}
