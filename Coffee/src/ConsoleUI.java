import javax.swing.*;
import java.awt.*;

public class ConsoleUI {
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel buttonPanel;
    private InventoryManager inventoryManager;

    public ConsoleUI(InventoryManager inventoryManager) {
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

        // Add modules
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

        // Hover Effect
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
        if (text.equals("DASHBOARD")) {
        mainPanel.add(createDashboard(), text); // Uses the new dashboard layout
    } else {
        mainPanel.add(createPage(text), text);    // Uses the generic layout for others
    }
}
    

    private JPanel createPage(String title) {
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


    private JPanel createDashboard(){

    JPanel dashboard = new JPanel(new BorderLayout(20, 20));
    dashboard.setBackground(new Color(5, 10, 15));
    dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    JPanel overviewContainer = new JPanel(new BorderLayout(0, 10));
    overviewContainer.setOpaque(false);

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);

    JLabel titleLabel = new JLabel("SYSTEM OVERVIEW");
    titleLabel.setForeground(new Color(0, 255, 255));
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 40));

    JLabel subStatusLabel = new JLabel(">_ STATUS: NOMINAL | LAST UPDATE: 11:26:50 PM");
    subStatusLabel.setForeground(new Color(100, 150, 150));
    subStatusLabel.setFont(new Font("Monospaced", Font.PLAIN, 20));

    headerPanel.add(titleLabel, BorderLayout.NORTH);
    headerPanel.add(subStatusLabel, BorderLayout.CENTER);
    
    JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
    statsRow.setOpaque(false);
    
  

int totalIngredients = inventoryManager.getIngredients().size();
int criticalCount = inventoryManager.getLowStockList().size();


int outOfStockCount = 0;
for (Ingredient ing : inventoryManager.getIngredients()){
    if (inventoryManager.getStock(ing) <= 0) {
        outOfStockCount++;
    }
}


int productionToday = inventoryManager.getProductionLog().size();

// --- Update the UI Cards ---
statsRow.add(createStatCard("TOTAL INGREDIENTS", String.valueOf(totalIngredients), "Active tracked items", new Color(0, 255, 255)));
statsRow.add(createStatCard("CRITICAL STOCK", String.valueOf(criticalCount), "Below threshold", Color.WHITE));
statsRow.add(createStatCard("OUT OF STOCK", String.valueOf(outOfStockCount), "Depleted items", new Color(255, 50, 50)));
statsRow.add(createStatCard("TODAY'S PRODUCTION", String.valueOf(productionToday), "Completed runs", new Color(50, 255, 50)));

    overviewContainer.add(headerPanel, BorderLayout.NORTH);
    overviewContainer.add(statsRow, BorderLayout.CENTER);


    JPanel lowerContent = new JPanel(new BorderLayout(20, 0));
    lowerContent.setOpaque(false);


JPanel alertsPanel = createSectionPanel("⚠ ACTIVE ALERTS");
alertsPanel.setPreferredSize(new Dimension(0, 180));


JPanel alertsContent = new JPanel();
alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
alertsContent.setOpaque(false);





for (String alert : inventoryManager.getAlerts()) {
    JLabel alertLabel = new JLabel("<html>" + alert + "</html>");
    alertLabel.setForeground(new Color(255, 100, 100));
        alertLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
alertLabel.setText(alert.toUpperCase());
    alertLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    alertsContent.add(alertLabel);
}




JScrollPane scroll = new JScrollPane(alertsContent);
scroll.setOpaque(false);
scroll.getViewport().setOpaque(false);
scroll.setBorder(null);
alertsPanel.add(scroll, BorderLayout.CENTER);
   

JPanel capabilityMap = createSectionPanel("☕ PRODUCTION CAPABILITY MAP");
    JPanel leftColumn = new JPanel(new BorderLayout(0,20));
    leftColumn.setOpaque(false);

    JPanel grid = new JPanel(new GridLayout(2, 2, 15, 15)); 
grid.setOpaque(false);
grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


for (CoffeeType coffee : inventoryManager.getCoffeeTypes()) {
    int max = inventoryManager.maxProducible(coffee);
    String status = max > 0 ? "SUPPLY ABUNDANT" : "CANNOT PRODUCE";
    grid.add(createCoffeeCard(coffee.getDisplayName(), max + " MUGS", status));
}
capabilityMap.add(grid, BorderLayout.CENTER);

leftColumn.add(capabilityMap, BorderLayout.CENTER);
    


    leftColumn.add(alertsPanel, BorderLayout.NORTH); // Pins to top
leftColumn.add(capabilityMap, BorderLayout.CENTER);


    

    // Right Side: System Activity Log
    JPanel logPanel = createSectionPanel(">_ SYSTEM ACTIVITY LOG");
    logPanel.setPreferredSize(new Dimension(350, 0));

    lowerContent.add(leftColumn, BorderLayout.CENTER);
    lowerContent.add(logPanel, BorderLayout.EAST);

    // Assemble everything
    dashboard.add(overviewContainer, BorderLayout.NORTH);
    dashboard.add(lowerContent, BorderLayout.CENTER);

    return dashboard;
}


private JPanel createStatCard(String title, String value, String subText, Color accentColor) {
    JPanel card = new JPanel(new BorderLayout(0, 5));
    card.setBackground(new Color(15, 25, 35));
    card.setBorder(BorderFactory.createLineBorder(new Color(30, 40, 50), 1));
    card.setBorder(BorderFactory.createCompoundBorder(
        card.getBorder(), 
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));

   JLabel lblTitle = new JLabel(title);
    lblTitle.setForeground(new Color(150, 150, 150));
    lblTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
    
    JLabel lblValue = new JLabel(value);
    lblValue.setForeground(Color.WHITE);
    lblValue.setFont(new Font("SansSerif", Font.BOLD, 32));

    JLabel lblSubText = new JLabel(subText);
    lblSubText.setForeground(new Color(100, 100, 100));
    lblSubText.setFont(new Font("SansSerif", Font.PLAIN, 15));
    
   JPanel centerVal = new JPanel(new BorderLayout());
    centerVal.setOpaque(false);
    centerVal.add(lblValue, BorderLayout.CENTER);
    centerVal.add(lblSubText, BorderLayout.SOUTH);

    card.add(lblTitle, BorderLayout.NORTH);
    card.add(centerVal, BorderLayout.CENTER);
    
    return card;
}


private JPanel createSectionPanel(String title) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(10, 15, 25));
    panel.setBorder(BorderFactory.createLineBorder(new Color(25, 35, 45)));
    
    JLabel label = new JLabel(title);
    label.setForeground(new Color(0, 255, 255));
    label.setFont(new Font("SansSerif", Font.BOLD, 25));
    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    panel.add(label, BorderLayout.NORTH);
    return panel; 
}



    private JPanel createCoffeeCard(String name, String mugs, String status) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(new Color(15, 25, 35));
    card.setBorder(BorderFactory.createLineBorder(new Color(30, 40, 50)));
    card.setBorder(BorderFactory.createCompoundBorder(
        card.getBorder(), 
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    JLabel nameLabel = new JLabel(name);
    nameLabel.setForeground(Color.WHITE);
    nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

    JLabel statusLabel = new JLabel(status);
    statusLabel.setForeground(new Color(100, 150, 100)); // Dim green
    statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

    JLabel mugLabel = new JLabel(mugs);
    mugLabel.setForeground(Color.WHITE);
    mugLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    mugLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
    mugLabel.setHorizontalAlignment(SwingConstants.CENTER);

    card.add(nameLabel, BorderLayout.NORTH);
    card.add(statusLabel, BorderLayout.CENTER);
    card.add(mugLabel, BorderLayout.EAST); // Puts the "6 MUGS" box on the right

    return card;
}
}



