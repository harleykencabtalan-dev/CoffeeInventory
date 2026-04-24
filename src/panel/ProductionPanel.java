import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ProductionPanel extends JPanel {

    // ─── Colours ─────────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(245, 245, 240);
    private static final Color ACCENT    = new Color(120, 90, 70);
    private static final Color TEXT_DARK = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT= new Color(150, 150, 150);
    private static final Color GREEN     = new Color(50, 140, 50);
    private static final Color RED_ERR   = new Color(180, 50, 50);

    // ─── Inner class: one independent order slot ─────────────────────────────────
    /**
     * Each OrderItem represents ONE coffee cup order with its own
     * size, temperature, and (optionally) a customised recipe.
     */
    private static class OrderItem {
        CoffeeType coffeeType;
        String     size        = "Medium";
        String     temp        = "Hot";
        boolean    isCustomized= false;
        Map<Ingredient, Double> recipe = new LinkedHashMap<>();

        OrderItem(CoffeeType ct) {
            this.coffeeType = ct;
            recipe.putAll(ct.getRecipe());
        }

        /** Label shown in the coffee-selector list inside Step 2. */
        String label(int index) {
            return "Coffee " + (index + 1) + " — " + coffeeType.getDisplayName()
                    + "  [" + size + ", " + temp + (isCustomized ? ", Custom" : "") + "]";
        }
    }

    // ─── State ───────────────────────────────────────────────────────────────────
    private final InventoryManager im;

    // Step-1 selections (still single coffee + qty → generates the list)
    private CoffeeType  selectedCoffee;
    private String      selectedSize   = "Medium";
    private String      selectedTemp   = "Hot";
    private int         quantity       = 1;
    private boolean     isCustomized   = false;   // default for new orders

    // *** NEW: one OrderItem per cup ***
    private List<OrderItem> orders = new ArrayList<>();
    // *** NEW: which order is being customised in Step 2 ***
    private int editingOrderIndex = 0;

    // Step indicator
    private final JPanel[] stepCards  = new JPanel[3];
    private final JLabel[] stepLabels = new JLabel[3];
    private int currentStep = 0;

    private CardLayout stepCardLayout;
    private JPanel     stepContentPanel;

    // Step-1 widgets
    private JComboBox<CoffeeType> coffeeCombo;
    private JSpinner  qtySpinner;
    private JLabel    lblMaxProducible;

    // Step-2 widgets (rebuilt each time)
    private JPanel step2Root;

    // Step-3 widgets (rebuilt each time)
    private JPanel step3Root;

    // ─── Constructor ─────────────────────────────────────────────────────────────
    public ProductionPanel(InventoryManager im) {
        this.im = im;
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ═══ HEADER ══════════════════════════════════════════════════════════════════
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

    // ═══ BODY ═════════════════════════════════════════════════════════════════════
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

    // ═══ STEP INDICATOR ══════════════════════════════════════════════════════════
    private JPanel buildStepIndicator() {
        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        String[][] steps = {{"01","SELECT COFFEE"},{"02","INGREDIENTS"},{"03","PRODUCTION"}};
        for (int i = 0; i < 3; i++) {
            boolean active = (i == currentStep);
            JPanel card = createMiniStepCard(steps[i][0], steps[i][1], active);
            stepCards[i]  = card;
            stepLabels[i] = (JLabel) card.getComponent(0);
            row.add(card);
        }
        return row;
    }

    private JPanel createMiniStepCard(String num, String title, boolean active) {
        JPanel mini = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int myIdx = -1;
                for (int i = 0; i < stepCards.length; i++) if (stepCards[i] == this) { myIdx = i; break; }
                boolean act = (myIdx == currentStep);
                g2.setColor(act ? ACCENT : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(220, 215, 205));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
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
        for (int i = 0; i < 3; i++) {
            JLabel lbl = (JLabel) stepCards[i].getComponent(0);
            lbl.setForeground(i == step ? Color.WHITE : TEXT_LIGHT);
            stepCards[i].repaint();
        }
        stepCardLayout.show(stepContentPanel, "STEP" + step);
    }

    // ═══ MAIN CARD ════════════════════════════════════════════════════════════════
    private JPanel buildMainCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(220, 215, 205));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 580));
        card.setPreferredSize(new Dimension(0, 580));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        stepCardLayout   = new CardLayout();
        stepContentPanel = new JPanel(stepCardLayout);
        stepContentPanel.setOpaque(false);

        stepContentPanel.add(buildStep1(), "STEP0");
        stepContentPanel.add(buildStep2(), "STEP1");
        stepContentPanel.add(buildStep3(), "STEP2");

        card.add(stepContentPanel, BorderLayout.CENTER);
        return card;
    }

    // ═══ STEP 1 — SELECT COFFEE ══════════════════════════════════════════════════
    private JPanel buildStep1() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);

        root.add(stepHeading("Step 1 — Select Your Coffee"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(1, 2, 40, 0));
        form.setOpaque(false);

        // LEFT
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        left.add(formLabel("Coffee Flavor:"));
        left.add(Box.createVerticalStrut(6));
        coffeeCombo = new JComboBox<>();
        for (CoffeeType ct : im.getCoffeeTypes()) coffeeCombo.addItem(ct);
        coffeeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        coffeeCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        coffeeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(coffeeCombo);
        left.add(Box.createVerticalStrut(20));

        left.add(formLabel("Temperature:"));
        left.add(Box.createVerticalStrut(6));
        JPanel tempRow = new JPanel(new GridLayout(1, 2, 10, 0));
        tempRow.setOpaque(false);
        tempRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tempRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnHot  = toggleBtn("☀  Hot",  true);
        JToggleButton btnIced = toggleBtn("❄  Iced", false);
        ButtonGroup tempGrp = new ButtonGroup();
        tempGrp.add(btnHot); tempGrp.add(btnIced);
        btnHot .addActionListener(e -> { selectedTemp = "Hot";  refreshMaxLabel(); });
        btnIced.addActionListener(e -> { selectedTemp = "Iced"; refreshMaxLabel(); });
        tempRow.add(btnHot); tempRow.add(btnIced);
        left.add(tempRow);
        left.add(Box.createVerticalStrut(20));

        left.add(formLabel("Cup Size:"));
        left.add(Box.createVerticalStrut(6));
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 10, 0));
        sizeRow.setOpaque(false);
        sizeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        sizeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnSm = toggleBtn("Small",  false);
        JToggleButton btnMd = toggleBtn("Medium", true);
        JToggleButton btnLg = toggleBtn("Large",  false);
        ButtonGroup sGrp = new ButtonGroup();
        sGrp.add(btnSm); sGrp.add(btnMd); sGrp.add(btnLg);
        btnSm.addActionListener(e -> { selectedSize = "Small";  refreshMaxLabel(); });
        btnMd.addActionListener(e -> { selectedSize = "Medium"; refreshMaxLabel(); });
        btnLg.addActionListener(e -> { selectedSize = "Large";  refreshMaxLabel(); });
        sizeRow.add(btnSm); sizeRow.add(btnMd); sizeRow.add(btnLg);
        left.add(sizeRow);

        // RIGHT
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);

        right.add(formLabel("Quantity:"));
        right.add(Box.createVerticalStrut(6));
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setFont(new Font("SansSerif", Font.BOLD, 18));
        qtySpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        qtySpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        qtySpinner.addChangeListener(e -> refreshMaxLabel());
        right.add(qtySpinner);
        right.add(Box.createVerticalStrut(16));

        lblMaxProducible = new JLabel("Max producible: —");
        lblMaxProducible.setFont(new Font("Monospaced", Font.PLAIN, 13));
        lblMaxProducible.setForeground(TEXT_LIGHT);
        lblMaxProducible.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(lblMaxProducible);
        right.add(Box.createVerticalStrut(20));

        right.add(formLabel("Order Type:"));
        right.add(Box.createVerticalStrut(6));
        JPanel orderRow = new JPanel(new GridLayout(1, 2, 10, 0));
        orderRow.setOpaque(false);
        orderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        orderRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JToggleButton btnDefault = toggleBtn("Default",    true);
        JToggleButton btnCustom  = toggleBtn("Customized", false);
        ButtonGroup oGrp = new ButtonGroup();
        oGrp.add(btnDefault); oGrp.add(btnCustom);
        btnDefault.addActionListener(e -> isCustomized = false);
        btnCustom .addActionListener(e -> isCustomized = true);
        orderRow.add(btnDefault); orderRow.add(btnCustom);
        right.add(orderRow);

        form.add(left);
        form.add(right);

        coffeeCombo.addActionListener(e -> refreshMaxLabel());
        refreshMaxLabel();

        JButton btnNext = primaryBtn("NEXT →  REVIEW INGREDIENTS");
        btnNext.addActionListener(e -> onStep1Next());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnNext);

        root.add(form,   BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        return root;
    }

    private double getSizeMultiplier() {
        return switch (selectedSize) {
            case "Small" -> 0.75;
            case "Large" -> 1.25;
            default      -> 1.0;
        };
    }

    /** Size multiplier for a specific order item. */
    private double getSizeMultiplierFor(OrderItem o) {
        return switch (o.size) {
            case "Small" -> 0.75;
            case "Large" -> 1.25;
            default      -> 1.0;
        };
    }

    private void refreshMaxLabel() {
        CoffeeType ct = (CoffeeType) coffeeCombo.getSelectedItem();
        if (ct == null) { lblMaxProducible.setText("Select a flavor"); return; }
        int max = im.maxProducible(ct, getSizeMultiplier());
        if (max <= 0) {
            lblMaxProducible.setForeground(RED_ERR);
            lblMaxProducible.setText("OUT OF STOCK");
        } else {
            lblMaxProducible.setForeground(GREEN);
            lblMaxProducible.setText("Max producible: " + max + " cups");
        }
    }

    private void onStep1Next() {
        selectedCoffee = (CoffeeType) coffeeCombo.getSelectedItem();
        quantity       = (int) qtySpinner.getValue();

        if (selectedCoffee == null) {
            JOptionPane.showMessageDialog(this, "Please select a coffee type.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Build one independent OrderItem per cup ──────────────────────────────
        orders.clear();
        for (int i = 0; i < quantity; i++) {
            OrderItem item = new OrderItem(selectedCoffee);
            item.size         = selectedSize;
            item.temp         = selectedTemp;
            item.isCustomized = isCustomized;
            orders.add(item);
        }

        editingOrderIndex = 0;
        rebuildStep2();
        goToStep(1);
    }

    // ═══ STEP 2 — INGREDIENTS / CUSTOMIZE (per-order) ════════════════════════════
    private JPanel buildStep2() {
        step2Root = new JPanel(new BorderLayout(0, 16));
        step2Root.setOpaque(false);
        return step2Root;
    }

    private void rebuildStep2() {
        step2Root.removeAll();

        OrderItem current = orders.get(editingOrderIndex);

        // ── Title ─────────────────────────────────────────────────────────────────
        step2Root.add(
            stepHeading("Step 2 — " + (current.isCustomized ? "Customize Recipe" : "Review Ingredients")),
            BorderLayout.NORTH);

        // ── Coffee selector row (only shown when qty > 1) ─────────────────────────
        JPanel centerWrap = new JPanel();
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));
        centerWrap.setOpaque(false);

        if (orders.size() > 1) {
            JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            selectorRow.setOpaque(false);
            selectorRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

            JLabel selLbl = new JLabel("Editing:");
            selLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            selLbl.setForeground(TEXT_DARK);

            // Dropdown listing "Coffee 1 — Espresso [Medium, Hot]", etc.
            String[] labels = new String[orders.size()];
            for (int i = 0; i < orders.size(); i++) labels[i] = orders.get(i).label(i);
            JComboBox<String> orderSelector = new JComboBox<>(labels);
            orderSelector.setSelectedIndex(editingOrderIndex);
            orderSelector.setFont(new Font("SansSerif", Font.PLAIN, 13));
            orderSelector.setPreferredSize(new Dimension(320, 32));
            orderSelector.addActionListener(e -> {
                editingOrderIndex = orderSelector.getSelectedIndex();
                rebuildStep2();
            });

            selectorRow.add(selLbl);
            selectorRow.add(orderSelector);
            centerWrap.add(selectorRow);
            centerWrap.add(Box.createVerticalStrut(12));
        }

        // ── Ingredient table ──────────────────────────────────────────────────────
        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);

        JPanel colHdr = new JPanel(new GridLayout(1, current.isCustomized ? 4 : 3, 0, 0));
        colHdr.setOpaque(false);
        colHdr.setBorder(new EmptyBorder(0, 0, 6, 0));
        for (String h : current.isCustomized
                ? new String[]{"INGREDIENT","UNIT","AMT / CUP","ADJUST"}
                : new String[]{"INGREDIENT","UNIT","AMT / CUP"}) {
            JLabel hl = new JLabel(h);
            hl.setFont(new Font("SansSerif", Font.BOLD, 11));
            hl.setForeground(TEXT_LIGHT);
            colHdr.add(hl);
        }
        tableWrap.add(colHdr, BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        double mult = getSizeMultiplierFor(current);

        for (Map.Entry<Ingredient, Double> entry : new ArrayList<>(current.recipe.entrySet())) {
            Ingredient ing    = entry.getKey();
            double     amt    = entry.getValue();
            double     needed = amt * mult;
            double     stock  = im.getStock(ing);
            boolean    ok     = stock >= needed;

            JPanel row = new JPanel(new GridLayout(1, current.isCustomized ? 4 : 3, 0, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 228, 218)));

            JLabel lName = new JLabel(ing.getDisplayName());
            lName.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lName.setForeground(TEXT_DARK);

            JLabel lUnit = new JLabel(ing.getUnit());
            lUnit.setFont(new Font("Monospaced", Font.PLAIN, 13));
            lUnit.setForeground(TEXT_LIGHT);

            JLabel lAmt = new JLabel(String.format("%.2f  (need %.2f)", amt * mult, needed));
            lAmt.setFont(new Font("Monospaced", Font.PLAIN, 13));
            lAmt.setForeground(ok ? TEXT_DARK : RED_ERR);

            row.add(lName); row.add(lUnit); row.add(lAmt);

            if (current.isCustomized) {
                JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                ctrl.setOpaque(false);

                JButton btnMinus = microBtn("−");
                JButton btnPlus  = microBtn("+");
                JButton btnDel   = microBtn("✕");
                btnDel.setForeground(RED_ERR);

                // Capture index so lambdas always refer to THIS order
                final int idx = editingOrderIndex;

                btnMinus.addActionListener(e -> {
                    Map<Ingredient, Double> r = orders.get(idx).recipe;
                    double cur  = r.getOrDefault(ing, 0.0);
                    double next = Math.max(0, cur - 1);
                    if (next == 0) r.remove(ing); else r.put(ing, next);
                    rebuildStep2();
                });
                btnPlus.addActionListener(e -> {
                    orders.get(idx).recipe.merge(ing, 1.0, Double::sum);
                    rebuildStep2();
                });
                btnDel.addActionListener(e -> {
                    orders.get(idx).recipe.remove(ing);
                    rebuildStep2();
                });

                ctrl.add(btnMinus); ctrl.add(btnPlus); ctrl.add(btnDel);
                row.add(ctrl);
            }

            rows.add(row);
            rows.add(Box.createVerticalStrut(2));
        }

        // Add-ingredient row (customize mode only)
        if (current.isCustomized) {
            rows.add(Box.createVerticalStrut(10));
            JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            addRow.setOpaque(false);
            addRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

            JComboBox<Ingredient> addCombo = new JComboBox<>();
            for (Ingredient ing : im.getIngredients())
                if (!current.recipe.containsKey(ing)) addCombo.addItem(ing);
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

            final int idx = editingOrderIndex;
            btnAddIng.addActionListener(e -> {
                Ingredient sel = (Ingredient) addCombo.getSelectedItem();
                if (sel != null) {
                    orders.get(idx).recipe.put(sel, (Double) addAmt.getValue());
                    rebuildStep2();
                }
            });

            JLabel addLbl = new JLabel("Add ingredient:");
            addLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            addLbl.setForeground(TEXT_LIGHT);
            addRow.add(addLbl); addRow.add(addCombo);
            addRow.add(addAmt); addRow.add(btnAddIng);
            rows.add(addRow);
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        tableWrap.add(scroll, BorderLayout.CENTER);
        centerWrap.add(tableWrap);
        step2Root.add(centerWrap, BorderLayout.CENTER);

        // ── Nav buttons ───────────────────────────────────────────────────────────
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
        // Validate ALL orders together
        StringBuilder err = new StringBuilder();

        // Aggregate total ingredient needs across all orders
        Map<Ingredient, Double> totalNeeded = new LinkedHashMap<>();
        for (OrderItem o : orders) {
            double m = getSizeMultiplierFor(o);
            for (Map.Entry<Ingredient, Double> e : o.recipe.entrySet()) {
                totalNeeded.merge(e.getKey(), e.getValue() * m, Double::sum);
            }
        }

        for (Map.Entry<Ingredient, Double> e : totalNeeded.entrySet()) {
            double avail = im.getStock(e.getKey());
            if (avail < e.getValue()) {
                err.append(String.format("• %s: need %.2f %s, have %.2f\n",
                        e.getKey().getDisplayName(),
                        e.getValue(), e.getKey().getUnit(), avail));
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

    // ═══ STEP 3 — CONFIRM PRODUCTION ═════════════════════════════════════════════
    private JPanel buildStep3() {
        step3Root = new JPanel(new BorderLayout(0, 16));
        step3Root.setOpaque(false);
        return step3Root;
    }

    private void rebuildStep3() {
        step3Root.removeAll();

        step3Root.add(stepHeading("Step 3 — Confirm Production"), BorderLayout.NORTH);

        JPanel summary = new JPanel(new BorderLayout(0, 10));
        summary.setOpaque(false);

        // Top stat cards
        long customCount = orders.stream().filter(o -> o.isCustomized).count();
        JPanel orderHdr = new JPanel(new GridLayout(1, 3, 20, 0));
        orderHdr.setOpaque(false);
        orderHdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        orderHdr.add(summaryStatCard("COFFEE",   selectedCoffee.getDisplayName(), ACCENT));
        orderHdr.add(summaryStatCard("QUANTITY", orders.size() + " cups",          new Color(80,120,80)));
        orderHdr.add(summaryStatCard("CUSTOM",   customCount + " / " + orders.size() + " customized",
                                                 new Color(80,100,160)));
        summary.add(orderHdr, BorderLayout.NORTH);

        // Per-order breakdown
        JPanel breakdown = new JPanel();
        breakdown.setLayout(new BoxLayout(breakdown, BoxLayout.Y_AXIS));
        breakdown.setOpaque(false);
        breakdown.setBorder(new EmptyBorder(10, 0, 0, 0));

        for (int i = 0; i < orders.size(); i++) {
            OrderItem o = orders.get(i);
            double    m = getSizeMultiplierFor(o);

            // Order header label
            JLabel orderLbl = new JLabel(
                "Coffee " + (i+1) + "  —  " + o.coffeeType.getDisplayName()
                + "  [" + o.size + ", " + o.temp + (o.isCustomized ? ", Custom" : "") + "]");
            orderLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            orderLbl.setForeground(ACCENT);
            orderLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            breakdown.add(orderLbl);
            breakdown.add(Box.createVerticalStrut(4));

            for (Map.Entry<Ingredient, Double> e : o.recipe.entrySet()) {
                Ingredient ing    = e.getKey();
                double     perCup = e.getValue();
                double     total  = perCup * m;
                double     stock  = im.getStock(ing);

                JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
                row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(235,228,218)));

                addSummaryCell(row, "  " + ing.getDisplayName(),                   Font.PLAIN, TEXT_DARK);
                addSummaryCell(row, String.format("%.2f/cup", perCup),             Font.PLAIN, TEXT_LIGHT);
                addSummaryCell(row, String.format("Total: %.2f %s", total, ing.getUnit()), Font.BOLD, ACCENT);
                addSummaryCell(row, String.format("Stock: %.2f", stock),           Font.PLAIN, stock >= total ? GREEN : RED_ERR);
                breakdown.add(row);
                breakdown.add(Box.createVerticalStrut(2));
            }
            breakdown.add(Box.createVerticalStrut(10));
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
        btnCancel.setBackground(new Color(245,245,240));
        btnCancel.setForeground(RED_ERR);
        btnCancel.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnCancel.setFocusable(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(200,180,170)));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e    -> goToStep(1));
        btnConfirm.addActionListener(e -> doConfirmProduction());
        btnCancel.addActionListener(e  -> goToStep(0));

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnCancel);
        rightBtns.add(btnConfirm);

        btnRow.add(btnBack,   BorderLayout.WEST);
        btnRow.add(rightBtns, BorderLayout.EAST);
        step3Root.add(btnRow, BorderLayout.SOUTH);

        step3Root.revalidate();
        step3Root.repaint();
    }

    private void doConfirmProduction() {
        // Produce each order item individually
        for (OrderItem o : orders) {
            CoffeeType synth = new CoffeeType(
                o.coffeeType.getId(),
                o.coffeeType.getDisplayName() + (o.isCustomized ? " (Custom)" : ""),
                Collections.unmodifiableMap(new LinkedHashMap<>(o.recipe))
            );
            im.produce(synth, getSizeMultiplierFor(o), 1);
        }

        JOptionPane.showMessageDialog(this,
            "Successfully produced " + orders.size() + "× " + selectedCoffee.getDisplayName(),
            "Production Complete",
            JOptionPane.INFORMATION_MESSAGE);

        orders.clear();
        goToStep(0);
    }

    // ═══ UI HELPERS ══════════════════════════════════════════════════════════════
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