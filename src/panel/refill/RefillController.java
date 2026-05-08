package panel.refill;

import model.Ingredient;
import model.InventoryManager;
import model.RefillRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class RefillController {

    private final InventoryManager im;

    public RefillController(InventoryManager im) {
        this.im = im;
    }

    public InventoryManager getManager() {
        return im;
    }

    public double getCurrentStock(Ingredient ing) {
        return im.getStock(ing);
    }

    public void registerIntake(Ingredient ing, double amount) {
        im.refill(ing, amount);
    }

    public LinkedList<RefillRecord> getRefillLog() {
        return im.getRefillLog();
    }

    public ArrayList<RefillRecord> getRefillLogReversed() {
        ArrayList<RefillRecord> reversed = new ArrayList<>(im.getRefillLog());
        Collections.reverse(reversed);
        return reversed;
    }

    public java.util.List<Ingredient> getIngredients() {
        return im.getIngredients();
    }
}