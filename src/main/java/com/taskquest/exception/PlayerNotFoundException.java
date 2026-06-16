package com.taskquest.exception;

/**
 * Levée lorsque le profil du joueur est introuvable ou non initialisé.
 * <p>
 * Survient si une opération nécessitant un joueur actif est appelée
 * avant que le joueur n'ait été créé ou chargé depuis la persistance.
 * </p>
 */
public class PlayerNotFoundException extends Exception {

    /**
     * @param message Description du problème
     */
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
