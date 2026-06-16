package com.taskquest.view;

import com.taskquest.controller.PlayerController;
import com.taskquest.controller.QuestController;
import com.taskquest.exception.DataCorruptedException;
import com.taskquest.exception.InvalidQuestException;
import com.taskquest.exception.PlayerNotFoundException;
import com.taskquest.model.Quest;
import com.taskquest.model.QuestStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panneau principal de l'application : liste des quêtes + barre d'actions.
 * La vue passe toujours par le contrôleur, jamais par le repository directement.
 */
public class QuestPanel extends JPanel {

    // palette dark RPG
    private static final Color BG       = new Color(18, 18, 32);
    private static final Color SURFACE  = new Color(26, 26, 44);
    private static final Color CARD_BG  = new Color(32, 32, 52);
    private static final Color CARD_SEL = new Color(48, 48, 80);
    private static final Color ACCENT   = new Color(110, 110, 230);
    private static final Color GOLD     = new Color(220, 170, 60);
    private static final Color SUCCESS  = new Color(70, 195, 105);
    private static final Color DANGER   = new Color(215, 65, 65);
    private static final Color WARNING  = new Color(225, 145, 45);
    private static final Color TEXT     = new Color(218, 218, 232);
    private static final Color SUBTEXT  = new Color(130, 130, 158);
    private static final Color BORDER   = new Color(48, 48, 76);

    private final QuestController questController;
    private final PlayerController playerController;
    private final PlayerPanel playerPanel;
    private final JFrame parentFrame;

    private DefaultListModel<Quest> listModel;
    private JList<Quest> questList;
    private JComboBox<String> filterCombo;
    private JLabel statusBar;
    private JLabel questCountLabel;

    private JButton btnStart;
    private JButton btnComplete;
    private JButton btnDelete;

    /**
     * @param questController  gestion des quêtes (CRUD)
     * @param playerController pour récupérer le joueur après un gain d'XP
     * @param playerPanel      à rafraîchir quand le joueur gagne de l'XP
     * @param parentFrame      fenêtre parente pour les dialogues
     */
    public QuestPanel(QuestController questController, PlayerController playerController,
                      PlayerPanel playerPanel, JFrame parentFrame) {
        this.questController  = questController;
        this.playerController = playerController;
        this.playerPanel      = playerPanel;
        this.parentFrame      = parentFrame;
        buildUI();
    }

    private void buildUI() {
        setBackground(BG);
        setLayout(new BorderLayout());
        add(buildToolbar(),   BorderLayout.NORTH);
        add(buildListArea(),  BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    // barre haute : titre + filtre + bouton nouvelle quête
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(SURFACE);
        bar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setBackground(SURFACE);

        JLabel title = new JLabel("⚔  Mes Quêtes");
        title.setForeground(GOLD);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        left.add(title);

        questCountLabel = new JLabel("");
        questCountLabel.setForeground(SUBTEXT);
        questCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        left.add(questCountLabel);

        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setBackground(SURFACE);

        JLabel filterLbl = new JLabel("Filtre :");
        filterLbl.setForeground(SUBTEXT);
        filterLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        right.add(filterLbl);

        filterCombo = new JComboBox<>(new String[]{ "Toutes", "À faire", "En cours", "Terminées" });
        styleCombo(filterCombo);
        filterCombo.addActionListener(e -> refresh());
        right.add(filterCombo);

        JButton addBtn = btn("+ Nouvelle quête", ACCENT, Color.WHITE);
        addBtn.addActionListener(e -> openCreateDialog());
        right.add(addBtn);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // zone scrollable avec le JList des quêtes
    private JScrollPane buildListArea() {
        listModel = new DefaultListModel<>();
        questList = new JList<>(listModel);
        questList.setCellRenderer(new QuestCardRenderer());
        questList.setBackground(BG);
        questList.setSelectionBackground(CARD_SEL);
        questList.setFixedCellHeight(-1);
        questList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) syncButtons();
        });

        JScrollPane scroll = new JScrollPane(questList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        return scroll;
    }

    // barre basse : actions sur la quête sélectionnée + message de statut
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SURFACE);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setBackground(SURFACE);

        btnStart    = btn("▶  Commencer", WARNING, Color.WHITE);
        btnComplete = btn("✓  Terminer",  SUCCESS, Color.WHITE);
        btnDelete   = btn("✕  Supprimer", DANGER,  Color.WHITE);

        btnStart.setEnabled(false);
        btnComplete.setEnabled(false);
        btnDelete.setEnabled(false);

        btnStart.addActionListener(e -> onStart());
        btnComplete.addActionListener(e -> onComplete());
        btnDelete.addActionListener(e -> onDelete());

        btns.add(btnStart);
        btns.add(btnComplete);
        btns.add(btnDelete);
        bar.add(btns, BorderLayout.WEST);

        statusBar = new JLabel(" ");
        statusBar.setForeground(SUBTEXT);
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        bar.add(statusBar, BorderLayout.EAST);

        return bar;
    }

    // --- actions utilisateur ---

    private void openCreateDialog() {
        QuestFormDialog dlg = new QuestFormDialog(parentFrame);
        dlg.setVisible(true);
        if (!dlg.isConfirmed()) return;

        try {
            questController.createQuest(
                dlg.getQuestTitle(),
                dlg.getQuestDescription(),
                dlg.getXpReward(),
                dlg.getQuestType()
            );
            refresh();
            status("Quête « " + dlg.getQuestTitle() + " » ajoutée !", SUCCESS);
        } catch (InvalidQuestException e) {
            err("Validation", e.getMessage());
        } catch (DataCorruptedException e) {
            err("Sauvegarde", e.getMessage());
        }
    }

    private void onStart() {
        Quest q = questList.getSelectedValue();
        if (q == null) return;
        try {
            questController.startQuest(q.getId());
            refresh();
            status("Quête démarrée : « " + q.getTitle() + " »", WARNING);
        } catch (InvalidQuestException | DataCorruptedException e) {
            err("Erreur", e.getMessage());
        }
    }

    private void onComplete() {
        Quest q = questList.getSelectedValue();
        if (q == null) return;
        try {
            boolean levelUp = questController.completeQuest(q.getId());
            playerPanel.refresh();
            refresh();
            status("+" + q.getXpReward() + " XP  ·  « " + q.getTitle() + " » terminée !", SUCCESS);

            if (levelUp) {
                try {
                    com.taskquest.model.Player p = playerController.getPlayer();
                    showLevelUpPopup(p.getLevel(), p.getTitle());
                } catch (PlayerNotFoundException ignored) {}
            }
        } catch (InvalidQuestException e) {
            err("Erreur", e.getMessage());
        } catch (PlayerNotFoundException e) {
            err("Joueur introuvable", e.getMessage());
        } catch (DataCorruptedException e) {
            err("Sauvegarde", e.getMessage());
        }
    }

    private void onDelete() {
        Quest q = questList.getSelectedValue();
        if (q == null) return;

        int rep = JOptionPane.showConfirmDialog(parentFrame,
            "Supprimer « " + q.getTitle() + " » ?\nCette action est définitive.",
            "Confirmer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (rep != JOptionPane.YES_OPTION) return;

        try {
            questController.deleteQuest(q.getId());
            refresh();
            status("Quête supprimée.", SUBTEXT);
        } catch (InvalidQuestException | DataCorruptedException e) {
            err("Erreur", e.getMessage());
        }
    }

    // popup de montée de niveau avec un peu de style
    private void showLevelUpPopup(int newLevel, String newTitle) {
        JDialog popup = new JDialog(parentFrame, "⬆ Montée de niveau !", true);
        popup.setUndecorated(true);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(new Color(22, 18, 40));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(140, 100, 220), 2),
            new EmptyBorder(28, 36, 22, 36)
        ));

        JLabel ico = new JLabel("⬆", SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        content.add(ico, BorderLayout.NORTH);

        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setBackground(new Color(22, 18, 40));

        JLabel lvlLbl = new JLabel("NIVEAU " + newLevel, SwingConstants.CENTER);
        lvlLbl.setForeground(GOLD);
        lvlLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lvlLbl.setAlignmentX(CENTER_ALIGNMENT);
        mid.add(lvlLbl);

        JLabel titleLbl = new JLabel("Titre débloqué : « " + newTitle + " »", SwingConstants.CENTER);
        titleLbl.setForeground(new Color(180, 145, 230));
        titleLbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);
        mid.add(titleLbl);

        content.add(mid, BorderLayout.CENTER);

        JButton ok = btn("  Continuer l'aventure  ", new Color(110, 80, 200), Color.WHITE);
        ok.setAlignmentX(CENTER_ALIGNMENT);
        ok.addActionListener(e -> popup.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(22, 18, 40));
        footer.add(ok);
        content.add(footer, BorderLayout.SOUTH);

        popup.setContentPane(content);
        popup.pack();
        popup.setLocationRelativeTo(parentFrame);
        popup.setVisible(true);
    }

    /**
     * Recharge la liste selon le filtre actif. À appeler après chaque modification.
     */
    public void refresh() {
        String filtre = (String) filterCombo.getSelectedItem();
        List<Quest> quests = switch (filtre) {
            case "À faire"   -> questController.getQuestsByStatus(QuestStatus.TODO);
            case "En cours"  -> questController.getQuestsByStatus(QuestStatus.IN_PROGRESS);
            case "Terminées" -> questController.getQuestsByStatus(QuestStatus.DONE);
            default          -> questController.getAllQuests();
        };

        listModel.clear();
        for (Quest q : quests) listModel.addElement(q);

        // compteur dans la barre titre
        List<Quest> all = questController.getAllQuests();
        long todo = all.stream().filter(q -> q.getStatus() == QuestStatus.TODO).count();
        long done = all.stream().filter(q -> q.getStatus() == QuestStatus.DONE).count();
        questCountLabel.setText("(" + todo + " à faire · " + done + " terminées)");

        syncButtons();
    }

    private void syncButtons() {
        Quest q = questList.getSelectedValue();
        if (q == null) {
            btnStart.setEnabled(false);
            btnComplete.setEnabled(false);
            btnDelete.setEnabled(false);
        } else {
            btnStart.setEnabled(q.getStatus() == QuestStatus.TODO);
            btnComplete.setEnabled(q.getStatus() != QuestStatus.DONE);
            btnDelete.setEnabled(true);
        }
    }

    private void status(String msg, Color c) {
        statusBar.setForeground(c);
        statusBar.setText(msg);
    }

    private void err(String titre, String msg) {
        JOptionPane.showMessageDialog(parentFrame, msg, titre, JOptionPane.ERROR_MESSAGE);
    }

    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setOpaque(true);
        return b;
    }

    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(36, 36, 58));
        c.setForeground(TEXT);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    // retourne des étoiles en fonction de l'XP (plus il y en a, plus c'est difficile)
    private String stars(int xp) {
        int n = Math.min(5, Math.max(1, (xp - 1) / 200 + 1));
        return "★".repeat(n) + "☆".repeat(5 - n);
    }

    // -----------------------------------------------------------------------
    //  Renderer : chaque quête = une carte avec bordure colorée selon statut
    // -----------------------------------------------------------------------
    private class QuestCardRenderer extends JPanel implements ListCellRenderer<Quest> {

        private final JLabel typeBadge;
        private final JLabel starLbl;
        private final JLabel titleLbl;
        private final JLabel descLbl;
        private final JLabel xpLbl;
        private final JLabel statusBadge;
        private final JPanel leftStripe;

        QuestCardRenderer() {
            setLayout(new BorderLayout(0, 0));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(38, 38, 60)),
                new EmptyBorder(0, 0, 0, 0)
            ));

            // bande colorée à gauche selon le statut
            leftStripe = new JPanel();
            leftStripe.setPreferredSize(new Dimension(5, 0));
            add(leftStripe, BorderLayout.WEST);

            JPanel inner = new JPanel(new BorderLayout(10, 0));
            inner.setBorder(new EmptyBorder(10, 12, 10, 12));
            inner.setOpaque(false);
            add(inner, BorderLayout.CENTER);

            // badge type
            typeBadge = new JLabel();
            typeBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            typeBadge.setOpaque(true);
            typeBadge.setBorder(new EmptyBorder(2, 7, 2, 7));
            typeBadge.setPreferredSize(new Dimension(86, 20));
            typeBadge.setHorizontalAlignment(SwingConstants.CENTER);
            inner.add(typeBadge, BorderLayout.WEST);

            // centre : titre + description + étoiles
            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setOpaque(false);
            center.setBorder(new EmptyBorder(0, 8, 0, 8));

            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            titleRow.setOpaque(false);
            titleLbl = new JLabel();
            titleLbl.setForeground(TEXT);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            titleRow.add(titleLbl);

            starLbl = new JLabel();
            starLbl.setForeground(new Color(180, 145, 50));
            starLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            titleRow.add(starLbl);

            center.add(titleRow);

            descLbl = new JLabel();
            descLbl.setForeground(SUBTEXT);
            descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            center.add(descLbl);
            inner.add(center, BorderLayout.CENTER);

            // droite : XP + statut
            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);

            xpLbl = new JLabel();
            xpLbl.setForeground(GOLD);
            xpLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            xpLbl.setAlignmentX(RIGHT_ALIGNMENT);
            right.add(xpLbl);

            right.add(Box.createRigidArea(new Dimension(0, 4)));

            statusBadge = new JLabel();
            statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            statusBadge.setOpaque(true);
            statusBadge.setBorder(new EmptyBorder(2, 7, 2, 7));
            statusBadge.setAlignmentX(RIGHT_ALIGNMENT);
            right.add(statusBadge);
            inner.add(right, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Quest> list, Quest q,
                int idx, boolean selected, boolean focused) {

            setBackground(selected ? CARD_SEL : CARD_BG);

            // bande colorée selon le statut
            switch (q.getStatus()) {
                case TODO        -> leftStripe.setBackground(new Color(65, 65, 100));
                case IN_PROGRESS -> leftStripe.setBackground(WARNING);
                case DONE        -> leftStripe.setBackground(SUCCESS);
            }

            // badge type
            if ("DAILY".equals(q.getType())) {
                typeBadge.setText("⟳ Quotidien");
                typeBadge.setBackground(new Color(72, 42, 115));
                typeBadge.setForeground(new Color(190, 150, 255));
            } else {
                typeBadge.setText("◆ Unique");
                typeBadge.setBackground(new Color(26, 52, 100));
                typeBadge.setForeground(new Color(115, 170, 255));
            }

            titleLbl.setText(q.getTitle());
            starLbl.setText(stars(q.getXpReward()));

            String desc = q.getDescription();
            if (desc != null && !desc.isBlank()) {
                descLbl.setText(desc.length() > 68 ? desc.substring(0, 65) + "…" : desc);
            } else {
                descLbl.setText("—");
            }

            xpLbl.setText("+" + q.getXpReward() + " XP");

            // badge statut
            switch (q.getStatus()) {
                case TODO -> {
                    statusBadge.setText("À FAIRE");
                    statusBadge.setBackground(new Color(48, 48, 72));
                    statusBadge.setForeground(new Color(148, 148, 178));
                }
                case IN_PROGRESS -> {
                    statusBadge.setText("EN COURS");
                    statusBadge.setBackground(new Color(90, 56, 16));
                    statusBadge.setForeground(new Color(255, 172, 70));
                }
                case DONE -> {
                    statusBadge.setText("TERMINÉE ✓");
                    statusBadge.setBackground(new Color(20, 66, 35));
                    statusBadge.setForeground(new Color(88, 210, 118));
                }
            }

            return this;
        }
    }

    // scrollbar sombre
    private static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(65, 65, 96);
            trackColor = new Color(20, 20, 35);
        }
        @Override protected JButton createDecreaseButton(int o) { return emptyBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return emptyBtn(); }
        private JButton emptyBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
    }
}
