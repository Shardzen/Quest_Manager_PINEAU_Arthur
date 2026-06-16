package com.taskquest;

import com.taskquest.controller.PlayerController;
import com.taskquest.controller.QuestController;
import com.taskquest.view.MainWindow;

import javax.swing.*;

/**
 * Point d'entrée principal de l'application TaskQuest.
 * Lance l'interface graphique sur l'Event Dispatch Thread (EDT) de Swing.
 */
public class Main {

    /**
     * Lance l'application.
     *
     * @param args Arguments de la ligne de commande (ignorés)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fallback sur le look & feel par défaut si le système n'est pas disponible
            }

            PlayerController playerController = new PlayerController();
            QuestController questController = new QuestController(playerController);

            MainWindow window = new MainWindow(questController, playerController);
            window.setVisible(true);
        });
    }
}
