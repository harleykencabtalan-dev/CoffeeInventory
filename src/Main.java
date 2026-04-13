import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {
        setTitle("Cafe OS");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

    
        InventoryManager inventoryManager = new InventoryManager();

        
        ConsoleUI ui = new ConsoleUI(inventoryManager);

        add(ui.createSideBar(), BorderLayout.WEST);
        add(ui.getMainPanel(), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}