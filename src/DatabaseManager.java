import java.io.*;
import java.net.*;
import java.util.*;

public class DatabaseManager {

    private static final String BASE_URL = "http://localhost/cafe/";

    
// LOAD INGREDIENTS
   

    public static List<Ingredient> loadIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try {
            String response = get("get_ingredients.php");
            String[] entries = response.replace("[","").replace("]","").split("\\},\\{");
            for (String entry : entries) {
                entry = entry.replace("{","").replace("}","").trim();
                if (entry.isEmpty()) continue;
                int    id        = (int) Double.parseDouble(extractValue(entry, "id"));
                String name      = extractValue(entry, "name");
                String unit      = extractValue(entry, "unit");
                double threshold = Double.parseDouble(extractValue(entry, "low_stock_threshold"));
                double current   = Double.parseDouble(extractValue(entry, "current_stock"));
                double theo      = Double.parseDouble(extractValue(entry, "theoretical_stock"));
                list.add(new Ingredient(id, name, unit, threshold, current, theo));
            }
            System.out.println("[DB] Ingredients loaded: " + list.size());
        } catch (Exception e) {
            System.out.println("[DB] Could not load ingredients: " + e.getMessage());
        }
        return list;
    }

 
    //  LOAD COFFEE TYPES + RECIPES
  

    public static List<CoffeeType> loadCoffeeTypes(List<Ingredient> ingredients) {
        List<CoffeeType> list = new ArrayList<>();
        try {
            String response = get("get_coffee_types.php");
            String[] entries = response.replace("[","").replace("]","").split("\\},\\{");
            for (String entry : entries) {
                entry = entry.replace("{","").replace("}","").trim();
                if (entry.isEmpty()) continue;
                int    id   = (int) Double.parseDouble(extractValue(entry, "id"));
                String name = extractValue(entry, "name");

              
                Map<Ingredient, Double> recipe = loadRecipe(id, ingredients);
                list.add(new CoffeeType(id, name, recipe));
            }
            System.out.println("[DB] Coffee types loaded: " + list.size());
        } catch (Exception e) {
            System.out.println("[DB] Could not load coffee types: " + e.getMessage());
        }
        return list;
    }

    private static Map<Ingredient, Double> loadRecipe(int coffeeTypeId,
                                                       List<Ingredient> ingredients) {
        Map<Ingredient, Double> recipe = new HashMap<>();
        try {
            String response = get("get_recipe.php?coffee_type_id=" + coffeeTypeId);
            
            String[] entries = response.replace("[","").replace("]","").split("\\},\\{");
            for (String entry : entries) {
                entry = entry.replace("{","").replace("}","").trim();
                if (entry.isEmpty()) continue;
                int    ingId  = (int) Double.parseDouble(extractValue(entry, "ingredient_id"));
                double amount = Double.parseDouble(extractValue(entry, "amount_per_cup"));
                for (Ingredient ing : ingredients) {
                    if (ing.getId() == ingId) {
                        recipe.put(ing, amount);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DB] Could not load recipe for coffee_type_id=" + coffeeTypeId + ": " + e.getMessage());
        }
        return recipe;
    }

    
    //  ADD / REMOVE INGREDIENT
  

    public static void addIngredient(String name, String unit,
                                     double threshold, double initialStock) {
        try {
            String params = "name="      + encode(name)
                          + "&unit="     + encode(unit)
                          + "&threshold=" + threshold
                          + "&stock="    + initialStock;
            post("add_ingredient.php", params);
            System.out.println("[DB] Ingredient added: " + name);
        } catch (Exception e) {
            System.out.println("[DB] Could not add ingredient: " + e.getMessage());
        }
    }

    public static void removeIngredient(int id) {
        try {
            post("remove_ingredient.php", "id=" + id);
            System.out.println("[DB] Ingredient removed: id=" + id);
        } catch (Exception e) {
            System.out.println("[DB] Could not remove ingredient: " + e.getMessage());
        }
    }

  
    //  ADD / REMOVE COFFEE TYPE


    public static void addCoffeeType(String name, Map<Integer, Double> recipe) {
        try {
           
            String response = post("add_coffee_type.php", "name=" + encode(name));
            int newId = (int) Double.parseDouble(response.trim());

            // 2. Insert each recipe row
            for (Map.Entry<Integer, Double> entry : recipe.entrySet()) {
                String params = "coffee_type_id=" + newId
                              + "&ingredient_id=" + entry.getKey()
                              + "&amount_per_cup=" + entry.getValue();
                post("add_recipe.php", params);
            }
            System.out.println("[DB] Coffee type added: " + name);
        } catch (Exception e) {
            System.out.println("[DB] Could not add coffee type: " + e.getMessage());
        }
    }

    public static void removeCoffeeType(int id) {
        try {
            // ON DELETE CASCADE handles recipe rows automatically
            post("remove_coffee_type.php", "id=" + id);
            System.out.println("[DB] Coffee type removed: id=" + id);
        } catch (Exception e) {
            System.out.println("[DB] Could not remove coffee type: " + e.getMessage());
        }
    }


    //  SAVE STOCK  
    

    public static void saveStock(Ingredient ingredient,
                                 double current, double theoretical) {
        try {
            String params = "id="                + ingredient.getId()
                          + "&current_stock="    + current
                          + "&theoretical_stock=" + theoretical;
            post("update_stock.php", params);
        } catch (Exception e) {
            System.out.println("[DB] Could not save stock: " + e.getMessage());
        }
    }


    //  LOG PRODUCTION
 

    public static void logProduction(String coffeeName, int quantity) {
        try {
            String params = "coffee_type=" + encode(coffeeName)
                          + "&quantity="   + quantity;
            post("add_production.php", params);
        } catch (Exception e) {
            System.out.println("[DB] Could not log production: " + e.getMessage());
        }
    }

    //  LOG REFILL
 

    public static void logRefill(Ingredient ingredient, double amount) {
        try {
            String params = "ingredient=" + encode(ingredient.getDisplayName())
                          + "&amount="    + amount;
            post("add_refill.php", params);
        } catch (Exception e) {
            System.out.println("[DB] Could not log refill: " + e.getMessage());
        }
    }


    //  LOAD LOGS

    public static Map<String, List<String>> loadLogs() {
        Map<String, List<String>> result = new HashMap<>();
        result.put("production", new ArrayList<>());
        result.put("refill",     new ArrayList<>());
        try {
            String response = get("get_logs.php");

            String prodSection = extractSection(response, "production");
            if (!prodSection.isEmpty()) {
                String[] entries = prodSection.replace("[","").replace("]","").split("\\},\\{");
                for (String entry : entries) {
                    entry = entry.replace("{","").replace("}","").trim();
                    if (entry.isEmpty()) continue;
                    String coffee = extractValue(entry, "coffee_type");
                    String qty    = extractValue(entry, "quantity");
                    String time   = extractValue(entry, "produced_at");
                    result.get("production").add("[" + time + "]  " + coffee + "  x" + qty);
                }
            }

            String refillSection = extractSection(response, "refill");
            if (!refillSection.isEmpty()) {
                String[] entries = refillSection.replace("[","").replace("]","").split("\\},\\{");
                for (String entry : entries) {
                    entry = entry.replace("{","").replace("}","").trim();
                    if (entry.isEmpty()) continue;
                    String ingredient = extractValue(entry, "ingredient");
                    String amount     = extractValue(entry, "amount");
                    String time       = extractValue(entry, "refilled_at");
                    result.get("refill").add("[" + time + "]  +" + amount + "  " + ingredient);
                }
            }
        } catch (Exception e) {
            System.out.println("[DB] Could not load logs: " + e.getMessage());
        }
        return result;
    }

   
    //  INTERNAL HELPERS
  

    private static String get(String endpoint) throws Exception {
        URI uri = new URI(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private static String post(String endpoint, String params) throws Exception {
        URI uri = new URI(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = json.indexOf("\"", start);
            return end == -1 ? "" : json.substring(start, end);
        }
        search = "\"" + key + "\":";
        start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.length();
        return json.substring(start, end).trim().replace("\"","").replace("}","");
    }

    private static String extractSection(String json, String key) {
        String search = "\"" + key + "\":[";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length() - 1;
        int depth = 0, end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) { end++; break; } }
            end++;
        }
        return json.substring(start, end);
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }
}