import javax.swing.*;

/**
 * The main entry point for the virtual pet game application.
 * Initializes game components and launches the user interface.
 */
public class main {
    
    /**
     * The main method that starts the application.
     * 
     * <p>Execution flow:
     * <ol>
     *   <li>Creates a new Player instance</li>
     *   <li>Initializes game save/load functionality</li>
     *   <li>Loads pets from save file</li>
     *   <li>Launches the user interface with loaded pets</li>
     *   <li>Displays the main menu</li>
     * </ol>
     * </p>
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Player player = new Player();
        
        SaveAndLoadGame saveLoad = new SaveAndLoadGame(player);
        
        String saveFileName = "save.json";
        
        Pet[] loadedPets = saveLoad.loadPetsFromSaveFile(saveFileName);
        
        UserInterface ui = new UserInterface(loadedPets);
        
        ui.displayMainMenu();
    }
}
