package com.taskquest.model;

/**
 * Définit les titres et seuils d'XP associés à chaque niveau du joueur.
 * Toutes les constantes de progression sont centralisées ici.
 */
public class Reward {

    /** Niveau maximum atteignable. */
    public static final int MAX_LEVEL = 6;

    /** Récompense XP maximale autorisée par quête (Security by design). */
    public static final int MAX_XP_REWARD = 1000;

    /** Titres débloqués à chaque niveau (index 0 = niveau 1). */
    private static final String[] TITLES = {
        "Novice", "Apprenti", "Développeur", "Vétéran", "Architecte", "Légende"
    };

    /** XP nécessaire pour passer du niveau N au niveau N+1 (index 0 = lvl 1→2). */
    public static final int[] XP_PER_LEVEL = {100, 200, 400, 800, 1600};

    private final int level;
    private final String title;

    /**
     * @param level Le niveau débloqué
     * @param title Le titre obtenu
     */
    public Reward(int level, String title) {
        this.level = level;
        this.title = title;
    }

    /**
     * Retourne le titre associé à un niveau donné.
     *
     * @param level Le niveau (1 à {@value #MAX_LEVEL})
     * @return Le titre correspondant, "Novice" si hors bornes
     */
    public static String getTitleForLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) return TITLES[0];
        return TITLES[level - 1];
    }

    /**
     * Retourne l'XP requise pour passer du niveau actuel au suivant.
     *
     * @param currentLevel Le niveau actuel du joueur
     * @return L'XP requise, ou 0 si niveau maximum atteint
     */
    public static int getXPForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return 0;
        return XP_PER_LEVEL[currentLevel - 1];
    }

    /** @return Le niveau associé à cette récompense */
    public int getLevel() { return level; }

    /** @return Le titre associé à cette récompense */
    public String getTitle() { return title; }
}
