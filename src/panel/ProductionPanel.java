import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;      
import java.util.*;

public class ProductionPanel extends JPanel {
    
    private static final Color BG           = new Color(245, 245, 240);
    private static final Color ACCENT       = new Color(120, 90, 70);
    private static final Color TEXT_DARK    = new Color(60, 60, 60);
    private static final Color TEXT_LIGHT   = new Color(150, 150, 150);
    private final Color COLOR_BROWN  = new Color(120, 90, 70);

    private final InventoryManager inventoryManager;
    private CoffeeType selectedCoffee;
    private JLabel nameLbl;




    public ProductionPanel(InventoryManager inventoryManager) {
    this.inventoryManager = inventoryManager;
    
    setLayout(new BorderLayout()); // Change this to BorderLayout
    setBackground(BG);
    setBorder(new EmptyBorder(30, 30, 30, 30));

    add(buildHeader(), BorderLayout.NORTH);
    add(buildBody(), BorderLayout.CENTER); // This will now handle the cards properly
}







    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // LEFT SIDE: Main Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Production Control");
        title.setFont(new Font("SansSerif", Font.BOLD, 40));
        title.setForeground(COLOR_BROWN);

        JLabel subtitle = new JLabel("Configure, Customize, and Execute Production Orders");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitle.setForeground(TEXT_LIGHT);

        titlePanel.add(title);
        titlePanel.add(subtitle);

       
        header.add(titlePanel, BorderLayout.WEST);
        

        return header;
    }









private JPanel buildBody() {
    JPanel body = new JPanel();
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); 
    body.setOpaque(false);
    
    // Ensure the body panel itself stays at the top
    body.setAlignmentY(Component.TOP_ALIGNMENT);

    // Add Step Indicator
    JPanel indicator = buildStepIndicator();
    indicator.setAlignmentX(Component.CENTER_ALIGNMENT);
    body.add(indicator); 
   
    body.add(Box.createVerticalStrut(50)); 
    
    // Add Main Card
    JPanel mainCard = buildMainStatusCard();
    mainCard.setAlignmentX(Component.CENTER_ALIGNMENT);
    body.add(mainCard);

    // This is the "Floor" that keeps everything above it from falling
    body.add(Box.createVerticalGlue()); 

    return body;
}


private JPanel buildStepIndicator() {
    JPanel row = new JPanel(new GridLayout(1, 3, 20, 0)); 
    row.setOpaque(false);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); 

    row.add(createMiniStepCard("01", "SELECT COFFEE", true));  // Active (Brown)
    row.add(createMiniStepCard("02", "INGREDIENTS", false));  // Inactive (White)
    row.add(createMiniStepCard("03", "PRODUCTION", false));   // Inactive (White)

    return row;
}



    private JPanel buildMainStatusCard() {
        // We create a panel that draws a rounded rectangle background
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
               
                g2.setColor(Color.WHITE);

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 45, 45); 
    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 45, 45);
                
                // Draw the subtle beige border
                g2.setColor(new Color(220, 215, 205));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
            }
        };
        
        card.setOpaque(false); 
        
    card.setAlignmentY(Component.TOP_ALIGNMENT);
       card.setPreferredSize(new Dimension(0, 610)); 
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 610));
   
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        return card;
    }



    private JPanel createMiniStepCard(String num, String title, boolean active) {
    JPanel mini = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Active cards are Brown, Inactive are White
            g2.setColor(active ? ACCENT : Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            
            g2.setColor(new Color(220, 215, 205));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
        }
    };
    mini.setOpaque(false);
    
    JLabel text = new JLabel(num + " " + title);
    text.setFont(new Font("SansSerif", Font.BOLD, 12));
    text.setForeground(active ? Color.WHITE : TEXT_LIGHT);
    text.setHorizontalAlignment(JLabel.CENTER);
    mini.add(text);

mini.setPreferredSize(new Dimension(0, 60));
    return mini;
}

}