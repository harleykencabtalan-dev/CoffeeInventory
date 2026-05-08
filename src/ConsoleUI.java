import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import model.InventoryManager;
import panel.dashboard.DashboardPanel;
import panel.logs.LogsPanel;
import panel.inventory.InventoryPanel;
import panel.production.ProductionPanel;
import panel.refill.RefillPanel;

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

    // Panel refs for refresh() calls
    private DashboardPanel dashboardPanel;
    private RefillPanel    refillPanel;
    private InventoryPanel inventoryPanel;

    // Track which button is active so we can repaint it correctly
    private JButton        activeBtn   = null;
    private JLabel         activeIcon  = null;
    private JLabel         activeText  = null;

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
        sideBar.setPreferredSize(new Dimension(220, 0));
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
        

        JPanel underline = new JPanel();
        underline.setBackground(GOLD_LIGHT);
        underline.setPreferredSize(new Dimension(0, 1));

        brand.add(nameRow,    BorderLayout.NORTH);
        brand.add(statusPill, BorderLayout.CENTER);
        brand.add(underline,  BorderLayout.SOUTH);
        return brand;
    }

    // ─── Nav panel ────────────────────────────────────────────────────────────
    // Uses GridLayout so all buttons are equal height and fill the entire
    // CENTER area of the sidebar — no gaps, no centring tricks needed.
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
        // Keep icon/text labels as instance refs so we can recolour them
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

        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.add(iconLbl);
        btn.add(textLbl);

        // Hover — only when not the active button
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
            // Restore previous active button's colours
            if (activeBtn != null && activeBtn != btn) {
                activeIcon.setForeground(GOLD);
                activeText.setForeground(TEXT_DARK);
                activeBtn.repaint();
            }

            // Mark this as active
            activeBtn  = btn;
            activeIcon = iconLbl;
            activeText = textLbl;
            iconLbl.setForeground(Color.WHITE);
            textLbl.setForeground(Color.WHITE);
            btn.repaint();

            // Refresh panels that need live data
            if (label.equals("DASHBOARD"))  dashboardPanel.refresh();
            if (label.equals("REFILLS"))    refillPanel.refresh();
            if (label.equals("INVENTORY"))  inventoryPanel.refresh();

            cardLayout.show(mainPanel, label);
        });

        // Build and register the content panel
        JPanel panel = switch (label) {
            case "DASHBOARD"      -> { dashboardPanel = new DashboardPanel(inventoryManager);  yield dashboardPanel;  }
            case "PRODUCTION"     -> new ProductionPanel(inventoryManager);
            case "REFILLS"        -> { refillPanel    = new RefillPanel(inventoryManager);     yield refillPanel;     }
            case "INVENTORY"      -> { inventoryPanel = new InventoryPanel(inventoryManager);  yield inventoryPanel;  }
            case "REPORTS / LOGS" -> new LogsPanel(inventoryManager);
            default               -> createPlaceholder(label);
        };
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

    // ─── Placeholder for unbuilt panels ───────────────────────────────────────
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

      

        center.add(Box.createVerticalGlue());
       
        center.add(Box.createVerticalStrut(10));
        center.add(lbl);
        center.add(Box.createVerticalStrut(4));
     
        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    public JPanel getMainPanel() { return mainPanel; }
}