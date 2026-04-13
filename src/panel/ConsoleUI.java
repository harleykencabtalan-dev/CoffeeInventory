import javax.swing.*;
import java.awt.*;

public class ConsoleUI {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private InventoryManager inventoryManager;
    
private final Color COLOR_BG      = new Color(245, 245, 240); // Off-white/Cream
    private final Color COLOR_SIDEBAR = new Color(245, 245, 240); // Soft Khaki
    private final Color COLOR_ACCENT  = new Color(120, 90, 70);   // Coffee Brown
    private final Color COLOR_BUTTON  = new Color(210, 200, 180); // Muted Sand

    public ConsoleUI(InventoryManager inventoryManager) {
       

        this.inventoryManager = inventoryManager;
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
    
        mainPanel.setBackground(new Color(245, 245, 240)); 
    }

    public JPanel createSideBar() {
        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setPreferredSize(new Dimension(250, 0));
    
        sideBar.setBackground(COLOR_SIDEBAR);
    sideBar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(245, 245, 240)));

        // BRAND PANEL
        JPanel brandPanel = new JPanel(new GridLayout(2, 1));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel logoLabel = new JLabel("☕ CAFÉ OS");
        logoLabel.setForeground(new Color(60, 60, 60)); // Dark Gray text
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel statusLabel = new JLabel("● SYSTEM ONLINE");
        statusLabel.setForeground(new Color(34, 139, 34)); // Forest Green
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
        btnTerminate.setBackground(new Color(200, 50, 50));
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
        
      
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        
        btn.setBackground(Color.WHITE); 
        btn.setForeground(new Color(70, 70, 70));
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 210)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                
                //Hover
                btn.setBackground(new Color(240, 230, 140)); 
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
               
                btn.setBackground(Color.WHITE);
            }
        });

        btn.addActionListener(e -> cardLayout.show(mainPanel, text));
        buttonPanel.add(btn);

        JPanel panel = switch (text) {
            case "DASHBOARD" -> new DashboardPanel(inventoryManager);
            //  case "PRODUCTION"  -> new ProductionPanel(inventoryManager);
            // case "INVENTORY"   -> new InventoryPanel(inventoryManager);
            // case "REFILLS"     -> new RefillPanel(inventoryManager);
            // case "SYSTEM LOGS" -> new LogsPanel(inventoryManager);
            // case "RECIPES"     -> new RecipePanel(inventoryManager);
            // case "INGREDIENTS" -> new IngredientPanel(inventoryManager);
            default          -> createPlaceholder(text);
        };

        mainPanel.add(panel, text);
    }

    private JPanel createPlaceholder(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(245, 245, 240));
        JLabel label = new JLabel(title + " CONTENT");
        label.setForeground(new Color(80, 80, 80));
        panel.add(label);
        return panel;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}