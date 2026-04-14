import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ProductionPanel extends JPanel {

    // ─── Colours (match partner's design exactly) ────────────────────────────────
    private static final Color BG        = new Color(245, 245, 240);
    private static final Color ACCENT    = new Color(120, 90, 70);   // brown
    private static final Color TEXT_DARK = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT= new Color(150, 150, 150);
    private static final Color GREEN     = new Color(50, 140, 50);
    private static final Color RED_ERR   = new Color(180, 50, 50);

    // ─── State ───────────────────────────────────────────────────────────────────
    private final InventoryManager im;

    // Step-1 selections
    private CoffeeType  selectedCoffee;
    private String      selectedSize   = "Medium";
    private String      selectedTemp   = "Hot";
    private int         quantity       = 1;
    private boolean     isCustomized   = false;

    // Step-2 custom recipe  (ingredient → amount override)
    private final Map<Ingredient, Double> customRecipe = new LinkedHashMap<>();

    // Step indicator labels (so we can repaint active state)
    private final JPanel[] stepCards = new JPanel[3];
    private final JLabel[] stepLabels = new JLabel[3];
    private int currentStep = 0;          // 0-based

    // Card layout that drives the 3-step content
    private CardLayout stepCardLayout;
    private JPanel     stepContentPanel;

    // Step-1 widgets we need to read back
    private JComboBox<CoffeeType> coffeeCombo;
    private JSpinner  qtySpinner;
    private JLabel    lblMaxProducible;

    // Step-3 summary
    private JPanel    summaryPanel;

    // ─── Constructor ─────────────────────────────────────────────────────────────

    public ProductionPanel(InventoryManager im) {
        this.im = im;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildHeader(),    BorderLayout.NORTH);
        add(buildBody(),      BorderLayout.CENTER);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  HEADER  (partner's original, untouched)
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Production Control");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(ACCENT);

        JLabel subtitle = new JLabel("Configure, Customize, and Execute Production Orders");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitle.setForeground(TEXT_LIGHT);

        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  BODY  =  step indicator  +  main card
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel indicator = buildStepIndicator();
        indicator.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(indicator);
        body.add(Box.createVerticalStrut(24));

        JPanel mainCard = buildMainCard();
        mainCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(mainCard);

        body.add(Box.createVerticalGlue());
        return body;
    }

    // ─── Step indicator (partner's visual, now dynamic) ──────────────────────────

    private JPanel buildStepIndicator() {
        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String[][] steps = {{"01", "SELECT COFFEE"}, {"02", "INGREDIENTS"}, {"03", "PRODUCTION"}};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean active = (i == currentStep);
            JPanel card = createMiniStepCard(steps[i][0], steps[i][1], active);
            JLabel lbl = (JLabel) ((BorderLayout) card.getLayout() == null
                    ? card.getComponent(0)
                    : card.getComponent(0));
            stepCards[i]  = card;
            stepLabels[i] = lbl;
            row.add(card);
        }
        return row;
    }

    private JPanel createMiniStepCard(String num, String title, boolean active) {
        JPanel mini = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // find my own index to check live active state
                int myIdx = -1;
                for (int i = 0; i < stepCards.length; i++) if (stepCards[i] == this) { myIdx = i; break; }
                boolean act = (myIdx == currentStep);
                g2.setColor(act ? ACCENT : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(220, 215, 205));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        mini.setOpaque(false);

        JLabel text = new JLabel(num + "  " + title);
        text.setFont(new Font("SansSerif", Font.BOLD, 13));
        text.setForeground(active ? Color.WHITE : TEXT_LIGHT);
        text.setHorizontalAlignment(JLabel.CENTER);
        mini.add(text, BorderLayout.CENTER);
        mini.setPreferredSize(new Dimension(0, 60));
        return mini;
    }

    private void goToStep(int step) {
        currentStep = step;
        // Repaint step cards
        for (int i = 0; i < 3; i++) {
            boolean act = (i == step);
            JLabel lbl = (JLabel) stepCards[i].getComponent(0);
            lbl.setForeground(act ? Color.WHITE : TEXT_LIGHT);
            stepCards[i].repaint();
        }
        stepCardLayout.show(stepContentPanel, "STEP" + step);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  MAIN CARD  (partner's rounded white card, now with real content)
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel buildMainCard() {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 580));
        card.setPreferredSize(new Dimension(0, 580));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Inner card-layout that switches between the 3 step panels
        stepCardLayout    = new CardLayout();
        stepContentPanel  = new JPanel(stepCardLayout);
        stepContentPanel.setOpaque(false);

        stepContentPanel.add(buildStep1(), "STEP0");
        stepContentPanel.add(buildStep2(), "STEP1");
        stepContentPanel.add(buildStep3(), "STEP2");

        card.add(stepContentPanel, BorderLayout.CENTER);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  STEP 1 — SELECT COFFEE
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel buildStep1() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);

        // ── Title row ──
        JLabel stepTitle = stepHeading("Step 1 — Select Your Coffee");
        root.add(stepTitle, BorderLayout.NORTH);

        // ── Main form (two columns) ──
        JPanel form = new JPanel(new GridLayout(1, 2, 40, 0));
        form.setOpaque(false);

        // LEFT COLUMN
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        // Coffee type
        left.add(formLabel("Coffee Flavor:"));
        left.add(Box.createVerticalStrut(6));
        coffeeCombo = new JComboBox<>();
        for (CoffeeType ct : im.getCoffeeTypes()) coffeeCombo.addItem(ct);
        coffeeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        coffeeCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        coffeeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(coffeeCombo);
        left.add(Box.createVerticalStrut(20));

        // Temperature
        left.add(formLabel("Temperature:"));
        left.add(Box.createVerticalStrut(6));
        JPanel tempRow = new JPanel(new GridLayout(1, 2, 10, 0));
        tempRow.setOpaque(false);
        tempRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tempRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnHot  = toggleBtn("☀  Hot",  true);
        JToggleButton btnIced = toggleBtn("❄  Iced", false);
        ButtonGroup   tempGrp = new ButtonGroup();
        tempGrp.add(btnHot); tempGrp.add(btnIced);
        btnHot.addActionListener(e  -> selectedTemp = "Hot");
        btnIced.addActionListener(e -> selectedTemp = "Iced");
        tempRow.add(btnHot);
        tempRow.add(btnIced);
        left.add(tempRow);
        left.add(Box.createVerticalStrut(20));

        // Cup size
        left.add(formLabel("Cup Size:"));
        left.add(Box.createVerticalStrut(6));
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 10, 0));
        sizeRow.setOpaque(false);
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        sizeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnSm = toggleBtn("Small",  false);
        JToggleButton btnMd = toggleBtn("Medium", true);
        JToggleButton btnLg = toggleBtn("Large",  false);
        ButtonGroup   sGrp  = new ButtonGroup();
        sGrp.add(btnSm); sGrp.add(btnMd); sGrp.add(btnLg);
        btnSm.addActionListener(e -> selectedSize = "Small");
        btnMd.addActionListener(e -> selectedSize = "Medium");
        btnLg.addActionListener(e -> selectedSize = "Large");
        sizeRow.add(btnSm); sizeRow.add(btnMd); sizeRow.add(btnLg);
        left.add(sizeRow);

        // RIGHT COLUMN
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        // Quantity
        right.add(formLabel("Quantity:"));
        right.add(Box.createVerticalStrut(6));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setFont(new Font("SansSerif", Font.BOLD, 18));
        qtySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        qtySpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(qtySpinner);
        right.add(Box.createVerticalStrut(16));

        // Max producible read-out
        lblMaxProducible = new JLabel("Max producible: —");
        lblMaxProducible.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lblMaxProducible.setForeground(TEXT_LIGHT);
        lblMaxProducible.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(lblMaxProducible);
        right.add(Box.createVerticalStrut(20));

        // Order type
        right.add(formLabel("Order Type:"));
        right.add(Box.createVerticalStrut(6));
        JPanel orderRow = new JPanel(new GridLayout(1, 2, 10, 0));
        orderRow.setOpaque(false);
        orderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        orderRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnDefault = toggleBtn("Default",    true);
        JToggleButton btnCustom  = toggleBtn("Customized", false);
        ButtonGroup   oGrp = new ButtonGroup();
        oGrp.add(btnDefault); oGrp.add(btnCustom);
        btnDefault.addActionListener(e -> isCustomized = false);
        btnCustom.addActionListener(e  -> isCustomized = true);
        orderRow.add(btnDefault);
        orderRow.add(btnCustom);
        right.add(orderRow);

        form.add(left);
        form.add(right);

        // Update max-producible whenever combo changes
        coffeeCombo.addActionListener(e -> refreshMaxLabel());
        refreshMaxLabel();

        // ── NEXT button ──
        JButton btnNext = primaryBtn("NEXT →  REVIEW INGREDIENTS");
        btnNext.addActionListener(e -> onStep1Next());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnNext);

        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        return root;
    }

    private void refreshMaxLabel() {
        CoffeeType ct = (CoffeeType) coffeeCombo.getSelectedItem();
        if (ct == null) return;
        int max = im.maxProducible(ct);
        lblMaxProducible.setText("Max producible: " + max + " cups");
        lblMaxProducible.setForeground(max > 0 ? GREEN : RED_ERR);
    }

    private void onStep1Next() {
        selectedCoffee = (CoffeeType) coffeeCombo.getSelectedItem();
        quantity       = (int) qtySpinner.getValue();

        if (selectedCoffee == null) {
            JOptionPane.showMessageDialog(this, "Please select a coffee type.", "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be at least 1.", "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Seed custom recipe from the real recipe
        customRecipe.clear();
        customRecipe.putAll(selectedCoffee.getRecipe());

        rebuildStep2();
        goToStep(1);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  STEP 2 — INGREDIENTS
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel step2Root;   // kept so rebuildStep2 can swap content

    private JPanel buildStep2() {
        step2Root = new JPanel(new BorderLayout(0, 16));
        step2Root.setOpaque(false);
        return step2Root;
    }

    private void rebuildStep2() {
        step2Root.removeAll();

        // Title
        step2Root.add(stepHeading("Step 2 — " + (isCustomized ? "Customize Recipe" : "Review Ingredients")),
                      BorderLayout.NORTH);

        // Recipe table area
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Column headers
        JPanel colHdr = new JPanel(new GridLayout(1, isCustomized ? 4 : 3, 0, 0));
        colHdr.setOpaque(false);
        colHdr.setBorder(new EmptyBorder(0, 0, 6, 0));
        for (String h : isCustomized
                ? new String[]{"INGREDIENT", "UNIT", "AMT / CUP", "ADJUST"}
                : new String[]{"INGREDIENT", "UNIT", "AMT / CUP"}) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("SansSerif", Font.BOLD, 11));
            hl.setForeground(TEXT_LIGHT);
            colHdr.add(hl);
        }
        tableWrap.add(colHdr, BorderLayout.NORTH);

        // Rows
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        for (Map.Entry<Ingredient, Double> entry : new ArrayList<>(customRecipe.entrySet())) {
            Ingredient ing    = entry.getKey();
            double     amt    = entry.getValue();
            double     stock  = im.getStock(ing);
            double     needed = amt * quantity;
            boolean    ok     = stock >= needed;

            JPanel row = new JPanel(new GridLayout(1, isCustomized ? 4 : 3, 0, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 228, 218)));

            JLabel lName = new JLabel(ing.getDisplayName());
            lName.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lName.setForeground(TEXT_DARK);

            JLabel lUnit = new JLabel(ing.getUnit());
            lUnit.setFont(new Font("Monospaced", Font.PLAIN, 13));
            lUnit.setForeground(TEXT_LIGHT);

            JLabel lAmt = new JLabel(String.format("%.2f  (need %.2f)", amt, needed));
            lAmt.setFont(new Font("Monospaced", Font.PLAIN, 13));
            lAmt.setForeground(ok ? TEXT_DARK : RED_ERR);

            row.add(lName);
            row.add(lUnit);
            row.add(lAmt);

            if (isCustomized) {
                JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                ctrl.setOpaque(false);

                JButton btnMinus = microBtn("−");
                JButton btnPlus  = microBtn("+");
                JButton btnDel   = microBtn("✕");
                btnDel.setForeground(RED_ERR);

                btnMinus.addActionListener(e -> {
                    double cur = customRecipe.getOrDefault(ing, 0.0);
                    double next = Math.max(0, cur - 1);
                    if (next == 0) customRecipe.remove(ing);
                    else           customRecipe.put(ing, next);
                    rebuildStep2();
                });
                btnPlus.addActionListener(e -> {
                    customRecipe.put(ing, customRecipe.getOrDefault(ing, 0.0) + 1);
                    rebuildStep2();
                });
                btnDel.addActionListener(e -> {
                    customRecipe.remove(ing);
                    rebuildStep2();
                });

                ctrl.add(btnMinus); ctrl.add(btnPlus); ctrl.add(btnDel);
                row.add(ctrl);
            }

            rows.add(row);
            rows.add(Box.createVerticalStrut(2));
        }

        // Add-on row (customized only)
        if (isCustomized) {
            rows.add(Box.createVerticalStrut(10));
            JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            addRow.setOpaque(false);
            addRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

            JComboBox<Ingredient> addCombo = new JComboBox<>();
            for (Ingredient ing : im.getIngredients()) {
                if (!customRecipe.containsKey(ing)) addCombo.addItem(ing);
            }
            addCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
            addCombo.setPreferredSize(new Dimension(160, 30));

            JSpinner addAmt = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 9999.0, 0.5));
            addAmt.setPreferredSize(new Dimension(70, 30));
            addAmt.setFont(new Font("SansSerif", Font.PLAIN, 12));

            JButton btnAddIng = new JButton("+ ADD");
            btnAddIng.setBackground(ACCENT);
            btnAddIng.setForeground(Color.WHITE);
            btnAddIng.setFont(new Font("SansSerif", Font.BOLD, 11));
            btnAddIng.setFocusable(false);
            btnAddIng.setBorder(new EmptyBorder(5, 10, 5, 10));
            btnAddIng.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAddIng.addActionListener(e -> {
                Ingredient sel = (Ingredient) addCombo.getSelectedItem();
                if (sel != null) {
                    customRecipe.put(sel, (Double) addAmt.getValue());
                    rebuildStep2();
                }
            });

            JLabel addLbl = new JLabel("Add ingredient:");
            addLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            addLbl.setForeground(TEXT_LIGHT);
            addRow.add(addLbl); addRow.add(addCombo); addRow.add(addAmt); addRow.add(btnAddIng);
            rows.add(addRow);
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(0, 0, 0, 0));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        tableWrap.add(scroll, BorderLayout.CENTER);
        step2Root.add(tableWrap, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);

        JButton btnBack = ghostBtn("← BACK");
        btnBack.addActionListener(e -> goToStep(0));

        JButton btnNext = primaryBtn("NEXT →  CONFIRM PRODUCTION");
        btnNext.addActionListener(e -> onStep2Next());

        btnRow.add(btnBack, BorderLayout.WEST);
        btnRow.add(btnNext, BorderLayout.EAST);
        step2Root.add(btnRow, BorderLayout.SOUTH);

        step2Root.revalidate();
        step2Root.repaint();
    }

    private void onStep2Next() {
        // Check feasibility using customRecipe
        StringBuilder err = new StringBuilder();
        for (Map.Entry<Ingredient, Double> e : customRecipe.entrySet()) {
            double needed = e.getValue() * quantity;
            double avail  = im.getStock(e.getKey());
            if (avail < needed) {
                err.append(String.format("• %s: need %.2f %s, have %.2f\n",
                    e.getKey().getDisplayName(), needed, e.getKey().getUnit(), avail));
            }
        }
        if (err.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Cannot produce — insufficient stock:\n\n" + err,
                "Stock Shortage", JOptionPane.ERROR_MESSAGE);
            return;
        }
        rebuildStep3();
        goToStep(2);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  STEP 3 — CONFIRM PRODUCTION
    // ═══════════════════════════════════════════════════════════════════════════════

    private JPanel step3Root;

    private JPanel buildStep3() {
        step3Root = new JPanel(new BorderLayout(0, 16));
        step3Root.setOpaque(false);
        return step3Root;
    }

    private void rebuildStep3() {
        step3Root.removeAll();

        step3Root.add(stepHeading("Step 3 — Confirm Production"), BorderLayout.NORTH);

        // Summary card
        JPanel summary = new JPanel(new BorderLayout(0, 10));
        summary.setOpaque(false);

        // Order summary header
        JPanel orderHdr = new JPanel(new GridLayout(1, 3, 20, 0));
        orderHdr.setOpaque(false);
        orderHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        orderHdr.add(summaryStatCard("COFFEE",   selectedCoffee.getDisplayName(), ACCENT));
        orderHdr.add(summaryStatCard("QUANTITY", quantity + " cups",              new Color(80, 120, 80)));
        orderHdr.add(summaryStatCard("ORDER",    isCustomized ? "Customized" : "Default", new Color(80, 100, 160)));
        summary.add(orderHdr, BorderLayout.NORTH);

        // Recipe breakdown
        JPanel breakdown = new JPanel();
        breakdown.setLayout(new BoxLayout(breakdown, BoxLayout.Y_AXIS));
        breakdown.setOpaque(false);
        breakdown.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel brkTitle = new JLabel("RECIPE BREAKDOWN");
        brkTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        brkTitle.setForeground(TEXT_LIGHT);
        brkTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        breakdown.add(brkTitle);
        breakdown.add(Box.createVerticalStrut(8));

        for (Map.Entry<Ingredient, Double> e : customRecipe.entrySet()) {
            Ingredient ing    = e.getKey();
            double     perCup = e.getValue();
            double     total  = perCup * quantity;
            double     stock  = im.getStock(ing);

            JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 228, 218)));

            addSummaryCell(row, ing.getDisplayName(),                          Font.PLAIN,  TEXT_DARK);
            addSummaryCell(row, String.format("%.2f/cup", perCup),             Font.PLAIN,  TEXT_LIGHT);
            addSummaryCell(row, String.format("Total: %.2f %s", total, ing.getUnit()), Font.BOLD, ACCENT);
            addSummaryCell(row, String.format("Stock: %.2f", stock),           Font.PLAIN,  stock >= total ? GREEN : RED_ERR);

            breakdown.add(row);
            breakdown.add(Box.createVerticalStrut(2));
        }

        JScrollPane scroll = new JScrollPane(breakdown);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        summary.add(scroll, BorderLayout.CENTER);
        step3Root.add(summary, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnBack    = ghostBtn("← BACK");
        JButton btnConfirm = primaryBtn("✓  CONFIRM & PRODUCE");
        JButton btnCancel  = new JButton("✕  CANCEL");
        btnCancel.setBackground(new Color(245, 245, 240));
        btnCancel.setForeground(RED_ERR);
        btnCancel.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnCancel.setFocusable(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 170)));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e    -> goToStep(1));
        btnCancel.addActionListener(e  -> resetToStep1());
        btnConfirm.addActionListener(e -> doConfirmProduction());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnCancel);
        rightBtns.add(btnConfirm);

        btnRow.add(btnBack,    BorderLayout.WEST);
        btnRow.add(rightBtns,  BorderLayout.EAST);
        step3Root.add(btnRow, BorderLayout.SOUTH);

        step3Root.revalidate();
        step3Root.repaint();
    }

    private void doConfirmProduction() {
        // Produce using customRecipe by temporarily creating a synthetic CoffeeType
        // (InventoryManager.produce() uses the recipe map, not a DB call)
        CoffeeType synth = new CoffeeType(
            selectedCoffee.getId(),
            selectedCoffee.getDisplayName() + (isCustomized ? " (Custom)" : ""),
            Collections.unmodifiableMap(new LinkedHashMap<>(customRecipe))
        );

        im.produce(synth, quantity);

        // Show undo window (soft reversal within 5 seconds)
        showUndoNotification(synth, quantity);

        resetToStep1();
    }

    private void showUndoNotification(CoffeeType ct, int qty) {
        // Non-blocking toast-style dialog with countdown
        JDialog toast = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), false);
        toast.setUndecorated(true);
        toast.setSize(420, 90);
        toast.setLocationRelativeTo(this);

        JPanel bg = new JPanel(new BorderLayout(10, 0));
        bg.setBackground(new Color(40, 40, 40));
        bg.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel msg = new JLabel("✓  Produced " + qty + "× " + ct.getDisplayName());
        msg.setFont(new Font("SansSerif", Font.BOLD, 13));
        msg.setForeground(Color.WHITE);

        JButton btnUndo = new JButton("UNDO (5s)");
        btnUndo.setBackground(new Color(200, 80, 80));
        btnUndo.setForeground(Color.WHITE);
        btnUndo.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnUndo.setFocusable(false);
        btnUndo.setBorder(new EmptyBorder(6, 12, 6, 12));

        bg.add(msg,     BorderLayout.CENTER);
        bg.add(btnUndo, BorderLayout.EAST);
        toast.add(bg);
        toast.setVisible(true);

        // 5-second countdown, then auto-close
       javax.swing.Timer[] timerHolder = new javax.swing.Timer[1];
        int[]   seconds     = {5};

       
        timerHolder[0] = new javax.swing.Timer(1000, null);
        timerHolder[0].addActionListener(e2 -> {
            seconds[0]--;
            btnUndo.setText("UNDO (" + seconds[0] + "s)");
            if (seconds[0] <= 0) {
                timerHolder[0].stop();
                toast.dispose();
            }
        });
        timerHolder[0].start();

        // Undo: refill everything back, remove last production log entry
        btnUndo.addActionListener(e2 -> {
            timerHolder[0].stop();
            toast.dispose();
            // Reverse stock deductions
            for (Map.Entry<Ingredient, Double> entry : ct.getRecipe().entrySet()) {
                im.refill(entry.getKey(), entry.getValue() * qty);
            }
            // Pop the production log (the refill also logged — remove that refill entry)
            // (In-session: remove last productionLog entry and the refill entries we just added)
            if (!im.getProductionLog().isEmpty()) im.getProductionLog().pop();
            // Remove the compensation refill entries (one per ingredient)
            for (int i = 0; i < ct.getRecipe().size(); i++) {
                if (!im.getRefillLog().isEmpty()) im.getRefillLog().removeLast();
            }
            JOptionPane.showMessageDialog(this, "Production reversed successfully.", "Undo Complete", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void resetToStep1() {
        selectedCoffee = null;
        quantity       = 1;
        isCustomized   = false;
        customRecipe.clear();
        if (coffeeCombo != null) coffeeCombo.setSelectedIndex(0);
        if (qtySpinner  != null) qtySpinner.setValue(1);
        refreshMaxLabel();
        goToStep(0);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════════════════════

    private JLabel stepHeading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXT_LIGHT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JToggleButton toggleBtn(String text, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setFocusable(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 185)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Custom paint so selected = brown, unselected = white
        b.addChangeListener(e -> {
            b.setBackground(b.isSelected() ? ACCENT : Color.WHITE);
            b.setForeground(b.isSelected() ? Color.WHITE : TEXT_DARK);
        });
        b.setBackground(selected ? ACCENT : Color.WHITE);
        b.setForeground(selected ? Color.WHITE : TEXT_DARK);
        return b;
    }

    private JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusable(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT_DARK);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(new Color(210, 200, 185)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton microBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(new Color(245, 240, 232));
        b.setForeground(TEXT_DARK);
        b.setFocusable(false);
        b.setBorder(new EmptyBorder(3, 8, 3, 8));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel summaryStatCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 210, 195)),
            new EmptyBorder(10, 14, 10, 14)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(TEXT_LIGHT);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 15));
        val.setForeground(accent);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    private void addSummaryCell(JPanel row, String text, int style, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", style, 12));
        l.setForeground(fg);
        l.setBorder(new EmptyBorder(0, 2, 0, 2));
        row.add(l);
    }
}
