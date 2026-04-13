import javax.swing.*;
import java.awt.*;

public class Panel {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private InventoryManager inventoryManager;

    public Panel(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(5, 10, 15));
    }

    public JPanel createSideBar() {
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setPreferredSize(new Dimension(250, 0));
        sideBar.setBackground(new Color(10, 25, 30));
        sideBar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // BRAND PANEL
        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel logoLabel = new JLabel("☕ CAFÉ OS");
        logoLabel.setForeground(new Color(0, 255, 255));
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel statusLabel = new JLabel("● SYSTEM ONLINE");
        statusLabel.setForeground(new Color(50, 255, 50));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        brandPanel.add(logoLabel);
        brandPanel.add(statusLabel);

        // BUTTON CONTAINER
        buttonPanel = new JPanel(new GridLayout(8, 1, 5, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] modules = {"DASHBOARD", "PRODUCTION", "INVENTORY", "REFILLS", "SYSTEM LOGS", "RECIPES", "INGREDIENTS"};
        for (String module : modules) {
            setUpModule(module);
        }

        JButton btnTerminate = new JButton("TERMINATE");
        btnTerminate.setBackground(new Color(150, 0, 0));
        btnTerminate.setForeground(Color.WHITE);
        btnTerminate.setFocusable(false);
        btnTerminate.addActionListener(e -> System.exit(0));

        sideBar.add(brandPanel, BorderLayout.NORTH);
        sideBar.add(buttonPanel, BorderLayout.CENTER);
        sideBar.add(btnTerminate, BorderLayout.SOUTH);

        return sideBar;
    }

    private void setUpModule(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setBackground(new Color(20, 45, 50));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(new Color(30, 70, 80)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 150, 136));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(20, 45, 50));
            }
        });

        btn.addActionListener(e -> cardLayout.show(mainPanel, text));
        buttonPanel.add(btn);

        JPanel panel = switch (text) {
            case "DASHBOARD"   -> new DashboardPanel(inventoryManager);
          //  case "PRODUCTION"  -> new ProductionPanel(inventoryManager);
           // case "INVENTORY"   -> new InventoryPanel(inventoryManager);
           // case "REFILLS"     -> new RefillPanel(inventoryManager);
           // case "SYSTEM LOGS" -> new LogsPanel(inventoryManager);
           // case "RECIPES"     -> new RecipePanel(inventoryManager);
           // case "INGREDIENTS" -> new IngredientPanel(inventoryManager);
            default            -> createPlaceholder(text);
        };

        mainPanel.add(panel, text);
    }

    private JPanel createPlaceholder(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(5, 10, 15));
        JLabel label = new JLabel(title + " CONTENT");
        label.setForeground(Color.WHITE);
        panel.add(label);
        return panel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}