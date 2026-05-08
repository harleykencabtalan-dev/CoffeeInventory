package panel.logs;

import model.InventoryManager;
import model.ProductionRecord;
import model.RefillRecord;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class LogsPanel extends JPanel {

    // ─── Colours (match app theme) ────────────────────────────────────────────
    private static final Color BG          = new Color(245, 245, 240);
    private static final Color ACCENT      = new Color(120, 90, 70);
    private static final Color GOLD        = new Color(180, 140, 40);
    private static final Color GOLD_LIGHT  = new Color(230, 210, 150);
    private static final Color GOLD_BG     = new Color(255, 248, 225);
    private static final Color TEXT_DARK   = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT  = new Color(150, 150, 150);
    private static final Color GREEN       = new Color(50, 140, 50);
    private static final Color PURPLE      = new Color(120, 80, 160);
    private static final Color CARD_BORDER = new Color(210, 180, 100);

    // ─── State ────────────────────────────────────────────────────────────────
    private final LogsController controller;
    private LogsController.Filter currentFilter = LogsController.Filter.ALL;

    // Content panels to swap
    private JPanel contentArea;
    private JPanel activeSection; // which log section is showing

    // Filter buttons so we can highlight active one
    private JButton[] filterBtns;
    private final String[] FILTER_LABELS = {"+ Today", "Yesterday", "This Week", "This Month"};
    private final LogsController.Filter[] FILTERS = {
        LogsController.Filter.TODAY,
        LogsController.Filter.YESTERDAY,
        LogsController.Filter.THIS_WEEK,
        LogsController.Filter.THIS_MONTH
    };

    // Section cards so we can track active section
    private JPanel productionCard;
    private JPanel refillCard;
    private JPanel varianceCard;
    private String currentSection = "PRODUCTION";

    // ─── Constructor ──────────────────────────────────────────────────────────
    public LogsPanel(InventoryManager im) {
        this.controller = new LogsController(im);
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("REPORTS / LOGS");
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(TEXT_DARK);

        // Gold underline bar
        JPanel underline = new JPanel();
        underline.setBackground(GOLD);
        underline.setPreferredSize(new Dimension(0, 3));

        JPanel titleWrap = new JPanel(new BorderLayout(0, 6));
        titleWrap.setOpaque(false);
        titleWrap.add(title,    BorderLayout.NORTH);
        titleWrap.add(underline, BorderLayout.SOUTH);

        header.add(titleWrap, BorderLayout.WEST);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CENTER: 2x2 section cards + log detail area
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setOpaque(false);

        // Top: 3 section selector cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 16, 16));
        cardsGrid.setOpaque(false);
        cardsGrid.setPreferredSize(new Dimension(0, 160));

        productionCard = buildSectionCard("☕", "Production Logs",
            new String[]{"Coffee type, size, quantity", "Customizations, add-ons"}, "PRODUCTION");
        refillCard = buildSectionCard("🧴", "Refill Logs",
            new String[]{"Ingredients name, amount used", "Timestamp"}, "REFILL");
        varianceCard = buildSectionCard("📊", "Variance Logs",
            new String[]{"Audit results and discrepancies"}, "VARIANCE");

        cardsGrid.add(productionCard);
        cardsGrid.add(refillCard);
        cardsGrid.add(varianceCard);

        // Bottom: dynamic log content area
        contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        showSection("PRODUCTION"); // default

        center.add(cardsGrid,   BorderLayout.NORTH);
        center.add(contentArea, BorderLayout.CENTER);
        return center;
    }

    // ─── Section selector card ────────────────────────────────────────────────
    private JPanel buildSectionCard(String icon, String title, String[] bullets, String section) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GOLD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(currentSection.equals(section) ? 2.5f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Header row: icon + title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(GOLD);
        titleRow.add(iconLabel);
        titleRow.add(titleLabel);

        // Bullet points
        JPanel bulletsPanel = new JPanel();
        bulletsPanel.setLayout(new BoxLayout(bulletsPanel, BoxLayout.Y_AXIS));
        bulletsPanel.setOpaque(false);
        for (String b : bullets) {
            JLabel bl = new JLabel("• " + b);
            bl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            bl.setForeground(TEXT_DARK);
            bulletsPanel.add(bl);
            bulletsPanel.add(Box.createVerticalStrut(3));
        }

        card.add(titleRow,     BorderLayout.NORTH);
        card.add(bulletsPanel, BorderLayout.CENTER);

        // Click to switch section
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showSection(section);
            }
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(255, 245, 200));
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.repaint();
            }
        });

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FOOTER: filter buttons
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(16, 0, 0, 0));

        filterBtns = new JButton[FILTER_LABELS.length];
        for (int i = 0; i < FILTER_LABELS.length; i++) {
            final int idx = i;
            JButton btn = makeFilterButton(FILTER_LABELS[i], i == 0);
            btn.addActionListener(e -> {
                currentFilter = FILTERS[idx];
                for (JButton b : filterBtns) styleFilterBtn(b, false);
                styleFilterBtn(btn, true);
                showSection(currentSection);
            });
            filterBtns[i] = btn;
            footer.add(btn);
        }

        return footer;
    }

    private JButton makeFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleFilterBtn(btn, active);
        return btn;
    }

    private void styleFilterBtn(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(GOLD_BG);
            btn.setForeground(GOLD);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1),
                new EmptyBorder(8, 20, 8, 20)));
        } else {
            btn.setBackground(BG);
            btn.setForeground(TEXT_DARK);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 190), 1),
                new EmptyBorder(8, 20, 8, 20)));
        }
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SECTION SWITCHING
    // ═════════════════════════════════════════════════════════════════════════
    private void showSection(String section) {
        currentSection = section;

        // Repaint cards to reflect active selection
        productionCard.repaint();
        refillCard.repaint();
        varianceCard.repaint();

        contentArea.removeAll();
        contentArea.add(buildLogTable(section), BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOG TABLE BUILDER
    // ═════════════════════════════════════════════════════════════════════════
    private JPanel buildLogTable(String section) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Column headers & rows depend on section
        String[] headers;
        JPanel rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        rowsPanel.setOpaque(false);

        switch (section) {
            case "PRODUCTION" -> {
                headers = new String[]{"COFFEE TYPE", "SIZE", "QTY", "CUSTOMIZATIONS", "TIMESTAMP"};
                List<ProductionRecord> logs = controller.getProductionLogs(currentFilter);
                if (logs.isEmpty()) {
                    rowsPanel.add(emptyLabel("No production records found."));
                } else {
                    for (ProductionRecord rec : logs) {
                        rowsPanel.add(buildRow(new String[]{
                            rec.getCoffeeName(),
                            rec.getSize(),
                            String.valueOf(rec.getQuantity()),
                            rec.getCustomizations(),
                            rec.getTimestamp().toString().substring(0, 19)
                        }, GREEN));
                        rowsPanel.add(rowDivider());
                    }
                }
            }
            case "REFILL" -> {
                headers = new String[]{"INGREDIENT", "AMOUNT", "UNIT", "TIMESTAMP"};
                List<RefillRecord> logs = controller.getRefillLogs(currentFilter);
                if (logs.isEmpty()) {
                    rowsPanel.add(emptyLabel("No refill records found."));
                } else {
                    for (RefillRecord rec : logs) {
                        rowsPanel.add(buildRow(new String[]{
                            rec.getIngredient().getDisplayName(),
                            String.format("+%.2f", rec.getAmount()),
                            rec.getIngredient().getUnit(),
                            rec.getTimestamp().toString().substring(0, 19)
                        }, GOLD));
                        rowsPanel.add(rowDivider());
                    }
                }
            }
            default -> { // VARIANCE
                headers = new String[]{"AUDIT ENTRY", "DETAILS"};
                List<String> logs = controller.getVarianceLogs(currentFilter);
                if (logs.isEmpty()) {
                    rowsPanel.add(emptyLabel("No variance records found."));
                } else {
                    for (String entry : logs) {
                        String[] parts = entry.split("\\|", 2);
                        rowsPanel.add(buildRow(parts.length > 1
                            ? new String[]{parts[0], parts[1]}
                            : new String[]{entry, "—"}, PURPLE));
                        rowsPanel.add(rowDivider());
                    }
                }
            }
        }

        // Header bar
        JPanel headerBar = buildHeaderBar(headers);

        JScrollPane scroll = new JScrollPane(rowsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Color.WHITE);

        // Wrap in card
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.add(headerBar, BorderLayout.NORTH);
        card.add(scroll,    BorderLayout.CENTER);

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildHeaderBar(String[] headers) {
        JPanel bar = new JPanel(new GridLayout(1, headers.length, 0, 0));
        bar.setBackground(GOLD_BG);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));
        for (String h : headers) {
            JLabel lbl = new JLabel(h);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            lbl.setForeground(GOLD);
            bar.add(lbl);
        }
        return bar;
    }

    private JPanel buildRow(String[] cells, Color accentColor) {
        JPanel row = new JPanel(new GridLayout(1, cells.length, 0, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(10, 16, 10, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        for (int i = 0; i < cells.length; i++) {
            JLabel lbl = new JLabel(cells[i]);
            lbl.setFont(new Font(i == cells.length - 1 ? "Monospaced" : "SansSerif", Font.PLAIN, 13));
            lbl.setForeground(i == 0 ? accentColor : TEXT_DARK);
            row.add(lbl);
        }
        return row;
    }

    private Component rowDivider() {
        JPanel div = new JPanel();
        div.setBackground(new Color(235, 228, 218));
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setPreferredSize(new Dimension(0, 1));
        return div;
    }

    private JLabel emptyLabel(String msg) {
        JLabel l = new JLabel(msg);
        l.setFont(new Font("Monospaced", Font.PLAIN, 13));
        l.setForeground(TEXT_LIGHT);
        l.setBorder(new EmptyBorder(20, 16, 20, 16));
        return l;
    }
}