package com.taskquest.view;

import com.taskquest.controller.PlayerController;
import com.taskquest.controller.QuestController;
import com.taskquest.exception.DataCorruptedException;
import com.taskquest.exception.InvalidQuestException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Fenêtre principale de l'application TaskQuest.
 * <p>
 * Orchestre les panneaux {@link PlayerPanel} et {@link QuestPanel},
 * gère l'initialisation au démarrage (chargement des données, premier lancement)
 * et la sauvegarde automatique à la fermeture.
 * </p>
 */
public class MainWindow extends JFrame {

    private static final Color BG_HEADER = new Color(14, 14, 26);
    private static final Color GOLD = new Color(200, 155, 50);
    private static final Color TEXT = new Color(220, 220, 235);

    private final PlayerController playerController;
    private final QuestController questController;

    private PlayerPanel playerPanel;
    private QuestPanel questPanel;

    /**
     * Crée la fenêtre principale et initialise tous les composants.
     *
     * @param questController  Le contrôleur de quêtes
     * @param playerController Le contrôleur joueur
     */
    public MainWindow(QuestController questController, PlayerController playerController) {
        this.questController = questController;
        this.playerController = playerController;
        buildUI();
        initialize();
    }

    /** Construit la structure graphique de la fenêtre. */
    private void buildUI() {
        setTitle("TaskQuest ⚔");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(820, 560));
        setPreferredSize(new Dimension(1000, 650));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(18, 18, 32));

        root.add(buildHeader(), BorderLayout.NORTH);

        playerPanel = new PlayerPanel(playerController);
        questPanel = new QuestPanel(questController, playerController, playerPanel, this);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, playerPanel, questPanel);
        split.setDividerLocation(220);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(new Color(18, 18, 32));

        root.add(split, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    /** Barre d'en-tête avec le titre de l'application. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_HEADER);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("⚔  TaskQuest");
        title.setForeground(GOLD);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Gestionnaire de quêtes gamifié");
        subtitle.setForeground(new Color(120, 120, 150));
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        header.add(subtitle, BorderLayout.EAST);

        return header;
    }

    /**
     * Charge les données persistées et gère le premier lancement.
     * En cas de données corrompues, propose de repartir de zéro.
     */
    private void initialize() {
        // Chargement du joueur
        try {
            playerController.loadPlayer();
        } catch (DataCorruptedException e) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Le profil du joueur est corrompu.\n" + e.getMessage()
                + "\n\nVoulez-vous créer un nouveau profil ? (les données corrompues seront ignorées)",
                "Données corrompues", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }

        // Première utilisation : demander le nom du joueur
        if (!playerController.hasPlayer()) {
            askForPlayerName();
        }

        // Chargement des quêtes
        try {
            questController.loadAll();
        } catch (DataCorruptedException e) {
            JOptionPane.showMessageDialog(this,
                "Certaines quêtes n'ont pas pu être chargées.\n" + e.getMessage()
                + "\n\nL'application continuera avec une liste vide.",
                "Données corrompues", JOptionPane.WARNING_MESSAGE);
        }

        // Rafraîchissement de l'interface
        playerPanel.refresh();
        questPanel.refresh();
    }

    /** Affiche le dialogue de création du premier personnage. */
    private void askForPlayerName() {
        String name = null;
        while (name == null || name.isBlank()) {
            name = JOptionPane.showInputDialog(this,
                "Bienvenue dans TaskQuest !\nQuel est le nom de votre aventurier ?",
                "Nouveau personnage", JOptionPane.QUESTION_MESSAGE);
            if (name == null) {
                System.exit(0);
            }
            name = name.trim();
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(this, "Le nom ne peut pas être vide.",
                    "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
                name = null;
            }
        }
        try {
            playerController.initializePlayer(name);
            playerController.savePlayer();
        } catch (InvalidQuestException | DataCorruptedException | com.taskquest.exception.PlayerNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Impossible de créer le joueur : " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /** Sauvegarde les données et ferme l'application proprement. */
    private void onClose() {
        try {
            questController.saveAll();
            if (playerController.hasPlayer()) {
                try {
                    playerController.savePlayer();
                } catch (com.taskquest.exception.PlayerNotFoundException ignored) {}
            }
        } catch (Exception e) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Erreur lors de la sauvegarde : " + e.getMessage()
                + "\n\nQuitter quand même ?",
                "Erreur de sauvegarde", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        dispose();
        System.exit(0);
    }
}
