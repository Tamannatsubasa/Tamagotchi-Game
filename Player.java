import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
/**
 * Represents a player in the game, managing score persistence and modification.
 * Automatically loads and saves scores to a JSON file.
 */
public class Player {
    /** The player's current score */
    private int score = 0;
    /**
     * Creates a new player and loads score from "save.json".
     */
    public Player(){
        loadPlayerFromFile("save.json");
    }

    /**
     * Gets the current score.
     * @return The player's score
     */
    public int getScore(){
        return this.score;
    }
    /**
     * Sets a new score and saves to file.
     * @param score The new score value
     */
    public void setScore(int score){
        this.score = score;
        save("save.json");
    }

    /**
     * Increases the score by 10 points and saves to file.
     */
    public void increaseScore(){
        this.score = score +10;
        save("save.json");
    }
    /**
     * Decreases the score by 10 points and saves to file.
     */
    public void decreaseScore(){
        this.score = score -10;
        save("save.json");
    }
    /**
     * Loads player score from a JSON file.
     * @param filePath Path to the JSON save file
     */
    public void loadPlayerFromFile(String filePath) {
        try {
            // Read the JSON file line by line
            StringBuilder jsonContent = new StringBuilder();
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine());
            }
            scanner.close();
            // Extract the score from the JSON content
            String jsonString = jsonContent.toString();
            if (jsonString.contains("\"score\":")){
                int scoreStartIndex = jsonString.indexOf("\"score\":") + "\"score\":".length();
                int scoreEndIndex = jsonString.indexOf(",", scoreStartIndex);
                // If there's no comma, look for closing bracket
                if (scoreEndIndex == -1) {
                    scoreEndIndex = jsonString.indexOf("}", scoreStartIndex);
                }
                // Extract and parse the score value
                String scoreStr = jsonString.substring(scoreStartIndex, scoreEndIndex).trim().replace("\"", "");
                this.score = Integer.parseInt(scoreStr);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found -> " + filePath);
        } catch (Exception e) {
            System.out.println("Error loading player data: " + e.getMessage());
        }
    }
    /**
     * Saves the current score to a JSON file.
     * @param filePath Path to the JSON save file
     */
    public void save(String filePath) {
        try {
            
            File file = new File(filePath);
            Scanner scanner = new Scanner(file);
            StringBuilder jsonContent = new StringBuilder();
            // Read the existing content of the file
            while (scanner.hasNextLine()) {
                jsonContent.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            
            String content = jsonContent.toString();
            
            // Construct the new score JSON key-value pair
            String newScoreStr = "\"score\": \"" + this.score + "\"";
            // Replace the existing score value in the file content
            String updatedContent = content.replaceAll("\"score\":\\s*\"\\d+\"", newScoreStr);
            // Write the updated content back to the file
            PrintWriter writer = new PrintWriter(file);
            writer.write(updatedContent);
            writer.flush();
            writer.close();
            
        } catch (Exception e) {
            System.out.println("Error saving player score: " + e.getMessage());
        }
    }
    /* 
    @Test
    public static void Test(){
        //Test 1
        Player player = new Player();
        player.setScore(10);

        player.increaseScore();

        int expectedValue1 = 20;
        int actualValue1 = player.getScore();

        if(expectedValue1 == actualValue1){
            System.out.println("Player Test 1 - PASSED");
        } else {
            System.out.println("Player Test 1 - FAILED");
        }

    }
    */
}
