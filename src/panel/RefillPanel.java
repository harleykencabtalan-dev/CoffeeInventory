import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

public class RefillPanel extends JPanel {

    // ─── Colours (match ProductionPanel) ─────────────────────────────────────
    private static final Color BG         = new Color(245, 245, 240);
    private static final Color ACCENT     = new Color(120, 90, 70);
    private static final Color TEXT_DARK  = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT = new Color(150, 150, 150);
    private static final Color GREEN      = new Color(50, 140, 50);
    private static final Color RED_ERR    = new Color(180, 50, 50);

    // ─── State ────────────────────────────────────────────────────────────────
    private final InventoryManager im;

    // Form widgets
    private JComboBox<Ingredient> ingredientCombo;
    private JSpinner              amountSpinner;
    private JLabel                lblCurrentStock;
    private JLabel                lblAfterRefill;

    // Log panel (so we can refresh it)
    private JPanel logContent;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public RefillPanel(InventoryManager im) {
        this.im = im;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Refill Station");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("Register Incoming Supplies and Update Stock Levels");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitle.setForeground(TEXT_LIGHT);

        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BODY  =  left form card  +  right log card
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.add(buildFormCard());
        body.add(buildLogCard());
        return body;
    }

    // ─── LEFT: New Intake Form ────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel cardTitle = new JLabel("+ NEW INTAKE");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardTitle.setForeground(ACCENT);
        cardTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Form fields
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        // Ingredient selector
        form.add(formLabel("SELECT RESOURCE"));
        form.add(Box.createVerticalStrut(6));
        ingredientCombo = new JComboBox<>();
        for (Ingredient ing : im.getIngredients()) ingredientCombo.addItem(ing);
        ingredientCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ingredientCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ingredientCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        ingredientCombo.addActionListener(e -> refreshStockLabels());
        form.add(ingredientCombo);
        form.add(Box.createVerticalStrut(20));

        // Amount spinner
        form.add(formLabel("QUANTITY"));
        form.add(Box.createVerticalStrut(6));
        amountSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.1, 99999.0, 1.0));
        amountSpinner.setFont(new Font("SansSerif", Font.BOLD, 16));
        amountSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amountSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountSpinner.addChangeListener(e -> refreshStockLabels());
        form.add(amountSpinner);
        form.add(Box.createVerticalStrut(20));

        // Stock preview
        JPanel previewCard = new JPanel(new GridLayout(1, 2, 10, 0));
        previewCard.setBackground(new Color(250, 248, 244));
        previewCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 210, 195)),
            new EmptyBorder(12, 14, 12, 14)));
        previewCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel currentBox = new JPanel(new GridLayout(2, 1, 0, 2));
        currentBox.setOpaque(false);
        JLabel currentLbl = new JLabel("CURRENT STOCK");
        currentLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        currentLbl.setForeground(TEXT_LIGHT);
        lblCurrentStock = new JLabel("—");
        lblCurrentStock.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblCurrentStock.setForeground(TEXT_DARK);
        currentBox.add(currentLbl);
        currentBox.add(lblCurrentStock);

        JPanel afterBox = new JPanel(new GridLayout(2, 1, 0, 2));
        afterBox.setOpaque(false);
        JLabel afterLbl = new JLabel("AFTER REFILL");
        afterLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        afterLbl.setForeground(TEXT_LIGHT);
        lblAfterRefill = new JLabel("—");
        lblAfterRefill.setFont(new Font("Monospaced", Font.BOLD, 15));
        lblAfterRefill.setForeground(GREEN);
        afterBox.add(afterLbl);
        afterBox.add(lblAfterRefill);

        previewCard.add(currentBox);
        previewCard.add(afterBox);
        form.add(previewCard);

        // Confirm button
        JButton btnRegister = new JButton("REGISTER INTAKE");
        btnRegister.setBackground(GREEN);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnRegister.setFocusable(false);
        btnRegister.setBorder(new EmptyBorder(12, 24, 12, 24));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        
        btnRegister.addActionListener(e -> doRegisterIntake());

        form.add(Box.createVerticalStrut(20));
        form.add(btnRegister);

        card.add(cardTitle, BorderLayout.NORTH);
        card.add(form,      BorderLayout.CENTER);

        // Trigger initial label refresh
        refreshStockLabels();

        return card;
    }

    // ─── RIGHT: Intake Log ────────────────────────────────────────────────────
    private JPanel buildLogCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel cardTitle = new JLabel("INTAKE LOG");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardTitle.setForeground(ACCENT);
        cardTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(cardTitle, BorderLayout.NORTH);

        // Column headers
        JPanel colHdr = new JPanel(new GridLayout(1, 3, 0, 0));
        colHdr.setOpaque(false);
        colHdr.setBorder(new EmptyBorder(0, 0, 6, 0));
        for (String h : new String[]{"RESOURCE", "AMOUNT", "TIMESTAMP"}) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("SansSerif", Font.BOLD, 11));
            hl.setForeground(TEXT_LIGHT);
            colHdr.add(hl);
        }
        card.add(colHdr, BorderLayout.CENTER);

        // Log rows
        logContent = new JPanel();
        logContent.setLayout(new BoxLayout(logContent, BoxLayout.Y_AXIS));
        logContent.setOpaque(false);

        rebuildLog();

        JScrollPane scroll = new JScrollPane(logContent);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.SOUTH);

        // Make scroll take all remaining space
        card.setLayout(new BorderLayout(0, 10));
        card.add(cardTitle, BorderLayout.NORTH);
        card.add(colHdr,    BorderLayout.CENTER);
        card.add(scroll,    BorderLayout.SOUTH);

        // Give scroll more room
        card.setLayout(new BorderLayout(0, 10));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(cardTitle, BorderLayout.NORTH);
        top.add(colHdr,    BorderLayout.SOUTH);
        card.removeAll();
        card.add(top,    BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ─── Actions ──────────────────────────────────────────────────────────────
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

        im.refill(ing, amount);

        refreshStockLabels();
        rebuildLog();

        JOptionPane.showMessageDialog(this,
            String.format("Successfully added %.2f %s of %s.", amount, ing.getUnit(), ing.getDisplayName()),
            "Intake Registered", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshStockLabels() {
           try { amountSpinner.commitEdit(); } catch (Exception ex) { /* ignore */ }
        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) {
            lblCurrentStock.setText("—");
            lblAfterRefill.setText("—");
            return;
        }

        double current = im.getStock(ing);
        double amount  = (Double) amountSpinner.getValue();
        double after   = current + amount;

        lblCurrentStock.setText(String.format("%.2f %s", current, ing.getUnit()));
        lblAfterRefill.setText(String.format("%.2f %s", after, ing.getUnit()));
    }

    private void rebuildLog() {
        logContent.removeAll();

        LinkedList<RefillRecord> log = im.getRefillLog();

        if (log.isEmpty()) {
            JLabel empty = new JLabel("No refills recorded yet.");
            empty.setFont(new Font("Monospaced", Font.PLAIN, 12));
            empty.setForeground(TEXT_LIGHT);
            empty.setBorder(new EmptyBorder(10, 2, 0, 0));
            logContent.add(empty);
        } else {
            // Show most recent first
            ArrayList<RefillRecord> reversed = new ArrayList<>(log);
            Collections.reverse(reversed);
            for (RefillRecord rec : reversed) {
                JPanel row = new JPanel(new GridLayout(1, 3, 0, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 228, 218)));

                JLabel lName = new JLabel(rec.getIngredient().getDisplayName());
                lName.setFont(new Font("SansSerif", Font.PLAIN, 13));
                lName.setForeground(TEXT_DARK);

                JLabel lAmt = new JLabel(String.format("+%.2f %s", rec.getAmount(), rec.getIngredient().getUnit()));
                lAmt.setFont(new Font("Monospaced", Font.BOLD, 13));
                lAmt.setForeground(GREEN);

                JLabel lTime = new JLabel(rec.toString().substring(1, 20)); // extract timestamp
                lTime.setFont(new Font("Monospaced", Font.PLAIN, 11));
                lTime.setForeground(TEXT_LIGHT);

                row.add(lName);
                row.add(lAmt);
                row.add(lTime);

                logContent.add(row);
                logContent.add(Box.createVerticalStrut(2));
            }
        }

        logContent.revalidate();
        logContent.repaint();
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────
    private JPanel makeCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(220, 215, 205));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
                g2.dispose();
            }
        };
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}