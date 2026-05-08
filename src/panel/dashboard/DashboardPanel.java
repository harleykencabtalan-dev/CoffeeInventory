package panel.dashboard;

import model.CoffeeType;
import model.Ingredient;
import model.InventoryManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    // ─── Colours ──────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(250, 245, 230);
    private static final Color CARD_BG     = new Color(255, 252, 240);
    private static final Color CARD_BORDER = new Color(200, 175, 120);
    private static final Color GOLD        = new Color(180, 140, 50);
    private static final Color GOLD_DARK   = new Color(140, 100, 30);
    private static final Color HEADER_BG   = new Color(160, 120, 50);
    private static final Color ROW_ALT     = new Color(245, 238, 218);
    private static final Color GRID_LINE   = new Color(210, 190, 145);   // visible grid separator
    private static final Color TEXT_DARK   = new Color(60, 45, 20);
    private static final Color TEXT_MID    = new Color(120, 100, 60);
    private static final Color TEXT_LIGHT  = new Color(170, 150, 100);
    private static final Color GREEN       = new Color(60, 160, 60);
    private static final Color ORANGE      = new Color(210, 120, 30);
    private static final Color RED         = new Color(200, 50, 50);
    private static final Color BAR_GREEN   = new Color(80, 180, 80);
    private static final Color BAR_ORANGE  = new Color(220, 140, 40);
    private static final Color BAR_RED     = new Color(210, 60, 60);

    private final DashboardController controller;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public DashboardPanel(InventoryManager inventoryManager) {
        this.controller = new DashboardController(inventoryManager);
        setLayout(new BorderLayout(12, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));
        build();
    }

    public void refresh() {
        removeAll();
        build();
        revalidate();
        repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BUILD
    // ═════════════════════════════════════════════════════════════════════════
    private void build() {
        // TOP ROW — inventory + alerts
        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 0));
        topRow.setOpaque(false);
        topRow.add(buildInventoryLevelsCard());
        topRow.add(buildActiveAlertsCard());

        // BOTTOM ROW — 3 cards
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(buildProductionCapabilityCard());
        bottomRow.add(buildVarianceAuditCard());
        bottomRow.add(buildRecentActivitiesCard());

        // ── Use JSplitPane so the user gets ~40 % top / ~60 % bottom, ─────────
        // and both rows actually resize with the window instead of SOUTH
        // collapsing to its preferred height.
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topRow, bottomRow);
        split.setResizeWeight(0.42);          // top gets ~42 % of space
        split.setDividerSize(8);
        split.setDividerLocation(0.42);
        split.setContinuousLayout(true);
        split.setOpaque(false);
        split.setBorder(null);

        // Style the divider to match the theme
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) {
                        g.setColor(BG);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });

        add(split, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TOP-LEFT: INVENTORY LEVELS
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildInventoryLevelsCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(makeCardHeader("NAME", new String[]{"INVENTORY LEVELS", "STATUS"}), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(4, 0, 4, 0));

        List<Ingredient> ings = controller.getIngredients();
        for (int i = 0; i < ings.size(); i++) {
            rows.add(buildInventoryRow(ings.get(i), i % 2 == 0));
            if (i < ings.size() - 1) rows.add(gridLine());   // line between every row
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInventoryRow(Ingredient ing, boolean alt) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(alt ? CARD_BG : ROW_ALT);
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        String status    = controller.getStockStatus(ing);
        double ratio     = controller.getStockRatio(ing);
        Color  barColor  = switch (status) {
            case "CRITICAL"     -> BAR_ORANGE;
            case "OUT OF STOCK" -> BAR_RED;
            default             -> BAR_GREEN;
        };
        Color statusColor = switch (status) {
            case "CRITICAL"     -> ORANGE;
            case "OUT OF STOCK" -> RED;
            default             -> GREEN;
        };

        JLabel nameLbl = new JLabel(ing.getDisplayName().toUpperCase());
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        nameLbl.setForeground(TEXT_MID);
        nameLbl.setPreferredSize(new Dimension(100, 20));

        final double fRatio = ratio;
        final Color  fBar   = barColor;
        JPanel barBg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 210, 185));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int fillW = (int)(getWidth() * fRatio);
                if (fillW > 0) { g2.setColor(fBar); g2.fillRoundRect(0, 0, fillW, getHeight(), 6, 6); }
                g2.dispose();
            }
        };
        barBg.setOpaque(false);
        barBg.setPreferredSize(new Dimension(0, 14));

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusLbl.setForeground(statusColor);
        statusLbl.setPreferredSize(new Dimension(85, 20));
        statusLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel barWrap = new JPanel(new BorderLayout(4, 0));
        barWrap.setOpaque(false);
        barWrap.add(barBg,     BorderLayout.CENTER);
        barWrap.add(statusLbl, BorderLayout.EAST);

        row.add(nameLbl, BorderLayout.WEST);
        row.add(barWrap, BorderLayout.CENTER);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  TOP-RIGHT: ACTIVE ALERTS
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildActiveAlertsCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(makeCardHeader("INGREDIENT", new String[]{"STATUS"}), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        List<String> alerts = controller.getAlerts();
        if (alerts.isEmpty()) {
            JLabel ok = new JLabel("✓  All stock levels nominal");
            ok.setFont(new Font("SansSerif", Font.ITALIC, 12));
            ok.setForeground(GREEN);
            ok.setBorder(new EmptyBorder(12, 10, 12, 10));
            rows.add(ok);
        } else {
            for (int i = 0; i < alerts.size(); i++) {
                rows.add(buildAlertRow(alerts.get(i), i % 2 == 0));
                if (i < alerts.size() - 1) rows.add(gridLine());
            }
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAlertRow(String alert, boolean alt) {
        String ingName = "INGREDIENT";
        String status  = "CRITICAL";

        if (alert.contains("LOW STOCK:")) {
            String after = alert.replace("LOW STOCK:", "").trim();
            int paren = after.indexOf("(");
            if (paren > 0) ingName = after.substring(0, paren).trim().toUpperCase();
        }

        final String fin = ingName;
        boolean outOfStock = controller.getIngredients().stream()
            .filter(i -> i.getDisplayName().equalsIgnoreCase(fin))
            .anyMatch(i -> controller.getStock(i) <= 0);
        if (outOfStock) status = "OUT OF STOCK";

        Color statusColor = status.equals("OUT OF STOCK") ? RED : ORANGE;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(alt ? CARD_BG : ROW_ALT);
        row.setBorder(new EmptyBorder(8, 12, 8, 12));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel nameLbl = new JLabel(ingName);
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        nameLbl.setForeground(TEXT_MID);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusLbl.setForeground(statusColor);

        row.add(nameLbl,   BorderLayout.WEST);
        row.add(statusLbl, BorderLayout.EAST);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BOTTOM-LEFT: PRODUCTION CAPABILITY MAP
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildProductionCapabilityCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 0));
        card.add(sectionTitle("PRODUCTION CAPABILITY MAP"), BorderLayout.NORTH);

        // Sub-header
        JPanel subHeader = new JPanel(new GridLayout(1, 4, 0, 0));
        subHeader.setBackground(new Color(230, 215, 180));
        subHeader.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, GRID_LINE),   // bottom border under header
            new EmptyBorder(6, 10, 6, 10)));
        for (String h : new String[]{"COFFEE NAME", "SIZE", "HOT", "ICED"}) {
            JLabel l = new JLabel(h);
            l.setFont(new Font("SansSerif", Font.BOLD, 10));
            l.setForeground(GOLD_DARK);
            subHeader.add(l);
        }

        // Data rows
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        String[] sizes = {"SMALL", "MEDIUM", "LARGE"};
        double[] mults = {0.75, 1.0, 1.25};
        boolean alt = false;

        List<CoffeeType> coffeeTypes = controller.getCoffeeTypes();
        for (int ci = 0; ci < coffeeTypes.size(); ci++) {
            CoffeeType ct = coffeeTypes.get(ci);
            boolean firstRow = true;
            for (int s = 0; s < sizes.length; s++) {
                int hot  = controller.maxProducible(ct, mults[s]);
                int iced = Math.max(0, hot - 1);

                JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
                row.setBackground(alt ? CARD_BG : ROW_ALT);
                row.setBorder(new EmptyBorder(7, 10, 7, 10));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JLabel nameLbl = new JLabel(firstRow ? ct.getDisplayName().toUpperCase() : "");
                nameLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
                nameLbl.setForeground(GOLD_DARK);

                JLabel sizeLbl = new JLabel(sizes[s]);
                sizeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                sizeLbl.setForeground(TEXT_MID);

                JLabel hotLbl  = new JLabel(String.valueOf(hot));
                hotLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                hotLbl.setForeground(TEXT_DARK);

                JLabel icedLbl = new JLabel(String.valueOf(iced));
                icedLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
                icedLbl.setForeground(TEXT_DARK);

                row.add(nameLbl); row.add(sizeLbl);
                row.add(hotLbl);  row.add(icedLbl);
                rows.add(row);

                // Draw a grid line after every row, thicker after each coffee's last size
                boolean lastSizeOfCoffee = (s == sizes.length - 1);
                boolean lastCoffee       = (ci == coffeeTypes.size() - 1);
                if (!(lastSizeOfCoffee && lastCoffee)) {
                    rows.add(lastSizeOfCoffee ? thickGridLine() : gridLine());
                }

                firstRow = false;
                alt = !alt;
            }
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.add(subHeader, BorderLayout.NORTH);
        inner.add(scroll,    BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BOTTOM-MIDDLE: LATEST VARIANCE AUDIT
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildVarianceAuditCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout(0, 0));
        card.add(sectionTitle("LATEST VARIANCE AUDIT"), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        List<String> logs = controller.getManager().getVarianceLogs();

        if (logs.isEmpty()) {
            JLabel icon = new JLabel("📋");
            icon.setFont(new Font("SansSerif", Font.PLAIN, 28));
            icon.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel heading = new JLabel("No audits yet");
            heading.setFont(new Font("SansSerif", Font.BOLD, 13));
            heading.setForeground(TEXT_DARK);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel sub = new JLabel("<html>Run a variance audit from the<br>"
                + "<b>Variance Audit</b> panel to see<br>"
                + "discrepancy summaries here.</html>");
            sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
            sub.setForeground(TEXT_LIGHT);
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(icon);
            content.add(Box.createVerticalStrut(8));
            content.add(heading);
            content.add(Box.createVerticalStrut(6));
            content.add(sub);
        } else {
            long discrepancies = logs.stream()
                .filter(l -> l.contains("[ADJUST]") || l.contains("[SET]")).count();

            JLabel summaryLbl = new JLabel(discrepancies + " discrepanc"
                + (discrepancies == 1 ? "y" : "ies") + " found in " + logs.size() + " items");
            summaryLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            summaryLbl.setForeground(TEXT_DARK);
            summaryLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            String latest = logs.get(logs.size() - 1);
            String ts = latest.contains("|") ? latest.split("\\|")[0].trim() : "";

            JLabel tsLbl = new JLabel(ts.isEmpty() ? "See Reports/Logs for details" : ts);
            tsLbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            tsLbl.setForeground(TEXT_LIGHT);
            tsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(summaryLbl);
            content.add(Box.createVerticalStrut(6));
            content.add(tsLbl);
            content.add(Box.createVerticalStrut(12));

            int start = Math.max(0, logs.size() - 3);
            for (int i = start; i < logs.size(); i++) {
                JLabel entry = new JLabel("<html>" + logs.get(i).replace("|", "<br>") + "</html>");
                entry.setFont(new Font("Monospaced", Font.PLAIN, 10));
                entry.setForeground(TEXT_MID);
                entry.setAlignmentX(Component.LEFT_ALIGNMENT);
                entry.setBorder(new EmptyBorder(4, 0, 4, 0));
                content.add(entry);
                content.add(gridLine());
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BOTTOM-RIGHT: RECENT ACTIVITIES
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildRecentActivitiesCard() {
        JPanel card = makeCard();
        card.setLayout(new BorderLayout());
        card.add(sectionTitle("RECENT ACTIVITIES"), BorderLayout.NORTH);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(6, 10, 6, 10));

        List<String> activities = controller.getRecentActivities(12);

        if (activities.isEmpty()) {
            JLabel empty = new JLabel("No recent activity.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 12));
            empty.setForeground(TEXT_LIGHT);
            rows.add(empty);
        } else {
            for (int i = 0; i < activities.size(); i++) {
                String   act   = activities.get(i);
                String[] parts = act.split("\\|");
                String   type  = parts.length > 0 ? parts[0] : "";
                String   desc  = parts.length > 1 ? parts[1] : act;
                String   ts    = parts.length > 2 ? parts[2] : "";

                Color dotColor = switch (type) {
                    case "PROD"     -> GREEN;
                    case "REFILL"   -> GOLD;
                    case "VARIANCE" -> ORANGE;
                    default         -> TEXT_LIGHT;
                };

                JPanel row = new JPanel(new BorderLayout(6, 0));
                row.setOpaque(false);
                row.setBorder(new EmptyBorder(5, 0, 5, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel dot = new JLabel("●");
                dot.setFont(new Font("SansSerif", Font.PLAIN, 9));
                dot.setForeground(dotColor);
                dot.setVerticalAlignment(SwingConstants.TOP);

                JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 1));
                textBlock.setOpaque(false);

                JLabel descLbl = new JLabel(desc);
                descLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
                descLbl.setForeground(TEXT_DARK);

                JLabel tsLbl = new JLabel(ts);
                tsLbl.setFont(new Font("Monospaced", Font.PLAIN, 9));
                tsLbl.setForeground(TEXT_LIGHT);

                textBlock.add(descLbl);
                textBlock.add(tsLbl);
                row.add(dot,       BorderLayout.WEST);
                row.add(textBlock, BorderLayout.CENTER);
                rows.add(row);

                // Visible divider between every activity
                if (i < activities.size() - 1) rows.add(gridLine());
            }
        }

        JScrollPane scroll = new JScrollPane(rows);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        card.add(scroll, BorderLayout.CENTER);
        return card;
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

    private JPanel makeCardHeader(String leftCol, String[] rightCols) {
        JPanel bar = new JPanel(new GridLayout(1, 1 + rightCols.length, 0, 0));
        bar.setBackground(HEADER_BG);
        bar.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel left = new JLabel(leftCol);
        left.setFont(new Font("SansSerif", Font.BOLD, 11));
        left.setForeground(Color.WHITE);
        bar.add(left);

        for (String col : rightCols) {
            JLabel lbl = new JLabel(col);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(Color.WHITE);
            bar.add(lbl);
        }
        return bar;
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel("<html><b>" + text + "</b></html>");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(GOLD_DARK);
        lbl.setBackground(new Color(235, 220, 180));
        lbl.setOpaque(true);
        lbl.setBorder(new EmptyBorder(9, 12, 9, 12));
        return lbl;
    }

    /** Thin visible separator between rows */
    private JPanel gridLine() {
        JPanel line = new JPanel();
        line.setBackground(GRID_LINE);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    /** Slightly thicker separator between coffee groups */
    private JPanel thickGridLine() {
        JPanel line = new JPanel();
        line.setBackground(CARD_BORDER);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        line.setPreferredSize(new Dimension(0, 2));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }
}