package panel.ingredients;

import model.Ingredient;
import model.InventoryManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class IngredientsPanel extends JPanel {

    // ─── Theme ────────────────────────────────────────────────────────────────
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
    private static final Color RED         = new Color(190, 50, 50);

    // ─── State ────────────────────────────────────────────────────────────────
    private final IngredientsController controller;

    // Add form fields
    private JTextField addNameField;
    private JTextField addUnitField;
    private JSpinner   addStockSpinner;
    private JSpinner   addThresholdSpinner;

    // Edit form fields
    private JTextField editNameField;
    private JTextField editUnitField;
    private JSpinner   editThresholdSpinner;
    private JButton    btnSaveEdit;
    private JButton    btnRemove;
    private JLabel     editStatusLbl;
    private Ingredient selectedIngredient = null;

    // Controls form
    private JComboBox<Ingredient> controlCombo;
    private JSpinner               controlThresholdSpinner;

    // Ingredient table
    private JTable            table;
    private DefaultTableModel tableModel;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public IngredientsPanel(InventoryManager im) {
        this.controller = new IngredientsController(im);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    public void refresh() {
        rebuildTable();
        refreshControlCombo();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 18, 4));

        JLabel title = new JLabel("Ingredients");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(GOLD_DARK);

        JLabel subtitle = new JLabel("Add, edit, and manage your inventory ingredients");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_LIGHT);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        JPanel underline = new JPanel();
        underline.setBackground(GRID_LINE);
        underline.setPreferredSize(new Dimension(0, 1));

        header.add(titleBlock, BorderLayout.CENTER);
        header.add(underline,  BorderLayout.SOUTH);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BODY  — left column (add + controls) | right column (table + edit)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        // Left column: fixed width, add card on top, controls below
        JPanel leftCol = new JPanel(new BorderLayout(0, 14));
        leftCol.setOpaque(false);
        leftCol.setPreferredSize(new Dimension(320, 0));
        leftCol.add(buildAddCard(),      BorderLayout.CENTER);
        leftCol.add(buildControlsCard(), BorderLayout.SOUTH);

        // Right column: ingredient table on top, edit card below
        JPanel rightCol = new JPanel(new BorderLayout(0, 14));
        rightCol.setOpaque(false);
        rightCol.add(buildTableCard(), BorderLayout.CENTER);
        rightCol.add(buildEditCard(),  BorderLayout.SOUTH);

        body.add(leftCol,  BorderLayout.WEST);
        body.add(rightCol, BorderLayout.CENTER);
        return body;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LEFT TOP: ADD INGREDIENT
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildAddCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(cardHeader("＋  Add Ingredient"), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Name
        form.add(formLabel("INGREDIENT NAME"));
        form.add(Box.createVerticalStrut(5));
        addNameField = styledField("e.g. Espresso Beans");
        form.add(addNameField);
        form.add(Box.createVerticalStrut(14));

        // Unit
        form.add(formLabel("UNIT"));
        form.add(Box.createVerticalStrut(5));
        addUnitField = styledField("e.g. g, ml, pcs");
        form.add(addUnitField);
        form.add(Box.createVerticalStrut(14));

        // Initial stock + threshold side by side
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stockBox = new JPanel(new BorderLayout(0, 5));
        stockBox.setOpaque(false);
        stockBox.add(formLabel("INITIAL STOCK"), BorderLayout.NORTH);
        addStockSpinner = styledSpinner(0.0, 0.0, 999999.0, 1.0);
        stockBox.add(addStockSpinner, BorderLayout.CENTER);

        JPanel threshBox = new JPanel(new BorderLayout(0, 5));
        threshBox.setOpaque(false);
        threshBox.add(formLabel("LOW STOCK THRESHOLD"), BorderLayout.NORTH);
        addThresholdSpinner = styledSpinner(50.0, 0.0, 999999.0, 1.0);
        threshBox.add(addThresholdSpinner, BorderLayout.CENTER);

        row2.add(stockBox);
        row2.add(threshBox);
        form.add(row2);
        form.add(Box.createVerticalStrut(20));

        // Submit button
        JButton btnAdd = actionButton("＋  Add Ingredient", HEADER_BG);
        btnAdd.addActionListener(e -> doAddIngredient());
        form.add(btnAdd);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void doAddIngredient() {
        try { addStockSpinner.commitEdit(); addThresholdSpinner.commitEdit(); }
        catch (Exception ex) { /* ignore */ }

        String err = controller.addIngredient(
            addNameField.getText(),
            addUnitField.getText(),
            (Double) addStockSpinner.getValue(),
            (Double) addThresholdSpinner.getValue()
        );

        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Clear form
        addNameField.setText("");
        addUnitField.setText("");
        addStockSpinner.setValue(0.0);
        addThresholdSpinner.setValue(50.0);

        rebuildTable();
        refreshControlCombo();
        JOptionPane.showMessageDialog(this,
            "Ingredient added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LEFT BOTTOM: CONTROLS (threshold quick-set)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildControlsCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(cardHeader("⚙  Controls"), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 18, 16, 18));

        form.add(formLabel("SELECT INGREDIENT"));
        form.add(Box.createVerticalStrut(5));
        controlCombo = new JComboBox<>();
        refreshControlCombo();
        styleCombo(controlCombo);
        form.add(controlCombo);
        form.add(Box.createVerticalStrut(12));

        form.add(formLabel("SET LOW STOCK THRESHOLD"));
        form.add(Box.createVerticalStrut(5));
        controlThresholdSpinner = styledSpinner(50.0, 0.0, 999999.0, 1.0);

        form.add(controlThresholdSpinner);
        // When combo selection changes, pre-fill current threshold
        controlCombo.addActionListener(e -> {
            Ingredient ing = (Ingredient) controlCombo.getSelectedItem();
            if (ing != null)
                controlThresholdSpinner.setValue(ing.getLowStockThreshold());
        });

        
        form.add(Box.createVerticalStrut(14));

        JButton btnApply = actionButton("Apply Threshold", new Color(130, 100, 40));
        btnApply.addActionListener(e -> doUpdateThreshold());
        form.add(btnApply);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void refreshControlCombo() {
        controlCombo.removeAllItems();
        for (Ingredient ing : controller.getIngredients()) controlCombo.addItem(ing);
        Ingredient sel = (Ingredient) controlCombo.getSelectedItem();
        if (sel != null && controlThresholdSpinner != null) controlThresholdSpinner.setValue(sel.getLowStockThreshold());
    }

    private void doUpdateThreshold() {
        Ingredient ing = (Ingredient) controlCombo.getSelectedItem();
        if (ing == null) return;
        try { controlThresholdSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }

        String err = controller.updateThreshold(ing, (Double) controlThresholdSpinner.getValue());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        rebuildTable();
        refreshControlCombo();
        JOptionPane.showMessageDialog(this,
            "Threshold updated for " + ing.getDisplayName() + ".", "Updated", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RIGHT TOP: INGREDIENT TABLE
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildTableCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(cardHeader("≡  Ingredient List"), BorderLayout.NORTH);

        String[] cols = {"NAME", "UNIT", "CURRENT STOCK", "THRESHOLD", "STATUS"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(210, 185, 130));
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                    // Colour status column
                    if (col == 4) {
                        String val = (String) getValueAt(row, col);
                        c.setForeground("CRITICAL".equals(val) ? new Color(200, 120, 30)
                                      : "OUT OF STOCK".equals(val) ? RED : GREEN_DARK);
                    } else {
                        c.setForeground(TEXT_DARK);
                    }
                }
                return c;
            }
        };

        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(GRID_LINE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        // Style header
        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(230, 215, 180));
        th.setForeground(GOLD_DARK);
        th.setFont(new Font("SansSerif", Font.BOLD, 11));
        th.setReorderingAllowed(false);
        th.setBorder(new MatteBorder(0, 0, 2, 0, GRID_LINE));

        // Row click → populate edit form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateEditForm();
        });

        rebuildTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void rebuildTable() {
        tableModel.setRowCount(0);
        for (Ingredient ing : controller.getIngredients()) {
            double stock = controller.getStock(ing);
            String status = stock <= 0                       ? "OUT OF STOCK"
                          : stock < ing.getLowStockThreshold() ? "CRITICAL"
                          : "OK";
            tableModel.addRow(new Object[]{
                ing.getDisplayName(),
                ing.getUnit(),
                String.format("%.2f", stock),
                String.format("%.2f", ing.getLowStockThreshold()),
                status
            });
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RIGHT BOTTOM: EDIT INGREDIENT
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildEditCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(cardHeader("✎  Edit Ingredient"), BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(14, 18, 16, 18));

        // Hint label shown when nothing is selected
        editStatusLbl = new JLabel("← Select a row from the table above to edit");
        editStatusLbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        editStatusLbl.setForeground(TEXT_LIGHT);
        editStatusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(editStatusLbl);
        form.add(Box.createVerticalStrut(10));

        // Fields row: name | unit | threshold
        JPanel fieldsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        fieldsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel nameBox = new JPanel(new BorderLayout(0, 5));
        nameBox.setOpaque(false);
        nameBox.add(formLabel("NAME"), BorderLayout.NORTH);
        editNameField = styledField("");
        editNameField.setEnabled(false);
        nameBox.add(editNameField, BorderLayout.CENTER);

        JPanel unitBox = new JPanel(new BorderLayout(0, 5));
        unitBox.setOpaque(false);
        unitBox.add(formLabel("UNIT"), BorderLayout.NORTH);
        editUnitField = styledField("");
        editUnitField.setEnabled(false);
        unitBox.add(editUnitField, BorderLayout.CENTER);

        JPanel threshBox = new JPanel(new BorderLayout(0, 5));
        threshBox.setOpaque(false);
        threshBox.add(formLabel("THRESHOLD"), BorderLayout.NORTH);
        editThresholdSpinner = styledSpinner(0.0, 0.0, 999999.0, 1.0);
        editThresholdSpinner.setEnabled(false);
        threshBox.add(editThresholdSpinner, BorderLayout.CENTER);

        fieldsRow.add(nameBox);
        fieldsRow.add(unitBox);
        fieldsRow.add(threshBox);
        form.add(fieldsRow);
        form.add(Box.createVerticalStrut(14));

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSaveEdit = actionButton("Save Changes", HEADER_BG);
        btnSaveEdit.setEnabled(false);
        btnSaveEdit.addActionListener(e -> doSaveEdit());

        btnRemove = actionButton("Remove", RED);
        btnRemove.setEnabled(false);
        btnRemove.addActionListener(e -> doRemoveIngredient());

        btnRow.add(btnSaveEdit);
        btnRow.add(btnRemove);
        form.add(btnRow);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void populateEditForm() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= controller.getIngredients().size()) {
            selectedIngredient = null;
            setEditEnabled(false);
            editStatusLbl.setText("← Select a row from the table above to edit");
            return;
        }

        selectedIngredient = controller.getIngredients().get(row);
        editNameField.setText(selectedIngredient.getDisplayName());
        editUnitField.setText(selectedIngredient.getUnit());
        editThresholdSpinner.setValue(selectedIngredient.getLowStockThreshold());
        editStatusLbl.setText("Editing:  " + selectedIngredient.getDisplayName());
        editStatusLbl.setForeground(GOLD_DARK);
        setEditEnabled(true);
    }

    private void setEditEnabled(boolean enabled) {
        editNameField.setEnabled(enabled);
        editUnitField.setEnabled(enabled);
        editThresholdSpinner.setEnabled(enabled);
        btnSaveEdit.setEnabled(enabled);
        btnRemove.setEnabled(enabled);
    }

    private void doSaveEdit() {
        if (selectedIngredient == null) return;
        try { editThresholdSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }

        String err = controller.editIngredient(
            selectedIngredient,
            editNameField.getText(),
            editUnitField.getText(),
            (Double) editThresholdSpinner.getValue()
        );

        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedIngredient = null;
        setEditEnabled(false);
        editStatusLbl.setText("← Select a row from the table above to edit");
        editStatusLbl.setForeground(TEXT_LIGHT);
        rebuildTable();
        refreshControlCombo();
        JOptionPane.showMessageDialog(this, "Ingredient updated.", "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doRemoveIngredient() {
        if (selectedIngredient == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove \"" + selectedIngredient.getDisplayName() + "\"? This cannot be undone.",
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        controller.removeIngredient(selectedIngredient);
        selectedIngredient = null;
        setEditEnabled(false);
        editStatusLbl.setText("← Select a row from the table above to edit");
        editStatusLbl.setForeground(TEXT_LIGHT);
        rebuildTable();
        refreshControlCombo();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═════════════════════════════════════════════════════════════════════════
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

    private JLabel cardHeader(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        lbl.setBackground(HEADER_BG);
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(10, 12, 10, 12));
        return lbl;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setForeground(TEXT_DARK);
        f.setBackground(new Color(252, 249, 238));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_LINE),
            new EmptyBorder(6, 8, 6, 8)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Placeholder hint
        f.setText(placeholder);
        f.setForeground(TEXT_LIGHT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_LIGHT); }
            }
        });
        return f;
    }

    private JSpinner styledSpinner(double val, double min, double max, double step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setFont(new Font("SansSerif", Font.PLAIN, 13));
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setBorder(BorderFactory.createLineBorder(GRID_LINE));
        return s;
    }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (getModel().isPressed()
                    ? bg.darker() : bg) : new Color(200, 190, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return btn;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBackground(new Color(252, 249, 238));
        combo.setForeground(TEXT_DARK);
    }
}