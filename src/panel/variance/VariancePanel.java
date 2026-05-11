package panel.variance;

import model.Ingredient;
import model.InventoryManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Variance Audit panel — button-driven input (no live typing lag).
 *
 * Each ingredient row has:
 *   [ − ]  [ value display ]  [ + ]   step = 0.5
 *   "Set…" opens a small dialog to type an exact number if needed.
 *
 * Updates are applied only on button click → zero DocumentListener overhead.
 *
 * Status logic:
 *   physical > theoretical  →  Surplus          (blue)
 *   |variance| < 10% thr    →  Normal            (green)
 *   |variance| < 30% thr    →  Minor Shortage    (orange)
 *   else                    →  Critical Shortage (red)
 */
public class VariancePanel extends JPanel {

    // ─── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(245, 245, 240);
    private static final Color ACCENT    = new Color(120, 90, 70);
    private static final Color GOLD      = new Color(180, 140, 40);
    private static final Color GOLD_DARK = new Color(140, 100, 30);
    private static final Color TEXT_DARK = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT= new Color(150, 150, 150);
    private static final Color GREEN     = new Color(50, 140, 50);
    private static final Color BLUE      = new Color(30, 100, 200);
    private static final Color ORANGE    = new Color(200, 100, 30);
    private static final Color RED       = new Color(180, 50, 50);
    private static final Color DIVIDER   = new Color(220, 210, 190);
    private static final Color ROW_ALT   = new Color(250, 247, 240);

    private static final double STEP = 0.5;

    // ─── State ────────────────────────────────────────────────────────────────
    private final VarianceController controller;

    private final Map<Ingredient, double[]> valueMap       = new LinkedHashMap<>();
    private final Map<Ingredient, JLabel>   valueLabelMap  = new LinkedHashMap<>();
    private final Map<Ingredient, JLabel>   varLabelMap    = new LinkedHashMap<>();
    private final Map<Ingredient, JLabel>   statLabelMap   = new LinkedHashMap<>();

    // ─── Footer ───────────────────────────────────────────────────────────────
    private JLabel  lblStatus;
    private JButton btnFinalize;
    private JButton btnClear;
    private JPanel  tableBody;

    // =========================================================================
    public VariancePanel(InventoryManager im) {
        this.controller = new VarianceController(im);
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFooter(),    BorderLayout.SOUTH);
    }

    public void refresh() {
        controller.reload();
        rebuildRows();
    }

    // =========================================================================
    //  HEADER
    // =========================================================================
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(0, 4));
        h.setOpaque(false);
        h.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("VARIANCE AUDIT");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Use + / − to set physical stock per ingredient, then Finalize. Use \"Set…\" to type an exact value.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_LIGHT);

        h.add(title, BorderLayout.NORTH);
        h.add(sub,   BorderLayout.SOUTH);
        return h;
    }

    // =========================================================================
    //  TABLE
    // =========================================================================
    private JPanel buildTableCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(buildColHeader(), BorderLayout.NORTH);

        tableBody = new JPanel();
        tableBody.setLayout(new BoxLayout(tableBody, BoxLayout.Y_AXIS));
        tableBody.setOpaque(false);

        rebuildRows();

        JScrollPane scroll = new JScrollPane(tableBody,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildColHeader() {
        JPanel bar = new JPanel(new GridLayout(1, 6, 0, 0));
        bar.setBackground(ACCENT);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));
        for (String c : new String[]{"Ingredient", "Theoretical", "Physical", "Adjust", "Variance", "Status"}) {
            JLabel l = new JLabel(c);
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            l.setForeground(Color.WHITE);
            bar.add(l);
        }
        return bar;
    }

    // ─── Rebuild all rows ─────────────────────────────────────────────────────
    private void rebuildRows() {
        if (tableBody == null) return;
        tableBody.removeAll();
        valueMap.clear();
        valueLabelMap.clear();
        varLabelMap.clear();
        statLabelMap.clear();

        List<Ingredient> ingredients = controller.getIngredients();
        boolean locked = controller.isLocked();
        Map<Ingredient, Double> entries = controller.getEntries();

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);

            // Default physical to theoretical so variance starts at 0
            double initial = entries.containsKey(ing)
                ? entries.get(ing)
                : ing.getTheoreticalStock();
            double[] holder = {initial};
            valueMap.put(ing, holder);

            JPanel row = new JPanel(new GridLayout(1, 6, 0, 0));
            row.setOpaque(true);
            row.setBackground(i % 2 == 0 ? Color.WHITE : ROW_ALT);
            row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, DIVIDER),
                new EmptyBorder(10, 16, 10, 16)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

            // Col 1 — name
            JLabel lblName = new JLabel(ing.getDisplayName());
            lblName.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblName.setForeground(TEXT_DARK);

            // Col 2 — theoretical
            JLabel lblTheoretical = new JLabel(
                String.format("%.2f %s", ing.getTheoreticalStock(), ing.getUnit()));
            lblTheoretical.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblTheoretical.setForeground(TEXT_LIGHT);

            // Col 3 — current physical value display
            JLabel lblValue = new JLabel(String.format("%.2f %s", holder[0], ing.getUnit()));
            lblValue.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblValue.setForeground(TEXT_DARK);
            valueLabelMap.put(ing, lblValue);

            // Col 5 — variance
            JLabel lblVar = new JLabel("—");
            lblVar.setFont(new Font("SansSerif", Font.BOLD, 13));
            varLabelMap.put(ing, lblVar);

            // Col 6 — status
            JLabel lblStat = new JLabel("—");
            lblStat.setFont(new Font("SansSerif", Font.BOLD, 13));
            statLabelMap.put(ing, lblStat);

            // Show result immediately if entry already saved
            if (entries.containsKey(ing)) refreshRowLabels(ing, holder[0]);

            // Col 4 — [ − ] [ + ] [ Set… ]
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            controls.setOpaque(false);

            JButton btnMinus = spinBtn("−");
            JButton btnPlus  = spinBtn("+");
            JButton btnSet   = setBtn("Set…");

            btnMinus.setEnabled(!locked);
            btnPlus .setEnabled(!locked);
            btnSet  .setEnabled(!locked);

            btnMinus.addActionListener(e -> {
                holder[0] = Math.max(0, holder[0] - STEP);
                commitAndRefresh(ing, holder);
            });
            btnPlus.addActionListener(e -> {
                holder[0] = holder[0] + STEP;
                commitAndRefresh(ing, holder);
            });
            btnSet.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(this,
                    "Enter exact physical stock for " + ing.getDisplayName() + ":",
                    String.format("%.2f", holder[0]));
                if (input == null) return;
                try {
                    double v = Double.parseDouble(input.trim());
                    if (v < 0) throw new NumberFormatException();
                    holder[0] = v;
                    commitAndRefresh(ing, holder);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Please enter a valid number (0 or greater).",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            });

            controls.add(btnMinus);
            controls.add(btnPlus);
            controls.add(btnSet);

            row.add(lblName);
            row.add(lblTheoretical);
            row.add(lblValue);
            row.add(controls);
            row.add(lblVar);
            row.add(lblStat);

            tableBody.add(row);
        }

        tableBody.revalidate();
        tableBody.repaint();
    }

    // ─── Commit to controller + refresh display ────────────────────────────────
    private void commitAndRefresh(Ingredient ing, double[] holder) {
        controller.setEntry(ing, holder[0]);
        JLabel lv = valueLabelMap.get(ing);
        if (lv != null) lv.setText(String.format("%.2f %s", holder[0], ing.getUnit()));
        refreshRowLabels(ing, holder[0]);
    }

    private void refreshRowLabels(Ingredient ing, double physical) {
        double variance = physical - ing.getTheoreticalStock();
        String status  = computeStatus(ing, variance);
        Color  color   = statusColor(status);
        String varText = (variance >= 0 ? "+" : "") + String.format("%.2f %s", variance, ing.getUnit());

        JLabel lv = varLabelMap.get(ing);
        JLabel ls = statLabelMap.get(ing);
        if (lv != null) { lv.setText(varText); lv.setForeground(color); }
        if (ls != null) { ls.setText(status);  ls.setForeground(color); }
    }

    // =========================================================================
    //  STATUS LOGIC
    // =========================================================================
    private String computeStatus(Ingredient ing, double variance) {
        if (variance > 0.001)           return "Surplus";
        if (Math.abs(variance) < 0.001) return "Normal";
        double threshold = ing.getLowStockThreshold();
        double pct = threshold > 0 ? Math.abs(variance) / threshold : 1.0;
        if (pct < 0.1)  return "Normal";
        if (pct < 0.3)  return "Minor Shortage";
        return "Critical Shortage";
    }

    private Color statusColor(String status) {
        return switch (status) {
            case "Surplus"           -> BLUE;
            case "Minor Shortage"    -> ORANGE;
            case "Critical Shortage" -> RED;
            default                  -> GREEN;
        };
    }

    // =========================================================================
    //  FOOTER
    // =========================================================================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(14, 0, 0, 0));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblStatus.setForeground(TEXT_LIGHT);

        btnFinalize = footerBtn("Finalize Audit", GOLD, Color.WHITE);
        btnFinalize.addActionListener(e -> onFinalize());

        btnClear = footerBtn("Clear All", new Color(200, 200, 195), TEXT_DARK);
        btnClear.addActionListener(e -> onClear());

        JButton btnHistory = footerBtn("View History", ACCENT, Color.WHITE);
        btnHistory.addActionListener(e -> showHistoryDialog());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnHistory);
        btnRow.add(btnClear);
        btnRow.add(btnFinalize);

        footer.add(lblStatus, BorderLayout.WEST);
        footer.add(btnRow,    BorderLayout.EAST);
        return footer;
    }

    // =========================================================================
    //  ACTIONS
    // =========================================================================
    private void onFinalize() {
        if (controller.isLocked()) {
            JOptionPane.showMessageDialog(this,
                "Audit already finalized. Clear entries to start a new one.",
                "Already Locked", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Finalize this audit? No further edits until cleared.",
            "Confirm Finalize", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = controller.finalizeAudit();
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Cannot Finalize", JOptionPane.WARNING_MESSAGE);
        } else {
            setLocked(true);
            JOptionPane.showMessageDialog(this, "Audit finalized and saved.",
                "Audit Locked", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onClear() {
        if (controller.isLocked()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Audit is finalized. Clearing will unlock it. Continue?",
                "Unlock Audit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            controller.unlock();
        }
        controller.clearEntries();
        setLocked(false);
        rebuildRows();
    }

    private void setLocked(boolean locked) {
        if (btnFinalize != null) btnFinalize.setEnabled(!locked);
        if (btnClear    != null) btnClear.setText(locked ? "Unlock & Clear" : "Clear All");
        if (lblStatus   != null) {
            lblStatus.setText(locked ? "🔒  Audit is finalized — read only." : " ");
            lblStatus.setForeground(locked ? RED : TEXT_LIGHT);
        }
    }

    // =========================================================================
    //  HISTORY DIALOG
    // =========================================================================
    private void showHistoryDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = owner instanceof Frame f
            ? new JDialog(f, "Audit History", true)
            : new JDialog((Dialog) owner, "Audit History", true);
        dlg.setSize(560, 420);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  Audit History");
        hdr.setFont(new Font("SansSerif", Font.BOLD, 14));
        hdr.setForeground(Color.WHITE);
        hdr.setOpaque(true);
        hdr.setBackground(ACCENT);
        hdr.setBorder(new EmptyBorder(12, 16, 12, 16));
        dlg.add(hdr, BorderLayout.NORTH);

        List<String> history = controller.loadAuditHistory();
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(BG);
        list.setBorder(new EmptyBorder(14, 18, 14, 18));

        if (history.isEmpty()) {
            JLabel empty = new JLabel("No audit history found.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 13));
            empty.setForeground(TEXT_LIGHT);
            list.add(empty);
        } else {
            for (String entry : history) {
                JLabel l = new JLabel(entry);
                l.setFont(new Font("Monospaced", Font.PLAIN, 12));
                l.setForeground(TEXT_DARK);
                l.setAlignmentX(Component.LEFT_ALIGNMENT);
                l.setBorder(new EmptyBorder(3, 0, 3, 0));
                list.add(l);
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        dlg.add(scroll, BorderLayout.CENTER);

        JButton close = footerBtn("Close", GOLD_DARK, Color.WHITE);
        close.addActionListener(e -> dlg.dispose());
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrap.setBackground(BG);
        wrap.add(close);
        dlg.add(wrap, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================
    private JButton spinBtn(String label) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setBackground(new Color(230, 220, 200));
        b.setForeground(TEXT_DARK);
        b.setFocusable(false);
        b.setPreferredSize(new Dimension(36, 32));
        b.setBorder(BorderFactory.createLineBorder(DIVIDER));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JButton setBtn(String label) {
        JButton b = new JButton(label);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        b.setBackground(new Color(215, 205, 185));
        b.setForeground(TEXT_DARK);
        b.setFocusable(false);
        b.setPreferredSize(new Dimension(48, 32));
        b.setBorder(BorderFactory.createLineBorder(DIVIDER));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(DIVIDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(0, 0, 14, 0));
        return card;
    }

    private JButton footerBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusable(false);
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }
}