import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import model.InventoryManager;
import panel.customization.CustomizationPanel;
import panel.dashboard.DashboardPanel;
import panel.logs.LogsPanel;
import panel.inventory.InventoryPanel;
import panel.production.ProductionPanel;
import panel.refill.RefillPanel;
import panel.ingredients.IngredientsPanel;


public class ConsoleUI {

    // ─── Theme colours ────────────────────────────────────────────────────────
    private static final Color BG         = new Color(250, 245, 230);
    private static final Color SIDEBAR_BG = new Color(245, 238, 215);
    private static final Color GOLD       = new Color(180, 140, 50);
    private static final Color GOLD_DARK  = new Color(140, 100, 30);
    private static final Color GOLD_LIGHT = new Color(220, 200, 140);
    private static final Color BTN_BG     = new Color(255, 252, 240);
    private static final Color BTN_ACTIVE = new Color(160, 120, 50);
    private static final Color TEXT_DARK  = new Color(60, 45, 20);
    private static final Color TEXT_LIGHT = new Color(170, 150, 100);
    private static final Color GREEN_DOT  = new Color(60, 160, 60);

    private JPanel           mainPanel;
    private CardLayout       cardLayout;
    private InventoryManager inventoryManager;

    // ─── Panel refs for refresh() calls ──────────────────────────────────────
    private DashboardPanel   dashboardPanel;
    private RefillPanel      refillPanel;
    private InventoryPanel   inventoryPanel;
    private IngredientsPanel ingredientsPanel;   
    private CustomizationPanel customizationPanel;

    // ─── Active nav button tracking ──────────────────────────────────────────
    private JButton activeBtn  = null;
    private JLabel  activeIcon = null;
    private JLabel  activeText = null;

    private static final String[][] NAV_ITEMS = {
        {"DASHBOARD",      "⊞"},
        {"PRODUCTION",     "☕"},
        {"INVENTORY",      "⊡"},
        {"REFILLS",        "↺"},
        {"VARIANCE AUDIT", "≈"},
        {"REPORTS / LOGS", "≡"},
        {"CUSTOMIZATION",  "⚙"},
        {"INGREDIENTS",    "⚗"},
    };

    public ConsoleUI(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═════════════════════════════════════════════════════════════════════════
    public JPanel createSideBar() {
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setPreferredSize(new Dimension(280, 0));
        sideBar.setBackground(SIDEBAR_BG);
        sideBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, GOLD_LIGHT));

        sideBar.add(buildBrandPanel(), BorderLayout.NORTH);
        sideBar.add(buildNavPanel(),   BorderLayout.CENTER);
        sideBar.add(buildExitButton(), BorderLayout.SOUTH);

        return sideBar;
    }

    // ─── Brand ────────────────────────────────────────────────────────────────
    private JPanel buildBrandPanel() {
        JPanel brand = new JPanel(new BorderLayout(0, 6));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(22, 16, 14, 16));

        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        nameRow.setOpaque(false);

        JLabel cupIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(3, 4, 18, 14, 4, 4);
                g2.drawArc(18, 8, 8, 7, -90, 180);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawArc(7, 0, 4, 4, 0, 180);
                g2.drawArc(13, 0, 4, 4, 0, 180);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(28, 22); }
        };

        JLabel logoLabel = new JLabel("VAULTREX");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setForeground(GOLD_DARK);

        nameRow.add(cupIcon);
        nameRow.add(logoLabel);

        JPanel statusPill = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 245, 225));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        statusPill.setOpaque(false);
        statusPill.setBorder(new EmptyBorder(3, 8, 3, 10));
        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 9));
        dot.setForeground(GREEN_DOT);
        JLabel statusTxt = new JLabel("SYSTEM ONLINE");
        statusTxt.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusTxt.setForeground(new Color(40, 120, 40));
        statusPill.add(dot);
        statusPill.add(statusTxt);

        JPanel underline = new JPanel();
        underline.setBackground(GOLD_LIGHT);
        underline.setPreferredSize(new Dimension(0, 1));

        brand.add(nameRow,    BorderLayout.NORTH);
        brand.add(statusPill, BorderLayout.CENTER);
        brand.add(underline,  BorderLayout.SOUTH);
        return brand;
    }

    // ─── Nav panel ────────────────────────────────────────────────────────────
    private JPanel buildNavPanel() {
        JPanel nav = new JPanel(new GridLayout(NAV_ITEMS.length, 1, 0, 6));
        nav.setOpaque(false);
        nav.setBorder(new EmptyBorder(12, 12, 12, 12));
        for (String[] item : NAV_ITEMS) {
            nav.add(buildNavButton(item[0], item[1]));
        }
        return nav;
    }

    private JButton buildNavButton(String label, String icon) {
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        iconLbl.setForeground(GOLD);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        textLbl.setForeground(TEXT_DARK);

        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = (this == activeBtn);
                g2.setColor(isActive ? BTN_ACTIVE : BTN_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!isActive) {
                    g2.setColor(GOLD_LIGHT);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.add(iconLbl);
        btn.add(textLbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) {
                    iconLbl.setForeground(GOLD_DARK);
                    textLbl.setForeground(GOLD_DARK);
                    btn.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) {
                    iconLbl.setForeground(GOLD);
                    textLbl.setForeground(TEXT_DARK);
                    btn.repaint();
                }
            }
        });

        btn.addActionListener(e -> {
            if (activeBtn != null && activeBtn != btn) {
                activeIcon.setForeground(GOLD);
                activeText.setForeground(TEXT_DARK);
                activeBtn.repaint();
            }
            activeBtn  = btn;
            activeIcon = iconLbl;
            activeText = textLbl;
            iconLbl.setForeground(Color.WHITE);
            textLbl.setForeground(Color.WHITE);
            btn.repaint();

            // Refresh panels that track live data
            if (label.equals("DASHBOARD"))   dashboardPanel.refresh();
            if (label.equals("REFILLS"))     refillPanel.refresh();
            if (label.equals("INVENTORY"))   inventoryPanel.refresh();
            if (label.equals("INGREDIENTS") && ingredientsPanel != null) ingredientsPanel.refresh();
if (label.equals("CUSTOMIZATION") && customizationPanel != null) customizationPanel.refresh();
            cardLayout.show(mainPanel, label);
        });

        // Build panel — try/catch surfaces any constructor crash visibly
        // instead of leaving a silent blank white panel
        JPanel panel;
        try {
            panel = switch (label) {
                case "DASHBOARD"      -> { dashboardPanel   = new DashboardPanel(inventoryManager);   yield dashboardPanel;   }
                case "PRODUCTION"     -> new ProductionPanel(inventoryManager);
                case "REFILLS"        -> { refillPanel      = new RefillPanel(inventoryManager);      yield refillPanel;      }
                case "INVENTORY"      -> { inventoryPanel   = new InventoryPanel(inventoryManager);   yield inventoryPanel;   }
                case "INGREDIENTS"    -> { ingredientsPanel = new IngredientsPanel(inventoryManager); yield ingredientsPanel; }
                case "REPORTS / LOGS" -> new LogsPanel(inventoryManager);
                case "CUSTOMIZATION" -> { customizationPanel = new CustomizationPanel(inventoryManager); yield customizationPanel; }
                default               -> createPlaceholder(label);
            };
        } catch (Exception ex) {
            ex.printStackTrace();
            panel = createErrorCard(label, ex.getMessage());
        }

        mainPanel.add(panel, label);
        return btn;
    }

    // ─── Exit button ──────────────────────────────────────────────────────────
    private JPanel buildExitButton() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(4, 12, 16, 12));

        JPanel line = new JPanel();
        line.setBackground(GOLD_LIGHT);
        line.setPreferredSize(new Dimension(0, 1));
        wrap.add(line, BorderLayout.NORTH);

        JButton exit = new JButton("⏻  EXIT") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(200, 60, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        exit.setOpaque(false);
        exit.setContentAreaFilled(false);
        exit.setBorderPainted(false);
        exit.setFocusable(false);
        exit.setFont(new Font("SansSerif", Font.BOLD, 13));
        exit.setForeground(Color.WHITE);
        exit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exit.setPreferredSize(new Dimension(0, 38));
        exit.addActionListener(e -> System.exit(0));

        JPanel btnWrap = new JPanel(new BorderLayout());
        btnWrap.setOpaque(false);
        btnWrap.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnWrap.add(exit, BorderLayout.CENTER);
        wrap.add(btnWrap, BorderLayout.CENTER);
        return wrap;
    }

    // ─── Placeholder ──────────────────────────────────────────────────────────
    private JPanel createPlaceholder(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl.setForeground(GOLD_DARK);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Coming soon");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_LIGHT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(lbl);
        center.add(Box.createVerticalStrut(6));
        center.add(sub);
        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    // ─── Error card — shown if a panel crashes during construction ────────────
    private JPanel createErrorCard(String label, String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel title = new JLabel("⚠  Failed to load: " + label);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(180, 50, 50));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detail = new JLabel("<html><center>"
            + (message != null ? message : "Unknown error — check stacktrace")
            + "<br><br>See the terminal / debug console for details.</center></html>");
        detail.setFont(new Font("Monospaced", Font.PLAIN, 11));
        detail.setForeground(TEXT_LIGHT);
        detail.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(title);
        center.add(Box.createVerticalStrut(12));
        center.add(detail);
        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    public JPanel getMainPanel() { return mainPanel; }
}