package com.taskquest.view;

import com.taskquest.model.Quest;
import com.taskquest.model.Reward;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogue modal de création d'une nouvelle quête.
 * <p>
 * Valide les entrées avant de les rendre disponibles au contrôleur.
 * </p>
 */
public class QuestFormDialog extends JDialog {

    // Palette de couleurs dark RPG
    private static final Color BG = new Color(22, 22, 38);
    private static final Color SURFACE = new Color(32, 32, 52);
    private static final Color ACCENT = new Color(110, 110, 230);
    private static final Color TEXT = new Color(220, 220, 235);
    private static final Color SUBTEXT = new Color(140, 140, 165);
    private static final Color SUCCESS = new Color(70, 200, 110);
    private static final Color DANGER = new Color(220, 70, 70);

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner xpSpinner;
    private JComboBox<String> typeCombo;
    private boolean confirmed = false;

    /**
     * Crée et affiche le dialogue de création de quête.
     *
     * @param parent La fenêtre parente
     */
    public QuestFormDialog(Frame parent) {
        super(parent, "Nouvelle Quête", true);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /** Construit l'interface du dialogue. */
    private void buildUI() {
        setBackground(BG);
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Titre du dialogue
        JLabel header = new JLabel("⚔  Nouvelle Quête");
        header.setForeground(new Color(200, 160, 80));
        header.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(header, BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 0, 4, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Titre
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(label("Titre *"), gc);
        titleField = styledTextField(30);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(titleField, gc);

        // Description
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        form.add(label("Description"), gc);
        descriptionArea = new JTextArea(4, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(SURFACE);
        descriptionArea.setForeground(TEXT);
        descriptionArea.setCaretColor(TEXT);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 85), 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        descScroll.setBackground(SURFACE);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.BOTH;
        form.add(descScroll, gc);

        // Récompense XP
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        form.add(label("XP (1–" + Reward.MAX_XP_REWARD + ") *"), gc);
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(50, 1, Reward.MAX_XP_REWARD, 10);
        xpSpinner = new JSpinner(spinnerModel);
        styleSpinner(xpSpinner);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(xpSpinner, gc);

        // Type
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        form.add(label("Type *"), gc);
        typeCombo = new JComboBox<>(new String[]{"ONETIME", "DAILY"});
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText("ONETIME".equals(value) ? "Unique (accomplie une seule fois)" : "Quotidienne (se réinitialise chaque jour)");
                setBackground(isSelected ? ACCENT : SURFACE);
                setForeground(TEXT);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        typeCombo.setBackground(SURFACE);
        typeCombo.setForeground(TEXT);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeCombo.setBorder(BorderFactory.createLineBorder(new Color(55, 55, 85), 1));
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(typeCombo, gc);

        root.add(form, BorderLayout.CENTER);

        // Boutons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(BG);
        buttons.setBorder(new EmptyBorder(16, 0, 0, 0));

        JButton cancelBtn = createButton("Annuler", DANGER, Color.WHITE);
        cancelBtn.addActionListener(e -> dispose());

        JButton okBtn = createButton("Créer la quête", SUCCESS, Color.WHITE);
        okBtn.addActionListener(e -> onConfirm());

        buttons.add(cancelBtn);
        buttons.add(okBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(okBtn);
    }

    /** Valide les champs et ferme le dialogue si tout est correct. */
    private void onConfirm() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le titre ne peut pas être vide.",
                "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }
        if (title.length() > Quest.MAX_TITLE_LENGTH) {
            JOptionPane.showMessageDialog(this,
                "Le titre ne peut pas dépasser " + Quest.MAX_TITLE_LENGTH + " caractères.",
                "Titre trop long", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String desc = descriptionArea.getText().trim();
        if (desc.length() > Quest.MAX_DESCRIPTION_LENGTH) {
            JOptionPane.showMessageDialog(this,
                "La description ne peut pas dépasser " + Quest.MAX_DESCRIPTION_LENGTH + " caractères.",
                "Description trop longue", JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    /** @return true si l'utilisateur a cliqué sur "Créer la quête" */
    public boolean isConfirmed() { return confirmed; }

    /** @return Le titre saisi */
    public String getQuestTitle() { return titleField.getText().trim(); }

    /** @return La description saisie */
    public String getQuestDescription() { return descriptionArea.getText().trim(); }

    /** @return La récompense XP saisie */
    public int getXpReward() { return (Integer) xpSpinner.getValue(); }

    /** @return "DAILY" ou "ONETIME" */
    public String getQuestType() { return (String) typeCombo.getSelectedItem(); }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(SUBTEXT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private JTextField styledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(SURFACE);
        tf.setForeground(TEXT);
        tf.setCaretColor(TEXT);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 55, 85), 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(SURFACE);
        spinner.setForeground(TEXT);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(SURFACE);
            de.getTextField().setForeground(TEXT);
            de.getTextField().setCaretColor(TEXT);
            de.getTextField().setBorder(new EmptyBorder(4, 6, 4, 6));
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        return btn;
    }
}
