package panel.refill;

import javax.swing.*;
import javax.swing.border.*;

import model.Ingredient;
import model.InventoryManager;
import model.RefillRecord;

import java.awt.*;
import java.util.*;

public class RefillPanel extends JPanel {

    // ─── Theme colours (matches DashboardPanel / ConsoleUI) ───────────────────
    private static final Color BG          = new Color(250, 245, 230);
    private static final Color CARD_BG     = new Color(255, 252, 240);
    private static final Color CARD_BORDER = new Color(200, 175, 120);
    private static final Color GOLD        = new Color(180, 140, 50);
    private static final Color GOLD_DARK   = new Color(140, 100, 30);
    private static final Color HEADER_BG   = new Color(160, 120, 50);
    private static final Color ROW_ALT     = new Color(245, 238, 218);
    private static final Color GRID_LINE   = new Color(210, 190, 145);
    private static final Color TEXT_DARK   = new Color(60, 45, 20);
    private static final Color TEXT_MID    = new Color(120, 100, 60);
    private static final Color TEXT_LIGHT  = new Color(170, 150, 100);
    private static final Color GREEN       = new Color(60, 160, 60);
    private static final Color GREEN_DARK  = new Color(40, 120, 40);

    // ─── State ────────────────────────────────────────────────────────────────
    private final RefillController controller;

    private JComboBox<Ingredient> ingredientCombo;
    private JSpinner              amountSpinner;
    private JLabel                lblCurrentStock;
    private JLabel                lblAfterRefill;
    private JPanel                logContent;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public RefillPanel(InventoryManager im) {
        this.controller = new RefillController(im);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    public void refresh() {
        ingredientCombo.removeAllItems();
        for (Ingredient ing : controller.getIngredients()) ingredientCombo.addItem(ing);
        refreshStockLabels();
        rebuildLog();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 18, 4));

        JLabel title = new JLabel("Refill Station");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(GOLD_DARK);

        JLabel subtitle = new JLabel("Register Incoming Supplies and Update Stock Levels");
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
    //  BODY  — left (form, wider) + right (log, narrower)
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
body.setOpaque(false);

GridBagConstraints gbc = new GridBagConstraints();
gbc.fill = GridBagConstraints.BOTH;
gbc.weighty = 1.0;
gbc.gridy = 0;

gbc.weightx = 0.8;
gbc.gridx = 0;
body.add(buildFormCard(), gbc);

gbc.weightx = 0.2;
gbc.gridx = 1;
body.add(buildLogCard(), gbc);

return body;
        
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LEFT: NEW INTAKE FORM
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildFormCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        JLabel headerLbl = new JLabel("  + NEW INTAKE");
        headerLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerLbl.setForeground(Color.WHITE);
        headerLbl.setBackground(HEADER_BG);
        headerLbl.setOpaque(true);
        headerLbl.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.add(headerLbl, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Ingredient selector
        form.add(formLabel("SELECT RESOURCE"));
        form.add(Box.createVerticalStrut(14));
        ingredientCombo = new JComboBox<>();
        for (Ingredient ing : controller.getIngredients()) ingredientCombo.addItem(ing);
        styleCombo(ingredientCombo);
        ingredientCombo.addActionListener(e -> refreshStockLabels());
        form.add(ingredientCombo);
        form.add(Box.createVerticalStrut(28));
      

        // Amount spinner
        form.add(formLabel("QUANTITY TO ADD"));
        form.add(Box.createVerticalStrut(14));
        amountSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.1, 99999.0, 1.0));
        amountSpinner.setFont(new Font("SansSerif", Font.BOLD, 18));
        amountSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        amountSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountSpinner.addChangeListener(e -> refreshStockLabels());
        form.add(amountSpinner);
        form.add(Box.createVerticalStrut(22));

        // Stock preview card
        form.add(formLabel("STOCK PREVIEW"));
        form.add(Box.createVerticalStrut(14));

        JPanel previewCard = new JPanel(new GridLayout(1, 2, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 238, 218));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(GRID_LINE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.drawLine(getWidth()/2, 10, getWidth()/2, getHeight()-10);
                g2.dispose();
            }
        };
        previewCard.setOpaque(false);
        previewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel currentBox = new JPanel(new GridLayout(2, 1, 0, 4));
        currentBox.setOpaque(false);
        currentBox.setBorder(new EmptyBorder(12, 16, 12, 8));
        JLabel currentLbl = new JLabel("CURRENT STOCK");
        currentLbl.setFont(new Font("Monospaced", Font.BOLD, 16));
        currentLbl.setForeground(TEXT_LIGHT);
        lblCurrentStock = new JLabel("—");
        lblCurrentStock.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblCurrentStock.setForeground(TEXT_DARK);
        currentBox.add(currentLbl);
        currentBox.add(lblCurrentStock);

        JPanel afterBox = new JPanel(new GridLayout(2, 1, 0, 4));
        afterBox.setOpaque(false);
        afterBox.setBorder(new EmptyBorder(12, 16, 12, 8));
        JLabel afterLbl = new JLabel("AFTER REFILL");
        afterLbl.setFont(new Font("Monospaced", Font.BOLD, 20));
        afterLbl.setForeground(TEXT_LIGHT);
        lblAfterRefill = new JLabel("—");
        lblAfterRefill.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblAfterRefill.setForeground(GREEN_DARK);
        afterBox.add(afterLbl);
        afterBox.add(lblAfterRefill);

        previewCard.add(currentBox);
        previewCard.add(afterBox);
        form.add(previewCard);
        form.add(Box.createVerticalStrut(36));

        // Register button
        JButton btnRegister = new JButton("REGISTER INTAKE") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? GREEN_DARK : GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnRegister.setOpaque(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRegister.setFocusable(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(e -> doRegisterIntake());
        form.add(btnRegister);

        card.add(form, BorderLayout.CENTER);

        refreshStockLabels();
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  RIGHT: INTAKE LOG
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildLogCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());

        // ── FIX: create headerLbl once and only add it via topStack ──────────
        JLabel headerLbl = new JLabel("  INTAKE LOG");
        headerLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        headerLbl.setForeground(Color.WHITE);
        headerLbl.setBackground(HEADER_BG);
        headerLbl.setOpaque(true);
        headerLbl.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Column headers
        JPanel colHdr = new JPanel(new GridLayout(1, 3, 0, 0));
        colHdr.setBackground(new Color(230, 215, 180));
        colHdr.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, GRID_LINE),
            new EmptyBorder(6, 12, 6, 12)));
        for (String h : new String[]{"RESOURCE", "AMOUNT", "TIMESTAMP"}) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("SansSerif", Font.BOLD, 10));
            hl.setForeground(GOLD_DARK);
            colHdr.add(hl);
        }

        // Log rows
        logContent = new JPanel();
        logContent.setLayout(new BoxLayout(logContent, BoxLayout.Y_AXIS));
        logContent.setOpaque(false);
        rebuildLog();

        JScrollPane scroll = new JScrollPane(logContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // Stack header bar + column header — headerLbl added here only
        JPanel topStack = new JPanel(new BorderLayout());
        topStack.setOpaque(false);
        topStack.add(headerLbl, BorderLayout.NORTH);
        topStack.add(colHdr,    BorderLayout.SOUTH);

        card.add(topStack, BorderLayout.NORTH);
        card.add(scroll,   BorderLayout.CENTER);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ═════════════════════════════════════════════════════════════════════════
    private void doRegisterIntake() {
        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) {
            JOptionPane.showMessageDialog(this, "Please select an ingredient.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try { amountSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }
        double amount = (Double) amountSpinner.getValue();
        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }
        controller.registerIntake(ing, amount);
        refreshStockLabels();
        rebuildLog();
        JOptionPane.showMessageDialog(this,
            String.format("Successfully added %.2f %s of %s.", amount, ing.getUnit(), ing.getDisplayName()),
            "Intake Registered", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshStockLabels() {
        try { amountSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }
        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) { lblCurrentStock.setText("—"); lblAfterRefill.setText("—"); return; }
        double current = controller.getCurrentStock(ing);
        double amount  = (Double) amountSpinner.getValue();
        lblCurrentStock.setText(String.format("%.2f %s", current, ing.getUnit()));
        lblAfterRefill.setText(String.format("%.2f %s", current + amount, ing.getUnit()));
    }

    private void rebuildLog() {
        logContent.removeAll();
        ArrayList<RefillRecord> reversed = controller.getRefillLogReversed();

        if (reversed.isEmpty()) {
            JLabel empty = new JLabel("No refills recorded yet.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 12));
            empty.setForeground(TEXT_LIGHT);
            empty.setBorder(new EmptyBorder(14, 14, 0, 0));
            logContent.add(empty);
        } else {
            for (int i = 0; i < reversed.size(); i++) {
                RefillRecord rec = reversed.get(i);
                boolean alt = i % 2 == 0;

                JPanel row = new JPanel(new GridLayout(1, 3, 0, 0));
                row.setBackground(alt ? CARD_BG : ROW_ALT);
                row.setBorder(new EmptyBorder(8, 12, 8, 12));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel lName = new JLabel(rec.getIngredient().getDisplayName());
                lName.setFont(new Font("SansSerif", Font.PLAIN, 12));
                lName.setForeground(TEXT_DARK);

                JLabel lAmt = new JLabel(String.format("+%.2f %s", rec.getAmount(), rec.getIngredient().getUnit()));
                lAmt.setFont(new Font("Monospaced", Font.BOLD, 12));
                lAmt.setForeground(GREEN_DARK);

                // Parse timestamp from toString: "[yyyy-MM-dd HH:mm:ss] ..."
                String ts = rec.toString();
                if (ts.startsWith("[")) {
                    int end = ts.indexOf("]");
                    ts = end > 1 ? ts.substring(1, end) : ts;
                }
                JLabel lTime = new JLabel(ts);
                lTime.setFont(new Font("Monospaced", Font.PLAIN, 10));
                lTime.setForeground(TEXT_LIGHT);

                row.add(lName);
                row.add(lAmt);
                row.add(lTime);
                logContent.add(row);

                // Grid line between rows
                if (i < reversed.size() - 1) {
                    JPanel line = new JPanel();
                    line.setBackground(GRID_LINE);
                    line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    line.setPreferredSize(new Dimension(0, 1));
                    line.setAlignmentX(Component.LEFT_ALIGNMENT);
                    logContent.add(line);
                }
            }
        }
        logContent.revalidate();
        logContent.repaint();
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

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        combo.setFont(new Font("SansSerif", Font.PLAIN, 18));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT_DARK);
    }
}