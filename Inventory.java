import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
/**
 * Manages an inventory of pet items, including loading and saving to a JSON file.
 * 
 * <p>Supported item types: magic potion, bandage, tennis ball, steak, kibble, salmon.</p>
 */

public class Inventory {

    /** List of all valid item types in the inventory */
    private static final List<String> type = Arrays.asList( "magic potion", "bandage", "tennis ball", "steak", "kibble", "salmon"
    );
    
    /** Current inventory mapping items to their quantities */
    private Map<String, Integer> currentInventory;
    /**
     * Constructs a new inventory and loads data from "save.json".
     * Initializes all item quantities to 0 if not found in file.
     */
    public Inventory() {
        currentInventory = new HashMap<>();
        for (String item : type) {
            currentInventory.put(item, 0);
        }
        loadInventoryFromFile("save.json");
    }
    /**
     * Gets the quantity of a specific item in inventory.
     * @param item The item to check
     * @return Quantity of the item, or 0 if not found
     */
    public int getItemValue(String item){
        return currentInventory.get(item);
    }
    /**
     * Adds a specified amount of an item to inventory.
     * @param item The item to add
     * @param amount Quantity to add
     */
    public void addItem(String item, int amount) {
        currentInventory.merge(item, amount, Integer::sum);
        save("save.json");
    }
    /**
     * Removes one unit of an item from inventory (if available).
     * @param item The item to remove
     */
    public void removeItem(String item) {
        if(currentInventory.get(item) > 0){ // Only remove if item quantity is greater than zero
            currentInventory.put(item, currentInventory.get(item) - 1);
            save("save.json");
        }
   
    }
    /**
     * Gets the complete inventory mapping.
     * @return Map of items to their quantities
     */
    public Map<String, Integer> getInventory() {
        return currentInventory;
    }

    /**
     * Loads inventory data from a JSON file.
     * @param filePath Path to the JSON file
     */
    private void loadInventoryFromFile(String filePath) {
        try {
            StringBuilder jsonContent = new StringBuilder();
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine());
            }
            scanner.close();

   
            String jsonString = jsonContent.toString();
            if (jsonString.contains("\"inventory\":")) { // Read file content line by line
                int startIndex = jsonString.indexOf("{", jsonString.indexOf("\"inventory\":")) + 1;
                int endIndex = jsonString.indexOf("}", startIndex);
                String inventoryData = jsonString.substring(startIndex, endIndex);
                String[] items = inventoryData.split(",");

                // Parse the JSON key-value pairs to update inventory
                for (String item : items) {
                    String[] pair = item.split(":"); 
                    String key = pair[0].trim().replace("\"", ""); // Extract key (item name)
                    int value = Integer.parseInt(pair[1].trim()); // Extract value (quantity)
                    if (type.contains(key)) {
                        currentInventory.put(key, value);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found -> " + filePath);
        } catch (Exception e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }
    /**
     * Saves current inventory to a JSON file.
     * @param filePath Path to the JSON file
     */
    public void save(String filePath) {
        try {
            StringBuilder jsonContent = new StringBuilder();
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            String jsonString = jsonContent.toString();

            int inventoryKeyIndex = jsonString.indexOf("\"inventory\":");
            if (inventoryKeyIndex == -1) {
                System.out.println("Inventory section not found");
                return;
            }
            
            int openingBraceIndex = jsonString.indexOf("{", inventoryKeyIndex);
            int closingBraceIndex = findMatchingClosingBrace(jsonString, openingBraceIndex);
            // Rebuild the inventory JSON structure with updated values
            StringBuilder newInventory = new StringBuilder("{\n");
            boolean firstItem = true;
            for (String item : type) {
                if (!firstItem) {
                    newInventory.append(",\n");
                }
                newInventory.append("    \"").append(item).append("\": ").append(currentInventory.get(item));
                firstItem = false;
            }
            newInventory.append("\n  }");
            // Replace old inventory data with updated JSON
            String updatedJson = jsonString.substring(0, openingBraceIndex) 
                    + newInventory.toString()
                    + jsonString.substring(closingBraceIndex + 1);

            FileWriter writer = new FileWriter(filePath);
            writer.write(updatedJson);
            writer.close();
            
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
        }
    }
    /**
     * Helper method to find matching closing brace in JSON string.
     * @param jsonString The JSON content
     * @param openingIndex Index of opening brace
     * @return Index of matching closing brace, or -1 if not found
     */
    private int findMatchingClosingBrace(String jsonString, int openingIndex) {
        int braceCount = 1; // Start with one open brace
        for (int i = openingIndex + 1; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            if (c == '{') braceCount++; // Increase count for nested braces
            if (c == '}') braceCount--; // Decrease count for closing braces
            if (braceCount == 0) return i; // Found matching closing brace
        }
        return -1; 
    }
   
 
   //@Test
   public static void Test(){
    Inventory inventory = new Inventory();

    inventory.addItem("bandage", 2);

    //Test 1
    int expectedItemValue = 2;
    int actualItemValue = inventory.getItemValue("bandage");

    if(expectedItemValue == actualItemValue){
        System.out.println("Inventory Test 1 - PASSED");
    } else {
        System.out.println("Inventory Test 1 - FAILED");
    }

    //Test 2
    inventory.removeItem("magic potion");

    int expectedValue2 = 0;
    int actualValue2 = inventory.getItemValue("magic potion");

    if(expectedValue2 == actualValue2){
        System.out.println("Inventory Test 2 - PASSED");
    } else {
        System.out.println("Inventory Test 2 - FAILED");
    }

    //Test 3
    inventory.addItem("steak", 4);
    inventory.addItem("kibble", 1);

    int[] expectedValue3 = {2, 4, 1};

    int[] actualValue3 = {inventory.getItemValue("bandage"), inventory.getItemValue("steak"), inventory.getItemValue("kibble")};

    if(expectedValue3 == actualValue3){
        System.out.println("Inventory Test 3 - PASSED");
    } else {
        System.out.println("Inventory Test 3 - FAILED");
    }

   }
   
}