package panel.customization;

import model.CoffeeType;
import model.Ingredient;
import model.InventoryManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CustomizationPanel extends JPanel {

    // ─── Theme colours ────────────────────────────────────────────────────────
    private static final Color BG          = new Color(250, 245, 230);
    private static final Color CARD_BG     = new Color(255, 252, 240);
    private static final Color CARD_BORDER = new Color(200, 175, 120);
    private static final Color GOLD        = new Color(180, 140, 50);
    private static final Color GOLD_DARK   = new Color(140, 100, 30);
    private static final Color HEADER_BG   = new Color(160, 120, 50);
    private static final Color ROW_ALT     = new Color(245, 238, 218);
    private static final Color GRID_LINE   = new Color(210, 190, 145);
    private static final Color TEXT_DARK   = new Color(60, 45, 20);
    private static final Color TEXT_LIGHT  = new Color(170, 150, 100);
    private static final Color GREEN       = new Color(60, 160, 60);
    private static final Color GREEN_DARK  = new Color(40, 120, 40);
    private static final Color RED_ERR     = new Color(180, 50, 50);

    private final CustomizationController controller;

    // Table area rebuilt on every refresh
    private JPanel tableContent;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public CustomizationPanel(InventoryManager im) {
        this.controller = new CustomizationController(im);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    public void refresh() {
        rebuildTable();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 18, 4));

        JLabel title = new JLabel("Customization");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(GOLD_DARK);

        JLabel subtitle = new JLabel("Manage Coffee Types and Their Recipes");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_LIGHT);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        JButton btnAdd = new JButton("+ Add New Flavor") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GOLD_DARK : GOLD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setOpaque(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAdd.setFocusable(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnAdd.addActionListener(e -> showAddDialog());

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrap.setOpaque(false);
        rightWrap.add(btnAdd);

        JPanel underline = new JPanel();
        underline.setBackground(GRID_LINE);
        underline.setPreferredSize(new Dimension(0, 1));

        header.add(titleBlock, BorderLayout.CENTER);
        header.add(rightWrap,  BorderLayout.EAST);
        header.add(underline,  BorderLayout.SOUTH);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BODY — full-width scrollable table card
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        // Header bar
        JLabel headerLbl = new JLabel("  DEFAULT RECIPES");
        headerLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerLbl.setForeground(Color.WHITE);
        headerLbl.setBackground(HEADER_BG);
        headerLbl.setOpaque(true);
        headerLbl.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Column headers
        JPanel colHdr = new JPanel(new GridLayout(1, 4, 0, 0));
        colHdr.setBackground(new Color(230, 215, 180));
        colHdr.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, GRID_LINE),
            new EmptyBorder(8, 14, 8, 14)));
        for (String h : new String[]{"NAME", "CATEGORY", "RECIPE", "ACTIONS"}) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("SansSerif", Font.BOLD, 11));
            hl.setForeground(GOLD_DARK);
            colHdr.add(hl);
        }

        JPanel topStack = new JPanel(new BorderLayout());
        topStack.setOpaque(false);
        topStack.add(headerLbl, BorderLayout.NORTH);
        topStack.add(colHdr,    BorderLayout.SOUTH);

        // Scrollable table rows
        tableContent = new JPanel();
        tableContent.setLayout(new BoxLayout(tableContent, BoxLayout.Y_AXIS));
        tableContent.setOpaque(false);
        rebuildTable();

        JScrollPane scroll = new JScrollPane(tableContent,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel scrollWrap = new JPanel(new BorderLayout());
        scrollWrap.setOpaque(false);
        scrollWrap.setBorder(new EmptyBorder(0, 2, 10, 2));
        scrollWrap.add(scroll, BorderLayout.CENTER);

        card.add(topStack,   BorderLayout.NORTH);
        card.add(scrollWrap, BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TABLE ROWS
    // ═════════════════════════════════════════════════════════════════════════
    private void rebuildTable() {
        if (tableContent == null) return;
        tableContent.removeAll();

        List<CoffeeType> types = controller.getCoffeeTypes();

        if (types.isEmpty()) {
            JLabel empty = new JLabel("No coffee types defined yet. Click \"+ Add New Flavor\" to get started.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 13));
            empty.setForeground(TEXT_LIGHT);
            empty.setBorder(new EmptyBorder(20, 16, 0, 0));
            tableContent.add(empty);
        } else {
            for (int i = 0; i < types.size(); i++) {
                CoffeeType ct = types.get(i);
                boolean alt   = i % 2 != 0;
                tableContent.add(buildRow(ct, alt));
                if (i < types.size() - 1) {
                    JPanel line = new JPanel();
                    line.setBackground(GRID_LINE);
                    line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    line.setPreferredSize(new Dimension(0, 1));
                    line.setAlignmentX(Component.LEFT_ALIGNMENT);
                    tableContent.add(line);
                }
            }
        }

        tableContent.revalidate();
        tableContent.repaint();
    }

    private JPanel buildRow(CoffeeType ct, boolean alt) {
        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setBackground(alt ? ROW_ALT : CARD_BG);
        row.setBorder(new EmptyBorder(12, 14, 12, 14));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // NAME
        JLabel lName = new JLabel(ct.getDisplayName());
        lName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lName.setForeground(TEXT_DARK);

        // CATEGORY
        JLabel lCat = new JLabel(ct.getCategory());
        lCat.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lCat.setForeground(TEXT_DARK);

        // RECIPE — list each ingredient + amount
        JPanel recipeCol = new JPanel();
        recipeCol.setLayout(new BoxLayout(recipeCol, BoxLayout.Y_AXIS));
        recipeCol.setOpaque(false);
        for (Map.Entry<Ingredient, Double> e : ct.getRecipe().entrySet()) {
            JLabel ing = new JLabel(String.format("• %s  %.2f %s",
                e.getKey().getDisplayName(), e.getValue(), e.getKey().getUnit()));
            ing.setFont(new Font("Monospaced", Font.PLAIN, 11));
            ing.setForeground(TEXT_LIGHT);
            recipeCol.add(ing);
        }

        // ACTIONS
        JPanel actionsCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actionsCol.setOpaque(false);

        JButton btnEdit = actionBtn("Edit",   GOLD_DARK);
        JButton btnDel  = actionBtn("Delete", RED_ERR);

        btnEdit.addActionListener(e -> showEditDialog(ct));
        btnDel .addActionListener(e -> doDelete(ct));

        actionsCol.add(btnEdit);
        actionsCol.add(btnDel);

        row.add(lName);
        row.add(lCat);
        row.add(recipeCol);
        row.add(actionsCol);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ADD DIALOG
    // ═════════════════════════════════════════════════════════════════════════
    private void showAddDialog() {
        JDialog dlg  = makeDialog("Add New Flavor");
        JPanel  form = dialogForm();

        JTextField        tfName = styledField();
        JComboBox<String> tfCat  = categoryCombo();

        // Recipe builder
        Map<Ingredient, Double> recipe = new LinkedHashMap<>();
        JPanel recipeList = new JPanel();
        recipeList.setLayout(new BoxLayout(recipeList, BoxLayout.Y_AXIS));
        recipeList.setOpaque(false);

        JComboBox<Ingredient> ingCombo = new JComboBox<>();
        for (Ingredient ing : controller.getIngredients()) ingCombo.addItem(ing);
        ingCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        ingCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSpinner amtSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 9999.0, 0.5));
        amtSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amtSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        amtSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAddIng = new JButton("+ Add");
        btnAddIng.setBackground(HEADER_BG);
        btnAddIng.setForeground(Color.WHITE);
        btnAddIng.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAddIng.setFocusable(false);
        btnAddIng.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnAddIng.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddIng.addActionListener(e -> {
            Ingredient sel = (Ingredient) ingCombo.getSelectedItem();
            if (sel == null) return;
            try { amtSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }
            double amt = (Double) amtSpinner.getValue();
            recipe.put(sel, amt);
            refreshRecipeList(recipeList, recipe, null, null);
        });

        form.add(dialogLabel("FLAVOR NAME"));
        form.add(Box.createVerticalStrut(5));
        form.add(tfName);
        form.add(Box.createVerticalStrut(14));

        form.add(dialogLabel("CATEGORY"));
        form.add(Box.createVerticalStrut(5));
        form.add(tfCat);
        form.add(Box.createVerticalStrut(18));

        form.add(dialogLabel("RECIPE INGREDIENTS"));
        form.add(Box.createVerticalStrut(6));

        JPanel addIngRow = new JPanel(new GridLayout(1, 3, 8, 0));
        addIngRow.setOpaque(false);
        addIngRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addIngRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        addIngRow.add(ingCombo);
        addIngRow.add(amtSpinner);
        addIngRow.add(btnAddIng);
        form.add(addIngRow);
        form.add(Box.createVerticalStrut(8));
        form.add(recipeList);

        form.add(Box.createVerticalStrut(18));
        JButton btnSave = confirmBtn("SAVE FLAVOR");
        btnSave.addActionListener(e -> {
            String err = controller.addCoffeeType(
                tfName.getText(),
                (String) tfCat.getSelectedItem(),
                recipe);
            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
            } else {
                dlg.dispose();
                rebuildTable();
            }
        });
        form.add(btnSave);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  EDIT DIALOG
    // ═════════════════════════════════════════════════════════════════════════
    private void showEditDialog(CoffeeType ct) {
        JDialog dlg  = makeDialog("Edit — " + ct.getDisplayName());
        JPanel  form = dialogForm();

        JTextField        tfName = styledField();
        tfName.setText(ct.getDisplayName());
        JComboBox<String> tfCat  = categoryCombo();
        tfCat.setSelectedItem(ct.getCategory());

        // Editable recipe copy
        Map<Ingredient, Double> recipe = new LinkedHashMap<>(ct.getRecipe());
        JPanel recipeList = new JPanel();
        recipeList.setLayout(new BoxLayout(recipeList, BoxLayout.Y_AXIS));
        recipeList.setOpaque(false);

        JComboBox<Ingredient> ingCombo = new JComboBox<>();
        for (Ingredient ing : controller.getIngredients()) ingCombo.addItem(ing);
        ingCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ingCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        ingCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSpinner amtSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 9999.0, 0.5));
        amtSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amtSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        amtSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAddIng = new JButton("+ Add");
        btnAddIng.setBackground(HEADER_BG);
        btnAddIng.setForeground(Color.WHITE);
        btnAddIng.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAddIng.setFocusable(false);
        btnAddIng.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnAddIng.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddIng.addActionListener(e -> {
            Ingredient sel = (Ingredient) ingCombo.getSelectedItem();
            if (sel == null) return;
            try { amtSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }
            double amt = (Double) amtSpinner.getValue();
            recipe.put(sel, amt);
            refreshRecipeList(recipeList, recipe, ct, dlg);
        });

        form.add(dialogLabel("FLAVOR NAME"));
        form.add(Box.createVerticalStrut(5));
        form.add(tfName);
        form.add(Box.createVerticalStrut(14));

        form.add(dialogLabel("CATEGORY"));
        form.add(Box.createVerticalStrut(5));
        form.add(tfCat);
        form.add(Box.createVerticalStrut(18));

        form.add(dialogLabel("RECIPE INGREDIENTS"));
        form.add(Box.createVerticalStrut(6));

        JPanel addIngRow = new JPanel(new GridLayout(1, 3, 8, 0));
        addIngRow.setOpaque(false);
        addIngRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addIngRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        addIngRow.add(ingCombo);
        addIngRow.add(amtSpinner);
        addIngRow.add(btnAddIng);
        form.add(addIngRow);
        form.add(Box.createVerticalStrut(8));

        refreshRecipeList(recipeList, recipe, ct, dlg);
        form.add(recipeList);

        form.add(Box.createVerticalStrut(18));
        JButton btnSave = confirmBtn("SAVE CHANGES");
        btnSave.addActionListener(e -> {
            String err = controller.updateCoffeeType(
                ct,
                tfName.getText(),
                (String) tfCat.getSelectedItem());
            if (err != null) {
                JOptionPane.showMessageDialog(dlg, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Sync recipe with live references
            CoffeeType live = null;
            for (CoffeeType t : controller.getCoffeeTypes()) {
                if (t.getId() == ct.getId()) { live = t; break; }
            }
            if (live != null) {
                for (Map.Entry<Ingredient, Double> entry : recipe.entrySet()) {
                    Ingredient liveIng = controller.findIngredientById(entry.getKey().getId());
                    if (liveIng == null) continue;
                    if (live.getRecipe().containsKey(liveIng)) {
                        controller.updateIngredientAmount(live, liveIng, entry.getValue());
                    } else {
                        controller.addIngredientToRecipe(live, liveIng, entry.getValue());
                    }
                    for (CoffeeType t2 : controller.getCoffeeTypes()) {
                        if (t2.getId() == ct.getId()) { live = t2; break; }
                    }
                }
                // Remove ingredients no longer in recipe
                for (CoffeeType t2 : controller.getCoffeeTypes()) {
                    if (t2.getId() == ct.getId()) { live = t2; break; }
                }
                for (Ingredient existing : new ArrayList<>(live.getRecipe().keySet())) {
                    boolean stillPresent = false;
                    for (Ingredient edited : recipe.keySet()) {
                        if (edited.getId() == existing.getId()) { stillPresent = true; break; }
                    }
                    if (!stillPresent) {
                        controller.removeIngredientFromRecipe(live, existing);
                        for (CoffeeType t2 : controller.getCoffeeTypes()) {
                            if (t2.getId() == ct.getId()) { live = t2; break; }
                        }
                    }
                }
            }
            dlg.dispose();
            rebuildTable();
        });
        form.add(btnSave);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ─── Shared: rebuild the recipe list inside a dialog form ─────────────────
    private void refreshRecipeList(JPanel recipeList,
                                   Map<Ingredient, Double> recipe,
                                   CoffeeType ctForRemove,
                                   JDialog dlg) {
        recipeList.removeAll();
        for (Map.Entry<Ingredient, Double> e : new LinkedHashMap<>(recipe).entrySet()) {
            Ingredient ing = e.getKey();
            double     amt = e.getValue();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lbl = new JLabel(String.format("• %s  —  %.2f %s",
                ing.getDisplayName(), amt, ing.getUnit()));
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
            lbl.setForeground(TEXT_DARK);

            JButton btnRem = new JButton("✕");
            btnRem.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnRem.setForeground(RED_ERR);
            btnRem.setBackground(CARD_BG);
            btnRem.setBorder(new EmptyBorder(2, 6, 2, 6));
            btnRem.setFocusable(false);
            btnRem.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRem.addActionListener(ev -> {
                recipe.remove(ing);
                refreshRecipeList(recipeList, recipe, ctForRemove, dlg);
            });

            row.add(lbl,    BorderLayout.CENTER);
            row.add(btnRem, BorderLayout.EAST);
            recipeList.add(row);
        }
        recipeList.revalidate();
        recipeList.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DELETE
    // ═════════════════════════════════════════════════════════════════════════
    private void doDelete(CoffeeType ct) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + ct.getDisplayName() + "\"? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteCoffeeType(ct);
            rebuildTable();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private JComboBox<String> categoryCombo() {
        List<String> cats = controller.getCategories();
        JComboBox<String> combo = new JComboBox<>(cats.toArray(new String[0]));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBackground(CARD_BG);
        return combo;
    }

    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JDialog makeDialog(String title) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame
            ? new JDialog((Frame) owner, title, true)
            : new JDialog((Dialog) owner, title, true);
        dlg.setSize(520, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(BG);
        return dlg;
    }

    private JPanel dialogForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(BG);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));
        return form;
    }

    private JLabel dialogLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            new EmptyBorder(6, 10, 6, 10)));
        f.setBackground(CARD_BG);
        f.setForeground(TEXT_DARK);
        return f;
    }

    private JButton confirmBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GREEN_DARK : GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private JButton actionBtn(String text, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 11));
        b.setForeground(fg);
        b.setBackground(CARD_BG);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 1),
            new EmptyBorder(4, 12, 4, 12)));
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}