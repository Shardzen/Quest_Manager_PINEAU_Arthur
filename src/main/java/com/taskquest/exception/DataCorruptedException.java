package com.taskquest.exception;

/**
 * Levée lorsque les données persistées sont corrompues ou illisibles.
 * <p>
 * Peut survenir si le fichier JSON est malformé, tronqué ou si les
 * champs obligatoires sont absents ou ont un type inattendu.
 * </p>
 */
public class DataCorruptedException extends Exception {

    /**
     * @param message Description de la corruption détectée
     */
    public DataCorruptedException(String message) {
        super(message);
    }

    /**
     * @param message Description de l'erreur
     * @param cause   La cause sous-jacente (ex. IOException, JsonParseException)
     */
    public DataCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
