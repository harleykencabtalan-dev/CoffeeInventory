package panel.inventory;

import model.Ingredient;
import model.InventoryManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    // ─── Colours ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(245, 245, 240);
    private static final Color ACCENT      = new Color(120, 90, 70);
    private static final Color GOLD        = new Color(180, 140, 40);
    private static final Color GOLD_HEADER = new Color(160, 120, 50);
    private static final Color GOLD_BG     = new Color(255, 248, 225);
    private static final Color TEXT_DARK   = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT  = new Color(150, 150, 150);
    private static final Color GREEN       = new Color(50, 140, 50);
    private static final Color RED         = new Color(180, 50, 50);
    private static final Color ROW_ALT     = new Color(250, 247, 240);
    private static final Color DIVIDER     = new Color(220, 210, 190);

    // ─── State ────────────────────────────────────────────────────────────────
    private final InventoryController controller;

    private boolean        showLowOnly     = false;
    private Ingredient     selectedIng     = null;
    private String         adjustMode      = "ADD"; // ADD | DEDUCT | SET

    // Widgets
    private JTextField     searchField;
    private JPanel         tableBody;
    private JLabel         lblSelectedName;
    private JTextField     amountField;
    private JTextField     reasonField;
    private JButton        btnAdd;
    private JButton        btnDeduct;
    private JButton        btnSet;
    private JLabel         lblFeedback;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public InventoryPanel(InventoryManager im) {
        this.controller = new InventoryController(im);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }
    
    public void refresh() {
    controller.reload();
    rebuildTable(controller.getIngredients());
}

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 16));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("INVENTORY");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.NORTH);

        // Toolbar: search + refresh + toggle
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        // Search bar
        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setBackground(Color.WHITE);
        searchWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            new EmptyBorder(6, 10, 6, 10)));
        searchWrap.setMaximumSize(new Dimension(400, 36));

        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBackground(Color.WHITE);

        JButton btnClear = new JButton("✕");
        btnClear.setBorder(null);
        btnClear.setContentAreaFilled(false);
        btnClear.setFocusable(false);
        btnClear.setForeground(TEXT_LIGHT);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> { searchField.setText(""); rebuildTable(controller.getIngredients()); });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { onSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
        });

        searchWrap.add(searchIcon,  BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        searchWrap.add(btnClear,    BorderLayout.EAST);

        // Right buttons
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton btnRefresh = toolbarBtn("↻  Refresh");
        btnRefresh.addActionListener(e -> {
            controller.reload();
            rebuildTable(controller.getIngredients());
        });

        JButton btnToggle = toolbarBtn("⇌  Toggle");
        btnToggle.addActionListener(e -> {
            showLowOnly = !showLowOnly;
            rebuildTable(showLowOnly
                ? controller.getLowStock()
                : controller.getIngredients());
        });

        rightBtns.add(btnRefresh);
        rightBtns.add(btnToggle);

        toolbar.add(searchWrap, BorderLayout.CENTER);
        toolbar.add(rightBtns,  BorderLayout.EAST);
        header.add(toolbar, BorderLayout.SOUTH);

        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BODY: table (left) + adjust panel (right)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        body.add(buildTableCard(), BorderLayout.CENTER);
        body.add(buildAdjustCard(), BorderLayout.EAST);

        return body;
    }

    // ─── LEFT: Inventory Table ────────────────────────────────────────────────
    private JPanel buildTableCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        // Column headers
        String[] cols = {"Name", "Stock", "Unit", "Threshold", "Status"};
        int[]    widths = {3, 2, 1, 2, 1};

        JPanel headerRow = new JPanel(new GridLayout(1, cols.length, 0, 0));
        headerRow.setBackground(ACCENT);
        headerRow.setBorder(new EmptyBorder(10, 16, 10, 16));
        for (String col : cols) {
            JLabel lbl = new JLabel(col);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            headerRow.add(lbl);
        }

        tableBody = new JPanel();
        tableBody.setLayout(new BoxLayout(tableBody, BoxLayout.Y_AXIS));
        tableBody.setOpaque(false);

        rebuildTable(controller.getIngredients());

        JScrollPane scroll = new JScrollPane(tableBody);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);

        return card;
    }

    private void rebuildTable(List<Ingredient> list) {
        tableBody.removeAll();

        if (list.isEmpty()) {
            JLabel empty = new JLabel("No ingredients found.");
            empty.setFont(new Font("Monospaced", Font.PLAIN, 13));
            empty.setForeground(TEXT_LIGHT);
            empty.setBorder(new EmptyBorder(20, 16, 20, 16));
            tableBody.add(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                Ingredient ing = list.get(i);
                tableBody.add(buildTableRow(ing, i % 2 == 0));
            }
        }

        tableBody.revalidate();
        tableBody.repaint();
    }

    private JPanel buildTableRow(Ingredient ing, boolean alt) {
        JPanel row = new JPanel(new GridLayout(1, 5, 0, 0));
        row.setBackground(alt ? Color.WHITE : ROW_ALT);
        row.setBorder(new EmptyBorder(10, 16, 10, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        double stock     = controller.getStock(ing);
        String status    = controller.getStatus(ing);

        addCell(row, ing.getDisplayName(), Font.PLAIN,  TEXT_DARK);
        addCell(row, String.format("%.2f", stock),     Font.BOLD,  TEXT_DARK);
        addCell(row, ing.getUnit(),                    Font.PLAIN,  TEXT_LIGHT);
        addCell(row, String.format("%.0f", ing.getLowStockThreshold()), Font.PLAIN, TEXT_LIGHT);

        // Status badge
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        JLabel badge = new JLabel(status);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        badge.setBackground(switch (status) {
            case "OUT" -> RED;
            case "LOW" -> new Color(200, 100, 30);
            default    -> GREEN;
        });
        badgeWrap.add(badge);
        row.add(badgeWrap);

        // Click to select
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectedIng = ing;
                lblSelectedName.setText(ing.getDisplayName() + "  (" + String.format("%.2f", stock) + " " + ing.getUnit() + ")");
                lblFeedback.setText(" ");
                amountField.setText("");
                reasonField.setText("");
                // Highlight row
                rebuildTable(showLowOnly ? controller.getLowStock() : controller.getIngredients());
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(GOLD_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(alt ? Color.WHITE : ROW_ALT);
            }
        });

        return row;
    }

    // ─── RIGHT: Adjust Stock Card ─────────────────────────────────────────────
    private JPanel buildAdjustCard() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 20, 24, 20));
        card.setPreferredSize(new Dimension(220, 0));

        // Title
        JLabel cardTitle = new JLabel("Adjust stock");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(Color.WHITE);
        cardTitle.setOpaque(true);
        cardTitle.setBackground(ACCENT);
        cardTitle.setBorder(new EmptyBorder(10, 16, 10, 16));
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Selected ingredient label
        lblSelectedName = new JLabel("None selected");
        lblSelectedName.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblSelectedName.setForeground(TEXT_LIGHT);
        lblSelectedName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSelectedName.setBorder(new EmptyBorder(10, 0, 14, 0));

        // Mode buttons
        btnAdd    = modeBtn("+ Add stock");
        btnDeduct = modeBtn("− Deduct stock");
        btnSet    = modeBtn("= Set exact value");
        styleActiveModeBtn(btnAdd); // default active

        btnAdd.addActionListener(e    -> { adjustMode = "ADD";    styleActiveModeBtn(btnAdd);    styleInactiveModeBtn(btnDeduct); styleInactiveModeBtn(btnSet); });
        btnDeduct.addActionListener(e -> { adjustMode = "DEDUCT"; styleActiveModeBtn(btnDeduct); styleInactiveModeBtn(btnAdd);    styleInactiveModeBtn(btnSet); });
        btnSet.addActionListener(e    -> { adjustMode = "SET";    styleActiveModeBtn(btnSet);    styleInactiveModeBtn(btnAdd);    styleInactiveModeBtn(btnDeduct); });

        // Amount field
        JLabel lblAmount = sideLabel("Amount:");
        amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER),
            new EmptyBorder(6, 8, 6, 8)));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Reason field
        JLabel lblReason = sideLabel("Reason:");
        reasonField = new JTextField();
        reasonField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        reasonField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER),
            new EmptyBorder(6, 8, 6, 8)));
        reasonField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        reasonField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Feedback label
        lblFeedback = new JLabel(" ");
        lblFeedback.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblFeedback.setForeground(RED);
        lblFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Apply button
        JButton btnApply = new JButton("Apply change");
        btnApply.setBackground(GOLD);
        btnApply.setForeground(Color.WHITE);
        btnApply.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnApply.setFocusable(false);
        btnApply.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnApply.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnApply.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnApply.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnApply.addActionListener(e -> onApply());

        card.add(cardTitle);
        card.add(lblSelectedName);
        card.add(btnAdd);
        card.add(Box.createVerticalStrut(6));
        card.add(btnDeduct);
        card.add(Box.createVerticalStrut(6));
        card.add(btnSet);
        card.add(Box.createVerticalStrut(16));
        card.add(lblAmount);
        card.add(Box.createVerticalStrut(4));
        card.add(amountField);
        card.add(Box.createVerticalStrut(12));
        card.add(lblReason);
        card.add(Box.createVerticalStrut(4));
        card.add(reasonField);
        card.add(Box.createVerticalStrut(8));
        card.add(lblFeedback);
        card.add(Box.createVerticalGlue());
        card.add(btnApply);

        return card;
    }

    // ─── Actions ──────────────────────────────────────────────────────────────
    private void onSearch() {
        String q = searchField.getText();
        rebuildTable(controller.search(q));
    }

    private void onApply() {
        if (selectedIng == null) {
            lblFeedback.setForeground(RED);
            lblFeedback.setText("Select an ingredient first.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException ex) {
            lblFeedback.setForeground(RED);
            lblFeedback.setText("Enter a valid number.");
            return;
        }

        String reason = reasonField.getText().trim();
        String err = switch (adjustMode) {
            case "ADD"    -> controller.addStock(selectedIng, amount, reason);
            case "DEDUCT" -> controller.deductStock(selectedIng, amount, reason);
            default       -> controller.setExactStock(selectedIng, amount, reason);
        };

        if (err != null) {
            lblFeedback.setForeground(RED);
            lblFeedback.setText("<html>" + err + "</html>");
        } else {
            lblFeedback.setForeground(GREEN);
            lblFeedback.setText("✓ Stock updated.");
            amountField.setText("");
            reasonField.setText("");
            double newStock = controller.getStock(selectedIng);
            lblSelectedName.setText(selectedIng.getDisplayName()
                + "  (" + String.format("%.2f", newStock) + " " + selectedIng.getUnit() + ")");
            rebuildTable(showLowOnly ? controller.getLowStock() : controller.getIngredients());
        }
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────
    private JPanel makeCard() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(DIVIDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
    }

    private JButton toolbarBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT_DARK);
        b.setFocusable(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER),
            new EmptyBorder(7, 16, 7, 16)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton modeBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setFocusable(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        styleInactiveModeBtn(b);
        return b;
    }

    private void styleActiveModeBtn(JButton b) {
        b.setBackground(GOLD_BG);
        b.setForeground(GOLD);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 1),
            new EmptyBorder(5, 10, 5, 10)));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
    }

    private void styleInactiveModeBtn(JButton b) {
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT_DARK);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            new EmptyBorder(5, 10, 5, 10)));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
    }

    private JLabel sideLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void addCell(JPanel row, String text, int style, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, 13));
        l.setForeground(fg);
        row.add(l);
    }
}