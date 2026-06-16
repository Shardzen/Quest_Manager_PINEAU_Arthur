package com.taskquest.model;

/**
 * Représente le profil du joueur dans TaskQuest.
 * <p>
 * Gère la progression en XP, les montées de niveau (jusqu'à {@link Reward#MAX_LEVEL})
 * et le titre associé au niveau actuel.
 * </p>
 */
public class Player {

    /** Niveau maximum atteignable par le joueur. */
    public static final int MAX_LEVEL = Reward.MAX_LEVEL;

    private String name;
    private int level;
    private int currentXP;
    private int totalXP;
    private String title;

    /**
     * Crée un nouveau joueur au niveau 1 avec 0 XP.
     *
     * @param name Le nom de l'aventurier
     */
    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.currentXP = 0;
        this.totalXP = 0;
        this.title = Reward.getTitleForLevel(1);
    }

    /**
     * Constructeur pour reconstruction depuis la persistance.
     *
     * @param name      Nom du joueur
     * @param level     Niveau actuel
     * @param currentXP XP accumulée dans le niveau courant
     * @param totalXP   XP totale gagnée depuis le début
     * @param title     Titre actuel
     */
    public Player(String name, int level, int currentXP, int totalXP, String title) {
        this.name = name;
        this.level = level;
        this.currentXP = currentXP;
        this.totalXP = totalXP;
        this.title = title;
    }

    /**
     * Ajoute des points d'expérience et déclenche les montées de niveau si nécessaire.
     * Sans effet si le joueur est déjà au niveau maximum ou si {@code xp <= 0}.
     *
     * @param xp Le nombre de points XP à ajouter (doit être strictement positif)
     */
    public void addXP(int xp) {
        if (xp <= 0 || level >= MAX_LEVEL) return;
        currentXP += xp;
        totalXP += xp;
        checkLevelUp();
    }

    /** Vérifie et applique les montées de niveau en cascade. */
    private void checkLevelUp() {
        while (level < MAX_LEVEL) {
            int xpNeeded = Reward.getXPForNextLevel(level);
            if (currentXP >= xpNeeded) {
                currentXP -= xpNeeded;
                level++;
                title = Reward.getTitleForLevel(level);
            } else {
                break;
            }
        }
        if (level >= MAX_LEVEL) {
            level = MAX_LEVEL;
            title = Reward.getTitleForLevel(MAX_LEVEL);
        }
    }

    /**
     * Retourne l'XP nécessaire pour atteindre le niveau suivant.
     *
     * @return L'XP requise, ou 0 si le joueur est au niveau maximum
     */
    public int getXPForNextLevel() {
        return Reward.getXPForNextLevel(level);
    }

    /** @return Le nom du joueur */
    public String getName() { return name; }

    /** @return Le niveau actuel (1 à {@value #MAX_LEVEL}) */
    public int getLevel() { return level; }

    /** @return L'XP accumulée dans le niveau courant */
    public int getCurrentXP() { return currentXP; }

    /** @return L'XP totale gagnée depuis le début */
    public int getTotalXP() { return totalXP; }

    /** @return Le titre associé au niveau actuel */
    public String getTitle() { return title; }

    /** @param name Le nouveau nom du joueur */
    public void setName(String name) { this.name = name; }

    /** @param level Le niveau (utilisé par la désérialisation) */
    public void setLevel(int level) { this.level = level; }

    /** @param currentXP L'XP courante (utilisée par la désérialisation) */
    public void setCurrentXP(int currentXP) { this.currentXP = currentXP; }

    /** @param totalXP L'XP totale (utilisée par la désérialisation) */
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    /** @param title Le titre (utilisé par la désérialisation) */
    public void setTitle(String title) { this.title = title; }
}
