package com.taskquest.view;

import com.taskquest.controller.PlayerController;
import com.taskquest.exception.PlayerNotFoundException;
import com.taskquest.model.Player;
import com.taskquest.model.Reward;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panneau latéral du profil joueur.
 * Affiche nom, niveau, titre, barre XP et la progression vers les titres suivants.
 */
public class PlayerPanel extends JPanel {

    // couleurs du thème dark RPG
    private static final Color BG      = new Color(20, 20, 36);
    private static final Color GOLD    = new Color(220, 170, 60);
    private static final Color TEXT    = new Color(220, 220, 235);
    private static final Color SUBTEXT = new Color(130, 130, 158);
    private static final Color XP_COL  = new Color(90, 160, 255);
    private static final Color XP_BG   = new Color(38, 38, 62);

    // avatars qui changent selon le niveau (niveau 1 à 6)
    private static final String[] AVATARS = { "🗡", "⚔", "🛡", "🏹", "🔮", "👑" };

    // couleurs XP qui évoluent avec le niveau
    private static final Color[] LEVEL_COLORS = {
        new Color(120, 120, 200),
        new Color(80, 160, 255),
        new Color(60, 200, 140),
        new Color(220, 170, 60),
        new Color(220, 110, 50),
        new Color(220, 70, 200)
    };

    final PlayerController playerController;

    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel levelLabel;
    private JLabel titleLabel;
    private JProgressBar xpBar;
    private JLabel xpLabel;
    private JLabel nextTitleLabel;
    private JLabel totalXpLabel;
    private JPanel levelDotsPanel;

    /**
     * @param playerController le contrôleur pour lire les données du joueur
     */
    public PlayerPanel(PlayerController playerController) {
        this.playerController = playerController;
        buildUI();
    }

    private void buildUI() {
        setBackground(BG);
        setPreferredSize(new Dimension(230, 0));
        setBorder(new EmptyBorder(20, 14, 20, 14));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // --- avatar ---
        avatarLabel = new JLabel("⚔", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        avatarLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(avatarLabel);

        add(gap(6));

        // --- nom ---
        nameLabel = new JLabel("—", SwingConstants.CENTER);
        nameLabel.setForeground(GOLD);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(nameLabel);

        // --- titre actuel ---
        titleLabel = new JLabel("Novice", SwingConstants.CENTER);
        titleLabel.setForeground(new Color(175, 135, 75));
        titleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(titleLabel);

        add(sep());

        // --- niveau (gros) ---
        levelLabel = new JLabel("Niveau 1", SwingConstants.CENTER);
        levelLabel.setForeground(TEXT);
        levelLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        levelLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(levelLabel);

        add(gap(10));

        // --- barre XP ---
        xpBar = new JProgressBar(0, 100);
        xpBar.setValue(0);
        xpBar.setStringPainted(false);
        xpBar.setForeground(XP_COL);
        xpBar.setBackground(XP_BG);
        xpBar.setBorderPainted(false);
        xpBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        xpBar.setAlignmentX(CENTER_ALIGNMENT);
        add(xpBar);

        add(gap(4));

        xpLabel = new JLabel("0 / 100 XP", SwingConstants.CENTER);
        xpLabel.setForeground(XP_COL);
        xpLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        xpLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(xpLabel);

        add(gap(6));

        // indique quel titre on débloque ensuite
        nextTitleLabel = new JLabel(" ", SwingConstants.CENTER);
        nextTitleLabel.setForeground(new Color(140, 120, 180));
        nextTitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        nextTitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(nextTitleLabel);

        add(sep());

        // --- indicateur visuel des 6 niveaux (points) ---
        JLabel progressTitle = new JLabel("Progression", SwingConstants.CENTER);
        progressTitle.setForeground(SUBTEXT);
        progressTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressTitle.setAlignmentX(CENTER_ALIGNMENT);
        add(progressTitle);

        add(gap(6));

        levelDotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        levelDotsPanel.setBackground(BG);
        levelDotsPanel.setAlignmentX(CENTER_ALIGNMENT);
        add(levelDotsPanel);

        add(sep());

        // --- XP totale gagnée ---
        JLabel totalLabel = new JLabel("XP totale gagnée", SwingConstants.CENTER);
        totalLabel.setForeground(SUBTEXT);
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        totalLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(totalLabel);

        add(gap(2));

        totalXpLabel = new JLabel("0 XP", SwingConstants.CENTER);
        totalXpLabel.setForeground(TEXT);
        totalXpLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalXpLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(totalXpLabel);

        add(Box.createVerticalGlue());
    }

    /**
     * Met à jour tous les composants avec les données actuelles du joueur.
     * À appeler après chaque gain d'XP ou action qui modifie le joueur.
     */
    public void refresh() {
        try {
            Player player = playerController.getPlayer();
            int lvl = player.getLevel();

            // avatar selon niveau
            avatarLabel.setText(AVATARS[Math.min(lvl - 1, AVATARS.length - 1)]);
            nameLabel.setText(player.getName());
            levelLabel.setText("Niveau " + lvl);
            titleLabel.setText("« " + player.getTitle() + " »");

            // couleur XP qui évolue avec le niveau
            Color xpColor = LEVEL_COLORS[Math.min(lvl - 1, LEVEL_COLORS.length - 1)];
            xpBar.setForeground(xpColor);
            xpLabel.setForeground(xpColor);

            int current = player.getCurrentXP();
            int needed  = player.getXPForNextLevel();

            if (needed > 0) {
                xpBar.setMaximum(needed);
                xpBar.setValue(current);
                xpLabel.setText(current + " / " + needed + " XP");
                String nextTitle = Reward.getTitleForLevel(lvl + 1);
                nextTitleLabel.setText("→ " + nextTitle + " dans " + (needed - current) + " XP");
            } else {
                xpBar.setMaximum(1);
                xpBar.setValue(1);
                xpLabel.setText("NIVEAU MAX !");
                nextTitleLabel.setText("Vous avez atteint la Légende !");
            }

            totalXpLabel.setText(player.getTotalXP() + " XP");

            // mise à jour des points de niveau
            refreshLevelDots(lvl);

        } catch (PlayerNotFoundException ignored) {}

        revalidate();
        repaint();
    }

    // dessine les 6 points de niveau (remplis = atteints, vides = pas encore)
    private void refreshLevelDots(int currentLevel) {
        levelDotsPanel.removeAll();
        String[] titles = { "N", "A", "D", "V", "Ar", "L" }; // initiales des titres

        for (int i = 1; i <= Reward.MAX_LEVEL; i++) {
            final int lvl = i;
            Color dotColor = (i <= currentLevel) ? LEVEL_COLORS[i - 1] : new Color(45, 45, 68);

            JLabel dot = new JLabel(titles[i - 1], SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(dotColor);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            dot.setPreferredSize(new Dimension(26, 26));
            dot.setForeground(i <= currentLevel ? Color.WHITE : new Color(70, 70, 95));
            dot.setFont(new Font("Segoe UI", Font.BOLD, 9));
            dot.setToolTipText("Niveau " + i + " – " + Reward.getTitleForLevel(i));
            levelDotsPanel.add(dot);
        }

        levelDotsPanel.revalidate();
        levelDotsPanel.repaint();
    }

    private Component gap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    private Component sep() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(8, 0, 8, 0));
        JSeparator s = new JSeparator();
        s.setForeground(new Color(48, 48, 72));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.add(s);
        return p;
    }
}
