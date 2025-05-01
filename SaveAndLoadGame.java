import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class SaveAndLoadGame {
    private Player player;
    
    public SaveAndLoadGame(Player player) {
        this.player = player;
    }
    
    /**
     * Loads pets from the save file.
     * @param fileName The name of the save file
     * @return An array of Pet objects
     */
    public Pet[] loadPetsFromSaveFile(String fileName) {
        Pet[] pets = new Pet[4]; // Assuming 4 pet slots
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            
            String content = jsonContent.toString();
            
            // Extract score
            if (content.contains("\"score\":")) {
                int scoreStart = content.indexOf("\"score\":");
                int scoreValueStart = content.indexOf("\"", scoreStart + 8) + 1;
                int scoreValueEnd = content.indexOf("\"", scoreValueStart);
                String scoreStr = content.substring(scoreValueStart, scoreValueEnd);
                player.setScore(Integer.parseInt(scoreStr));
            }
            
            // Extract inventory (simple approach)
            if (content.contains("\"inventory\":")) {
                // In a real implementation, you would parse the inventory items here
                // For simplicity, we're skipping detailed inventory parsing
            }
            
            // Extract pets array
            if (content.contains("\"pets\":")) {
                int petsStart = content.indexOf("\"pets\":");
                int petsArrayStart = content.indexOf("[", petsStart);
                int petsArrayEnd = findMatchingClosingBracket(content, petsArrayStart);
                String petsArrayStr = content.substring(petsArrayStart + 1, petsArrayEnd);
                
                // Split by pet objects or null values
                int currentPosition = 0;
                int petIndex = 0;
                
                while (currentPosition < petsArrayStr.length() && petIndex < 4) {
                    // Skip leading whitespace and commas
                    while (currentPosition < petsArrayStr.length() && 
                           (petsArrayStr.charAt(currentPosition) == ' ' || 
                            petsArrayStr.charAt(currentPosition) == '\n' || 
                            petsArrayStr.charAt(currentPosition) == ',')) {
                        currentPosition++;
                    }
                    
                    if (currentPosition >= petsArrayStr.length()) {
                        break;
                    }
                    
                    // Check if it's null
                    if (petsArrayStr.startsWith("null", currentPosition)) {
                        pets[petIndex] = null;
                        currentPosition += 4; // Length of "null"
                    } else if (petsArrayStr.charAt(currentPosition) == '{') {
                        // It's a pet object
                        int objectEnd = findMatchingClosingBracket(petsArrayStr, currentPosition);
                        String petObjectStr = petsArrayStr.substring(currentPosition, objectEnd + 1);
                        
                        // Parse pet properties
                        String type = extractStringProperty(petObjectStr, "type");
                        String name = extractStringProperty(petObjectStr, "name");
                        String state = extractStringProperty(petObjectStr, "state");
                        int hunger = extractIntProperty(petObjectStr, "hunger");
                        int sleep = extractIntProperty(petObjectStr, "sleep");
                        int health = extractIntProperty(petObjectStr, "health");
                        int happiness = extractIntProperty(petObjectStr, "happiness");
                        
                        // Create VitalStats object
                        VitalStats stats = new VitalStats();
                        stats.setState(state);
                        stats.setHunger(hunger);
                        stats.setSleep(sleep);
                        stats.setHealth(health);
                        stats.setHappiness(happiness);
                        
                        // Set default decay rates
                        int hungerDecay = 10;
                        int healthDecay = 0;
                        int sleepDecay = 10;
                        int happinessDecay = 10;
                        
                        // Adjust decay rates based on pet type
                        if (type.equals("Cat")) {
                            sleepDecay = 15;
                        } else if (type.equals("Bear")) {
                            hungerDecay = 15;
                        } else if (type.equals("Dog")) {
                            happinessDecay = 15;
                        }
                        
                        // Create the pet with loaded stats
                        Pet pet = new Pet(type, name, hungerDecay, healthDecay, sleepDecay, happinessDecay, type);
                        pet.setStats(stats);
                        
                        pets[petIndex] = pet;
                        currentPosition = objectEnd + 1;
                    }
                    
                    petIndex++;
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error loading save file: " + e.getMessage());
            // Return empty pets array if loading failed
        }
        
        return pets;
    }
    
    // Helper method to find matching closing bracket
    private int findMatchingClosingBracket(String text, int openBracketPosition) {
        char openBracket = text.charAt(openBracketPosition);
        char closeBracket;
        
        if (openBracket == '{') {
            closeBracket = '}';
        } else if (openBracket == '[') {
            closeBracket = ']';
        } else {
            return -1; // Not a bracket
        }
        
        int count = 1;
        for (int i = openBracketPosition + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == openBracket) {
                count++;
            } else if (c == closeBracket) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        
        return -1; // No matching bracket
    }
    
    // Helper method to extract string property
    private String extractStringProperty(String jsonObject, String propertyName) {
        String searchFor = "\"" + propertyName + "\":";
        int propStart = jsonObject.indexOf(searchFor);
        if (propStart == -1) {
            return "";
        }
        
        int valueStart = jsonObject.indexOf("\"", propStart + searchFor.length()) + 1;
        int valueEnd = jsonObject.indexOf("\"", valueStart);
        
        return jsonObject.substring(valueStart, valueEnd);
    }
    
    // Helper method to extract int property
    private int extractIntProperty(String jsonObject, String propertyName) {
        String searchFor = "\"" + propertyName + "\":";
        int propStart = jsonObject.indexOf(searchFor);
        if (propStart == -1) {
            return 0;
        }
        
        int valueStart = propStart + searchFor.length();
        // Skip whitespace
        while (valueStart < jsonObject.length() && 
               (jsonObject.charAt(valueStart) == ' ' || jsonObject.charAt(valueStart) == '\n')) {
            valueStart++;
        }
        
        int valueEnd = valueStart;
        while (valueEnd < jsonObject.length() && 
               Character.isDigit(jsonObject.charAt(valueEnd))) {
            valueEnd++;
        }
        
        return Integer.parseInt(jsonObject.substring(valueStart, valueEnd));
    }
    
    /**
     * Adds a new pet to the next available slot and saves the game.
     * Uses the same JSON format as the UserInterface.saveGameToJson() method.
     * 
     * @param fileName The name of the save file
     * @param newPet The new pet to add
     * @return true if the pet was added and save was successful, false otherwise
     */
    public boolean addPetToNextAvailableSlot(String fileName, Pet newPet) {
        try {
            Pet[] existingPets = loadPetsFromSaveFile(fileName);
            
            boolean petAdded = false;
            for (int i = 0; i < existingPets.length; i++) {
                if (existingPets[i] == null) {
                    existingPets[i] = newPet;
                    petAdded = true;
                    break;
                }
            }
            
            if (!petAdded) {
                System.out.println("No available pet slots!");
                return false;
            }
            
            try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
                out.println("{");
        
                out.println("  \"score\": \"" + player.getScore() + "\",");
                out.println("  \"inventory\": {");
                out.println("    \"salmon\": 5,");
                out.println("    \"tenis ball\": 2,");
                out.println("    \"kibble\": 10,");
                out.println("    \"steak\": 3,");
                out.println("    \"bandage\": 1,");
                out.println("    \"magic potions\": 7");
                out.println("  },");
        
                out.println("  \"pets\": [");
                for (int i = 0; i < existingPets.length; i++) {
                    Pet pet = existingPets[i];
                    if (pet != null) {
                        VitalStats vs = pet.getStats();
                        out.println("    {");
                        out.println("      \"type\": \"" + pet.getSprite() + "\",");
                        out.println("      \"name\": \"" + pet.getName() + "\",");
                        out.println("      \"state\": \"" + vs.getState() + "\",");
                        out.println("      \"hunger\": " + vs.getHunger() + ",");
                        out.println("      \"sleep\": " + vs.getSleep() + ",");
                        out.println("      \"health\": " + vs.getHealth() + ",");
                        out.println("      \"happiness\": " + vs.getHappiness());
                        out.print("    }");
                    } else {
                        out.print("    null");
                    }
                    if (i < existingPets.length - 1) {
                        out.println(",");
                    } else {
                        out.println();
                    }
                }
                out.println("  ]");
                out.println("}");
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Saves the current game state without adding a new pet.
     * 
     * @param fileName The name of the save file
     * @return true if save was successful, false otherwise
     */
    public boolean saveGame(String fileName) {
        
        try {
            Pet[] existingPets = loadPetsFromSaveFile(fileName);
            
            try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
                out.println("{");
        
                out.println("  \"score\": \"" + player.getScore() + "\",");
        
                out.println("  \"inventory\": {");
                out.println("    \"salmon\": 5,");
                out.println("    \"tenis ball\": 2,");
                out.println("    \"kibble\": 10,");
                out.println("    \"steak\": 3,");
                out.println("    \"bandage\": 1,");
                out.println("    \"magic potions\": 7");
                out.println("  },");
        
                out.println("  \"pets\": [");
                for (int i = 0; i < existingPets.length; i++) {
                    Pet pet = existingPets[i];
                    if (pet != null) {
                        VitalStats vs = pet.getStats();
                        out.println("    {");
                        out.println("      \"type\": \"" + pet.getType() + "\",");
                        out.println("      \"name\": \"" + pet.getName() + "\",");
                        out.println("      \"state\": \"" + vs.getState() + "\",");
                        out.println("      \"hunger\": " + vs.getHunger() + ",");
                        out.println("      \"sleep\": " + vs.getSleep() + ",");
                        out.println("      \"health\": " + vs.getHealth() + ",");
                        out.println("      \"happiness\": " + vs.getHappiness());
                        out.print("    }");
                    } else {
                        out.print("    null");
                    }
                    if (i < existingPets.length - 1) {
                        out.println(",");
                    } else {
                        out.println();
                    }
                }
                out.println("  ]");
                out.println("}");
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
    }
    public boolean updatePetOnTheFly(String fileName, Pet petToUpdate, VitalStats newStats) {
        try {
            Pet[] existingPets = loadPetsFromSaveFile(fileName);
            boolean petFound = false;
            
            for (int i = 0; i < existingPets.length; i++) {
                Pet current = existingPets[i];
                if (current != null 
                        && current.getName().equals(petToUpdate.getName()) 
                        && current.getType().equals(petToUpdate.getType())) {
                    current.setStats(newStats);
                    petFound = true;
                    break;
                }
            }
            
            if (!petFound) {
                System.out.println("Pet not found in the save file.");
                return false;
            }
            
            try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
                out.println("{");
        
                out.println("  \"score\": \"" + player.getScore() + "\",");
                out.println("  \"inventory\": {");
                out.println("    \"salmon\": 5,");
                out.println("    \"tenis ball\": 2,");
                out.println("    \"kibble\": 10,");
                out.println("    \"steak\": 3,");
                out.println("    \"bandage\": 1,");
                out.println("    \"magic potions\": 7");
                out.println("  },");
        
                out.println("  \"pets\": [");
                for (int i = 0; i < existingPets.length; i++) {
                    Pet pet = existingPets[i];
                    if (pet != null) {
                        VitalStats vs = pet.getStats();
                        out.println("    {");
                        out.println("      \"type\": \"" + pet.getType() + "\",");
                        out.println("      \"name\": \"" + pet.getName() + "\",");
                        out.println("      \"state\": \"" + vs.getState() + "\",");
                        out.println("      \"hunger\": " + vs.getHunger() + ",");
                        out.println("      \"sleep\": " + vs.getSleep() + ",");
                        out.println("      \"health\": " + vs.getHealth() + ",");
                        out.println("      \"happiness\": " + vs.getHappiness());
                        out.print("    }");
                    } else {
                        out.print("    null");
                    }
                    if (i < existingPets.length - 1) {
                        out.println(",");
                    } else {
                        out.println();
                    }
                }
                out.println("  ]");
                out.println("}");
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Error updating pet on the fly: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean saveParentalControls(String fileName, ParentalControls parentalControls) {
        try {
            StringBuilder fileContentBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContentBuilder.append(line).append("\n");
                }
            }
            String content = fileContentBuilder.toString();
    
            String newParentalBlock = "  \"parentalControls\": {\n" +
                    "    \"allowedStart\": \"" + parentalControls.getAllowedStart() + "\",\n" +
                    "    \"allowedEnd\": \"" + parentalControls.getAllowedEnd() + "\",\n" +
                    "    \"timeControlEnabled\": " + parentalControls.isTimeControlEnabled() + ",\n" +
                    "    \"totalPlayTime\": " + parentalControls.getTotalPlayTime() + ",\n" +
                    "    \"sessionCount\": " + parentalControls.getSessionCount() + "\n" +
                    "  }";
    
            if (content.contains("\"parentalControls\":")) {
                content = content.replaceAll("\"parentalControls\":\\s*\\{[^\\}]*\\}", newParentalBlock);
            } else {
                int petsIndex = content.indexOf("\"pets\":");
                if (petsIndex != -1) {
                    String before = content.substring(0, petsIndex);
                    String after = content.substring(petsIndex);
                    content = before + newParentalBlock + ",\n" + after;
                } else {
                    content += newParentalBlock;
                }
            }
            
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(content);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error saving parental controls: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}