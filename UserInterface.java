import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.time.LocalTime;

/**
 * CS 2212B
 * Name: Wyatt Cassiotis
 * Email: Wcassiot@uwo.ca
 * Created: 22 March 2025
 * 
 * This class handles the user interface for the virtual pet game.
 * It creates and manages the main window (JFrame) and various panels,
 * buttons, and timers that control game flow and pet interactions.
 */
public class UserInterface {
    // Creates a new player object to use during the game
    Player player = new Player();
    // The vital stats object tracking pet stats like hunger, health, etc.
    private VitalStats vitalStats;
    
    // Colors used in the ui
    private final Color Blue1 = new Color(9, 0, 66);
    private final Color Blue2 = new Color(52, 103, 189);
    
    // checking certian states
    private boolean isPetDisabled = false;
    private boolean isVetDisabled = false;
    private boolean isExerciceDisabled = false;
    private boolean isSleeping = false;

    // Making the mainfram 
    private JFrame mainFrame;
    // Pet's type and name.
    private String petType = "";
    private String petName = "";
    
    // Timers to frequently update 
    private Timer statsTimer;
    private Timer playTimeTimer;
    
    // Decay rates for pet's different vital stats
    private int hungerDecay = 10, healthDecay = 0, sleepDecay = 10, happinessDecay = 10;
    
    // Progress bars for displaying pet's stats
    private JProgressBar hungerBar, healthBar, happinessBar, sleepBar;

    // Label to display pet state 
    private JLabel stateLabel;
    
    // To store the state before sleeping
    private String previousState;
    
    // Array to hold up to 4 pets
    private Pet[] pets; 
    // Object to handle game saving and loading
    private SaveAndLoadGame saveLoad;
    // Name of the save file
    private String saveFileName;
    // Currently selected pet for the game
    private Pet currentPet;
    // Flag to indicate if the game is running
    private boolean isRunning;
    // Object for managing parental controls
    private ParentalControls pc = new ParentalControls();
    // Object representing the player's inventory
    private Inventory inventory = new Inventory();
    // Timer used to disable buttons temporarily
    private Timer disabledTimer;

    /**
     * Constructor for the user interface.
     * It loads any saved pets, initializes game objects,
     * and sets up the main frame for the game.
     * 
     * @param loadedPets Array of pets loaded from a save file (should be length 4)
     */
    public UserInterface(Pet[] loadedPets) {
        // Load pets if valid, otherwise create a new array for 4 pets
        if (loadedPets != null && loadedPets.length == 4) {
            this.pets = loadedPets;
        } else {
            this.pets = new Pet[4]; 
        }
        
        // Initialize the player, parental controls, and save/load objects
        this.player = new Player();
        this.pc = new ParentalControls();
        this.saveLoad = new SaveAndLoadGame(player);
        this.saveFileName = "save.json";
        
        // Set up the main game window
        mainFrame = new JFrame("VIRTUAL PET GAME");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
        
        // Play opening sound
        playSound("open.wav");
        isRunning = false;
    }

    /**
     * Adds a new pet if an empty slot is available.
     * 
     * @param petType The type of pet (e.g., bear, cat, dog)
     * @param petName The name of the new pet
     * @param vitalStats The initial vital stats for the pet
     * @param hungerDecay Decay value for hunger
     * @param healthDecay Decay value for health
     * @param sleepDecay Decay value for sleep
     * @param happinessDecay Decay value for happiness
     * @return True if the pet was added successfully, false otherwise
     */
    public boolean addNewPet(String petType, String petName, VitalStats vitalStats,
        int hungerDecay, int healthDecay, int sleepDecay, int happinessDecay) {
        
        // Create a new pet object
        Pet newPet = new Pet(petType, petName, hungerDecay, healthDecay, sleepDecay, happinessDecay, petType);
        newPet.setStats(vitalStats);
        boolean hasEmptySlot = false;
        
        // Check for an empty slot among the pet array
        for (int i = 0; i < pets.length; i++) {
            if (pets[i] == null) {
                hasEmptySlot = true;
                break;
            }
        }
        
        // Inform the user if there are no available slots
        if (!hasEmptySlot) {
            JOptionPane.showMessageDialog(mainFrame,
                "Cannot add new pet. All pet slots are full.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Save the new pet to the next available slot
        boolean success = saveLoad.addPetToNextAvailableSlot(saveFileName, newPet);
        
        if (success) {
            // Reload the pet list from the save file
            pets = saveLoad.loadPetsFromSaveFile(saveFileName);
            System.out.println("New pet added successfully");
            return true;
        } else {
            JOptionPane.showMessageDialog(mainFrame,
                "Failed to save the new pet.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Displays the main menu for the game
     * Provides options to start a new game, load a game, access parental controls,
     * view the tutorial, or exit the game
     */
    public void displayMainMenu() {
        isRunning = false;
        
        // Load and scale the background image to fill the window
        URL sourceBackground = getClass().getResource("/hi.jpg");
        ImageIcon background = new ImageIcon(sourceBackground);
        Image originalImage = background.getImage();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Image scaledImage = originalImage.getScaledInstance(
                screenSize.width,
                screenSize.height,
                Image.SCALE_SMOOTH
        );
        background = new ImageIcon(scaledImage);
        
        // Create a label to hold the background image
        JLabel backgroundLabel = new JLabel(background);
        backgroundLabel.setLayout(new GridBagLayout());
        
        // Create the button panel with vertical layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    
        // Create buttons for various menu options
        JButton newGameButton = createButton("Start New Game");
        JButton loadGameButton = createButton("Load Game");
        JButton parentalControlButton = createButton("Parental Controls");
        JButton tutorialButton = createButton("Tutorial");
        JButton exitButton = createButton("Exit");
        
        // Center align all buttons
        newGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        parentalControlButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        tutorialButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add buttons and vertical spacing
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(newGameButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(loadGameButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(parentalControlButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(tutorialButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());
        
        // Add action listeners for each button
        newGameButton.addActionListener(e -> {
            playSound("button.wav");
            displayStartGame();
        });
        loadGameButton.addActionListener(e -> {
            playSound("button.wav");
            displayLoadGame();
        });
        parentalControlButton.addActionListener(e -> {
            playSound("button.wav");
            
            // Handle parental control password setting or verification
            if (pc.getPassword() == null || pc.getPassword().isEmpty()) {
                String newPassword = JOptionPane.showInputDialog(null,
                        "No password set. Please enter a new password:");
                if (newPassword != null && !newPassword.isEmpty()) {
                    pc.setPassword(newPassword);
                    JOptionPane.showMessageDialog(null, "Password set successfully.");
                    displayParentalControl();
                } else {
                    JOptionPane.showMessageDialog(null, "Password not set. Try again.");
                }
            } else {
                String inputPassword = JOptionPane.showInputDialog(null, "Enter password:");
                if (inputPassword != null && inputPassword.equals(pc.getPassword())) {
                    displayParentalControl();
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect password.");
                }
            }
        });
        tutorialButton.addActionListener(e -> {
            playSound("button.wav");
            displayTutorial();
        });
        exitButton.addActionListener(e -> {
            playSound("button.wav");
            mainFrame.dispose();
        });
        
        // Set up layout constraints and add the button panel to the background label
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        backgroundLabel.add(buttonPanel, gbc);
        
        // Refresh the main frame to show the new content
        mainFrame.setContentPane(backgroundLabel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     * Displays the start game screen.
     * Provides the player with pet options to choose from.
     * Once selected, the player is prompted to name the pet and the pet is added to the save file.
     */
    public void displayStartGame() {
        mainFrame.getContentPane().removeAll();
        
        // Main panel with grid layout
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(Blue2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
    
        // panel with a back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Blue2);
        JButton backButton = createButton("Back");
        backButton.addActionListener(e -> {
            playSound("button.wav");
            displayMainMenu();
        });
        topPanel.add(backButton, BorderLayout.EAST);
    
        // Add ESC shortcut to return to main menu
        mainContainer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goBack");
        mainContainer.getActionMap().put("goBack", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayMainMenu();
            }
        });
    
        // panel for pet selection
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Blue2);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // label for pet selection
        JLabel titleLabel = new JLabel("Pets", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.white);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
    
        // Panel for pet cards 
        JPanel petSelectionPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        petSelectionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        petSelectionPanel.setBackground(Blue2);
    
        // Special effects descriptions for each pet type
        String[] specialEffects = {
            "Gets sleepier faster -15/5s instead of 10/5s",
            "Gets hungry faster -15/5s instead of 10/5s",
            "Loses happiness faster -15/5s instead of 10/5s"
        };
    
        // Array of pet types and corresponding images
        String[] petTypes = {"Cat", "Bear", "Dog"};
        String[] petImages = {
            "/petImages/Cat/Screenshot_3.png",
            "/petImages/Bear/Screenshot_3.png",
            "/petImages/Dog/Screenshot_3.png"
        };
    
        // Loop to create a card for each pet type
        for (int i = 0; i < petImages.length; i++) {
            String petInfo = specialEffects[i];
            String imagePath = petImages[i];
            String currentPetType = petTypes[i]; 
    
            // Create card panel for the pet
            JPanel petCard = new JPanel();
            petCard.setLayout(new BoxLayout(petCard, BoxLayout.Y_AXIS));
            petCard.setBackground(Blue2);
            petCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    
            // Label for pet image
            JLabel imageLabel = new JLabel();
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
            // Load the pet image and scale it
            URL imageURL = getClass().getResource(imagePath);
            if (imageURL != null) {
                ImageIcon petIcon = new ImageIcon(imageURL);
                int fixedHeight = 150;
                int originalWidth = petIcon.getIconWidth();
                int originalHeight = petIcon.getIconHeight();
                int scaledWidth = (int) ((double) originalWidth / originalHeight * fixedHeight);
                Image scaledImage = petIcon.getImage().getScaledInstance(scaledWidth, fixedHeight, Image.SCALE_SMOOTH);
                petIcon = new ImageIcon(scaledImage);
                imageLabel.setIcon(petIcon);
            } else {
                // if image is not found.
                imageLabel.setPreferredSize(new Dimension(120, 150));
                imageLabel.setText("<html><div style='text-align:center;'>No Image<br/>Found</div></html>");
                imageLabel.setForeground(Color.RED);
            }
    
            // Button to select the pet
            JButton selectButton = createButton("Select");
            selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            selectButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            selectButton.addActionListener(e -> {
                playSound("button.wav");
                petType = currentPetType;
                // Prompt the user to input a name for the pet
                String inputName = JOptionPane.showInputDialog(
                        mainFrame,
                        "Enter a name for your pet:",
                        "Pet Name",
                        JOptionPane.PLAIN_MESSAGE
                );
                if (inputName != null && !inputName.trim().isEmpty()) {
                    petName = inputName.trim();
                } else {
                    petName = "Pet A";
                }
                
                // Adjust stat decay values based on pet type
                switch (petType) {
                    case "Cat":
                        sleepDecay = 15;
                        break;
                    case "Bear":
                        hungerDecay = 15;
                        break;
                    case "Dog":
                        happinessDecay = 15;
                        break;
                }
                VitalStats newVitalStats = new VitalStats();
                
                // Add the new pet and save the game
                boolean added = addNewPet(petType, petName, newVitalStats, hungerDecay, healthDecay, sleepDecay, happinessDecay);
                if (added) {
                    this.vitalStats = newVitalStats;
                    saveLoad.saveGame("save.json");
                    displayMainMenu();
                }
            });
    
            // Label describing the pet's special effect
            JLabel petInfoLabel = new JLabel(petInfo, SwingConstants.CENTER);
            petInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            petInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            petInfoLabel.setForeground(Color.white);
            petInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
    
            // Assemble the pet card
            petCard.add(Box.createVerticalStrut(10));
            petCard.add(imageLabel);
            petCard.add(Box.createVerticalStrut(5));
            petCard.add(selectButton);
            petCard.add(Box.createVerticalStrut(5));
            petCard.add(petInfoLabel);
            petCard.add(Box.createVerticalStrut(10));
    
            // Make the entire card clickable
            petCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            petCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectButton.requestFocusInWindow();
                    selectButton.doClick();
                }
            });
    
            petSelectionPanel.add(petCard);
        }
        
        contentPanel.add(petSelectionPanel);
    
        // Center panel that holds all content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Blue2);
        centerPanel.add(contentPanel, new GridBagConstraints());
    
        // Add top and center panels to the main container
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        mainContainer.add(topPanel, gbc);
    
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainContainer.add(centerPanel, gbc);
    
        // Refresh the main frame to display the start game screen
        mainFrame.setContentPane(mainContainer);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     * Displays the load game screen
     * Shows saved pets and allows the user to load a pet
     */
    public void displayLoadGame() {
        mainFrame.getContentPane().removeAll();
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Blue2);
        
        // Add ESC key shortcut to return to the main menu
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToMainMenu");
        actionMap.put("returnToMainMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayMainMenu();
            }
        });
        
        // Top panel with title and back button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Load Game", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = createButton("Back");
        backButton.addActionListener(e -> {
            playSound("button.wav");
            displayMainMenu();
        });
        topPanel.add(backButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Load saved pets from file
        Pet[] savedPets = saveLoad.loadPetsFromSaveFile(saveFileName);
        
        // Center panel with grid layout for pet cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerPanel.setBackground(Blue2);
        
        boolean hasPets = false;
        for (Pet pet : savedPets) {
            if (pet != null) {
                hasPets = true;
                break;
            }
        }
        
        // Display message if no saved pets are found
        if (!hasPets) {
            JLabel noPetsLabel = new JLabel("No saved pets found. Create a new pet first.", SwingConstants.CENTER);
            noPetsLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            noPetsLabel.setForeground(Color.WHITE);
            centerPanel.setLayout(new BorderLayout());
            centerPanel.add(noPetsLabel, BorderLayout.CENTER);
        } else {
            // Loop to add each saved pet to the display
            for (int i = 0; i < savedPets.length; i++) {
                Pet pet = savedPets[i];
                if (pet != null) {
                    JPanel petCard = createPetCard(pet, i);
                    centerPanel.add(petCard);
                } else {
                    // Display an empty slot if no pet is saved
                    JPanel emptySlot = new JPanel();
                    emptySlot.setLayout(new BoxLayout(emptySlot, BoxLayout.Y_AXIS));
                    emptySlot.setBackground(new Color(200, 200, 200, 150));
                    emptySlot.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    
                    JLabel emptyLabel = new JLabel("Empty Slot", SwingConstants.CENTER);
                    emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 24));
                    emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    emptyLabel.setForeground(Color.DARK_GRAY);
                    
                    emptySlot.add(Box.createVerticalGlue());
                    emptySlot.add(emptyLabel);
                    emptySlot.add(Box.createVerticalGlue());
                    
                    centerPanel.add(emptySlot);
                }
            }
        }
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainFrame.setContentPane(mainPanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     * Displays the main game screen
     * This screen allows the player to interact with the pet,
     * view animations, manage the inventory, and access mini-games
     */
    public void displayGame() {
        isRunning = true;
        // Start tracking playtime for parental controls.
        pc.startSession(); 
    
        // Timer to increase playtime every minute
        playTimeTimer = new Timer(60000, e -> { 
            if (isRunning) {
                pc.addPlayTime();
            }
        });
        playTimeTimer.start();
    
        // Main panel with a custom background image
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            private Image backgroundImage = new ImageIcon(getClass().getResource("/bi.jpg")).getImage();
    
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
    
        // Set up key binding to return to the main menu using the ESC key
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();
    
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToMain");
        actionMap.put("backToMain", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayMainMenu();
            }
        });
    
        // Top panel with a button to return to the main menu
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton backToMainButton = createButton("Back to Main Menu");
        backToMainButton.addActionListener(e -> {
            playSound("button.wav");
            displayMainMenu();
        });
        topPanel.add(backToMainButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    
        // Center panel containing pet information and game content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);  
        centerPanel.add(Box.createVerticalGlue());
    
        // Panel to display pet name and player score
        JPanel petInfoPanel = new JPanel();
        petInfoPanel.setOpaque(false);
        petInfoPanel.setLayout(new BoxLayout(petInfoPanel, BoxLayout.Y_AXIS));
        petInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        JLabel petNameLabel = new JLabel(petName);
        petNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        petNameLabel.setForeground(Color.WHITE);
    
        stateLabel = new JLabel("Score: " + player.getScore());
        stateLabel.setForeground(Color.WHITE);
    
        petInfoPanel.add(petNameLabel);
        petInfoPanel.add(stateLabel);
        centerPanel.add(petInfoPanel);
    
        // Content panel with a grid layout to hold progress bars and pet image
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
    
        // Panel for progress bars representing pet vital stats
        JPanel progressContainerPanel = new JPanel();
        progressContainerPanel.setBackground(new Color(128, 128, 128, 128));
        progressContainerPanel.setOpaque(true);
        progressContainerPanel.setLayout(new BoxLayout(progressContainerPanel, BoxLayout.Y_AXIS));
        progressContainerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        Dimension barSize = new Dimension(200, 30);
    
        // Create and add the hunger progress bar
        JLabel hungerLabel = new JLabel("Hunger:");
        hungerLabel.setForeground(Color.WHITE);
        hungerBar = new JProgressBar(0, 100);
        hungerBar.setValue(vitalStats.getHunger());
        hungerBar.setPreferredSize(barSize);
        hungerBar.setForeground(new Color(255, 99, 71));
        progressContainerPanel.add(hungerLabel);
        progressContainerPanel.add(hungerBar);
        progressContainerPanel.add(Box.createVerticalStrut(10));
    
        // Create and add the health progress bar
        JLabel healthLabel = new JLabel("Health:");
        healthLabel.setForeground(Color.WHITE);
        healthBar = new JProgressBar(0, 100);
        healthBar.setValue(vitalStats.getHealth());
        healthBar.setPreferredSize(barSize);
        healthBar.setForeground(new Color(50, 205, 50));
        progressContainerPanel.add(healthLabel);
        progressContainerPanel.add(healthBar);
        progressContainerPanel.add(Box.createVerticalStrut(10));
    
        // Create and add the happiness progress bar
        JLabel happinessLabel = new JLabel("Happiness:");
        happinessLabel.setForeground(Color.WHITE);
        happinessBar = new JProgressBar(0, 100);
        happinessBar.setValue(vitalStats.getHappiness());
        happinessBar.setPreferredSize(barSize);
        happinessBar.setForeground(new Color(255, 215, 0));
        progressContainerPanel.add(happinessLabel);
        progressContainerPanel.add(happinessBar);
        progressContainerPanel.add(Box.createVerticalStrut(10));
    
        // Create and add the sleep progress bar
        JLabel sleepLabel = new JLabel("Sleep:");
        sleepLabel.setForeground(Color.WHITE);
        sleepBar = new JProgressBar(0, 100);
        sleepBar.setValue(vitalStats.getSleep());
        sleepBar.setPreferredSize(barSize);
        sleepBar.setForeground(new Color(65, 105, 225));
        progressContainerPanel.add(sleepLabel);
        progressContainerPanel.add(sleepBar);
    
        // Position the progress bars on the content panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(progressContainerPanel, gbc);
    
        // Set up the pet image with an array of sprites for different states
        JLabel petImageLabel = new JLabel();
        petImageLabel.setOpaque(false);
        petImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String[] petSprites = {
            "/petImages/" + petType + "/screenshot_1.png",
            "/petImages/" + petType + "/Flip1.png",
            "/petImages/" + petType + "/Flip2.png",
            "/petImages/" + petType + "/screenshot_3.png",
            "/petImages/" + petType + "/SLEEP.png",
            "/petImages/" + petType + "/ANGRY.png",
            "/petImages/" + petType + "/HUNGRY.png",
            "/petImages/" + petType + "/DEAD.png"
        };
        final int[] spriteIndex = {0};
        setPetImage(petImageLabel, petSprites[spriteIndex[0]]);
    
        // Timer to cycle through pet animations every 2 seconds
        Timer spriteTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(vitalStats.getState());
                if (vitalStats.getState().equals("dead")) {
                    setPetImage(petImageLabel, petSprites[7]);
                } else if (isSleeping) {
                    setPetImage(petImageLabel, petSprites[4]);
                } else if (vitalStats.getState().equals("angry")) {
                    setPetImage(petImageLabel, petSprites[5]);
                } else if (vitalStats.getState().equals("hungry")) {
                    setPetImage(petImageLabel, petSprites[6]);
                } else {
                    spriteIndex[0] = (spriteIndex[0] + 1) % (petSprites.length - 4);
                    setPetImage(petImageLabel, petSprites[spriteIndex[0]]);
                }
            }
        });
        spriteTimer.start();
    
        // Timer to ensure the sleeping sprite is displayed continuously if pet is sleeping
        Timer spriteSleepTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSleeping) {
                    setPetImage(petImageLabel, petSprites[4]);
                }
            }
        });
        spriteSleepTimer.start();
    
        // Timer to detect when the pet should start sleeping
        Timer startSleepTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((vitalStats.getState().equals("sleeping")) && (!isSleeping)) {
                    System.out.println(isSleeping);
                    System.out.println(vitalStats.getState().equals("sleeping"));
                    playSound("sleep.wav");
                    isSleeping = true;
                    updateBarsAndState();
                    System.out.println(vitalStats.getState());
                    Timer sleepTimer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (vitalStats.getSleep() < 100) {
                                vitalStats.decay(0, 0, -10, 0);
                                System.out.println("Sleep: " + vitalStats.getSleep());
                                updateBarsAndState();
                            } else {
                                vitalStats.setState("alive");
                                isSleeping = false;
                                ((Timer) event.getSource()).stop();
                                player.increaseScore();
                                updateBarsAndState();
                            }
                        }
                    });
                    sleepTimer.start();
                }
            }
        });
        startSleepTimer.start();
    
        // Position the pet image on the content panel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(petImageLabel, gbc);
        centerPanel.add(contentPanel);
        centerPanel.add(Box.createVerticalGlue());
    
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    
        // Bottom panel containing action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 134, 10));
        bottomPanel.setOpaque(false);
    
        // Create buttons for inventory, minigame, exercise, petting, sleeping, and vet visits
        JButton inventoryButton = createButton("Inventory");
        JButton minigameButton = createButton("Minigame");
        JButton exerciceButton = createButton("Exercice");
        JButton petButton = createButton("Play");
        JButton sleepButton = createButton("Go to bed");
        JButton vetButton = createButton("Take to vet");
    
        // Timer to disable buttons based on pet's state
        disabledTimer = new Timer(1000, e -> { 
            String state = currentPet.getStats().getState();
            if (state.equals("dead") || state.equals("sleeping")) {
                inventoryButton.setEnabled(false);
                minigameButton.setEnabled(false);
                exerciceButton.setEnabled(false);
                petButton.setEnabled(false);
                sleepButton.setEnabled(false);
                vetButton.setEnabled(false);
            } else if (state.equals("angry")) {
                inventoryButton.setEnabled(true);
                petButton.setEnabled(true);
                minigameButton.setEnabled(false);
                exerciceButton.setEnabled(false);
                sleepButton.setEnabled(false);
                vetButton.setEnabled(false);
            } else if (state.equals("hungry") || state.equals("alive") || state.equals("normal")) {
                inventoryButton.setEnabled(true);
                minigameButton.setEnabled(true);
                exerciceButton.setEnabled(true);
                petButton.setEnabled(true);
                sleepButton.setEnabled(true);
                vetButton.setEnabled(true);
            }
        });
        disabledTimer.start();

        // Define and add action for the Inventory button
        AbstractAction inventoryAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping) {
                    playSound("inventory.wav");
                    displayInventory();
                }
            }
        };
        inventoryButton.addActionListener(inventoryAction);
        actionMap.put("inventory", inventoryAction);
    
        // Define and add action for the Minigame button
        AbstractAction minigameAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping) {
                    playSound("minigame.wav");
                    displayMiniGame();
                    player.increaseScore();
                    updateBarsAndState();
                }
            }
        };
        minigameButton.addActionListener(minigameAction);
        actionMap.put("minigame", minigameAction);
    
        // Define and add action for the pet play button
        AbstractAction playAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping && !isPetDisabled) {
                    vitalStats.decay(0, 0, 0, -10);
                    happinessBar.setValue(vitalStats.getHappiness());
                    playSound("button.wav");
                    player.increaseScore();
                    updateBarsAndState();
                    isPetDisabled = true;
                    petButton.setEnabled(false);
                    new Timer(4000, event -> {
                        isPetDisabled = false;
                        petButton.setEnabled(true);
                    }).start();
                }
            }
        };
        petButton.addActionListener(playAction);
        actionMap.put("play", playAction);
    
        // Define and add action for the sleep button
        AbstractAction bedAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping) {
                    playSound("sleep.wav");
                    previousState = vitalStats.getState();
                    vitalStats.setState("sleeping");
                    isSleeping = true;
                    updateBarsAndState();
                    Timer sleepTimer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (vitalStats.getSleep() < 100) {
                                vitalStats.decay(0, 0, -10, 0);
                                updateBarsAndState();
                            } else {
                                vitalStats.setState(previousState);
                                isSleeping = false;
                                ((Timer) event.getSource()).stop();
                                player.increaseScore();
                                updateBarsAndState();
                            }
                        }
                    });
                    sleepTimer.start();
                } else {
                    playSound("button.wav");
                }
            }
        };
        sleepButton.addActionListener(bedAction);
        actionMap.put("bed", bedAction);
    
        // Define and add action for the vet button
        AbstractAction vetAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping && !isVetDisabled) {
                    vitalStats.decay(0, -10, 0, 0);
                    playSound("heal.wav");
                    player.decreaseScore();
                    updateBarsAndState();
                    isVetDisabled = true;
                    vetButton.setEnabled(false);
                    new Timer(4000, event -> {
                        isVetDisabled = false;
                        vetButton.setEnabled(true);
                    }).start();
                }
            }
        };
        vetButton.addActionListener(vetAction);
        actionMap.put("vet", vetAction);
    
        // Define and add action for the exercise button
        AbstractAction exerciseAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isSleeping && !isExerciceDisabled) {
                    playSound("heal.wav");
                    vitalStats.decay(10, -10, 10, 0);
                    player.increaseScore();
                    updateBarsAndState();
                    isExerciceDisabled = true;
                    exerciceButton.setEnabled(false);
                    new Timer(4000, event -> {
                        isExerciceDisabled = false;
                        exerciceButton.setEnabled(true);
                    }).start();
                }
            }
        };
        exerciceButton.addActionListener(exerciseAction);
        actionMap.put("exercise", exerciseAction);
    
        // Add all buttons to the bottom panel
        bottomPanel.add(inventoryButton);
        bottomPanel.add(minigameButton);
        bottomPanel.add(exerciceButton);
        bottomPanel.add(petButton);
        bottomPanel.add(sleepButton);
        bottomPanel.add(vetButton);
    
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        // Set up a timer to periodically decay vital stats
        if (statsTimer != null && statsTimer.isRunning()) {
            statsTimer.stop();
        }
        
        statsTimer = new Timer(5000, e -> {
            if (isRunning) {
                // Decay stats based on predefined decay values
                vitalStats.decay(hungerDecay, healthDecay, sleepDecay, happinessDecay);
                
                // Extra decay when the pet is hungry.
                if (vitalStats.getState().equals("hungry")) {
                    vitalStats.decay(0, 10, 0, 0);
                    vitalStats.decay(5, 0, 0, 0);
                }
                System.out.println("Attempting to save game to: " + saveFileName);
                boolean saved = saveLoad.updatePetOnTheFly(saveFileName, currentPet, currentPet.getStats());
                if (saved) {
                    System.out.println("Game saved successfully");
                } else {
                    System.out.println("Game save failed");
                }
                
                updateBarsAndState();
            }
        });
        statsTimer.start();
    
        // Refresh the main frame to display the game screen
        mainFrame.setContentPane(mainPanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }


    /**
     *  This method is for displaying the inventory, this will have the different types of items, what
     *  they do, the amount of them you have and when used will apply the effects
     */
    public void displayInventory() {
        // Setup for inventory screen: stop game execution and clear current frame content
        isRunning = false;
        mainFrame.getContentPane().removeAll();
    
        // Create the main panel with a background color and set up key binding to return to game
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Blue2);
    
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToGame");
        actionMap.put("returnToGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayGame();
            }
        });
    
        // Top section: create a header panel for the inventory title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        topPanel.setOpaque(false);
        JLabel inventoryLabel = new JLabel("Inventory");
        inventoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        inventoryLabel.setForeground(Color.WHITE);
        topPanel.add(inventoryLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    
        // Center section: create a panel that lays out item panels horizontally with spacing
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    
        // Arrays holding item images, descriptions and names for creating each inventory item panel
        String[] itemPaths = {
            "/images/item1.png",
            "/images/item2.png",
            "/images/item4.png",
            "/images/item3.png",
            "/images/item5.png",
            "/images/item6.png"
        };
    
        String[] itemDescriptions = {
            "GIFT: Magic potion +25 Health +10 Happiness",
            "GIFT: Bandages  +15 Health +15 Happiness",
            "GIFT: Tennis ball +30 Happiness",
            "FOOD: Steak +35 Hunger",
            "FOOD: Kibble +20 Hunger",
            "FOOD: Salmon +30 Hunger"
        };
        String[] itemName = {
            "magic potion",
            "bandage",
            "tennis ball",
            "steak",
            "kibble",
            "salmon"
        };
    
        // Loop to create individual item panels for each inventory item
        for (int i = 0; i < 6; i++) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setBackground(new Color(255, 255, 255, 200));
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            itemPanel.setPreferredSize(new Dimension(120, 180));
    
            // Item image section: load image if available, otherwise show placeholder text
            JLabel itemImageLabel = new JLabel();
            itemImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            itemImageLabel.setVerticalAlignment(SwingConstants.CENTER);
            itemImageLabel.setPreferredSize(new Dimension(80, 80));
            URL itemURL = getClass().getResource(itemPaths[i]);
            String item = itemName[i];
            if (itemURL != null) {
                ImageIcon icon = new ImageIcon(itemURL);
                Image scaled = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
                itemImageLabel.setIcon(icon);
            } else {
                itemImageLabel.setText("No Image");
                itemImageLabel.setForeground(Color.RED);
            }
    
            // Description and quantity section: show item details and current quantity from inventory
            JLabel descriptionLabel = new JLabel(itemDescriptions[i], SwingConstants.CENTER);
            descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            descriptionLabel.setForeground(new Color(0, 128, 0));
            JLabel quantityLabel = new JLabel("Quantity: " + inventory.getItemValue(itemName[i]));
            quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
            // Button section: provide a "Use" button for the item with confirmation and item effect handling
            JButton useButton = new JButton("Use");
            useButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            useButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            useButton.setBackground(new Color(66, 133, 244));
            useButton.setForeground(Color.WHITE);
            useButton.setFocusPainted(false);
            useButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            useButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            useButton.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                        mainFrame,
                        "Are you sure you want to use this item?",
                        "Confirm Use",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (result == JOptionPane.YES_OPTION) {
                    if (inventory.getItemValue(item) != 0) { 
                        // Apply item effects; different effects are applied based on pet's current state
                        if (!(currentPet.getStats().getState().equals("angry"))) {
                            switch (item.toLowerCase()) { 
                                case "magic potion":
                                    currentPet.getStats().decay(0, -25, 0, -10);
                                case "bandage":
                                    currentPet.getStats().decay(0, -15, 0, -15);
                                case "tennis ball":
                                    currentPet.getStats().decay(0, 0, 0, -30);
                                case "steak":
                                    currentPet.getStats().decay(-35, 0, 0, 0);
                                case "kibble":
                                    currentPet.getStats().decay(-20, 0, 0, 0);
                                case "salmon":
                                    currentPet.getStats().decay(-30, 0, 0, 0);
                                inventory.removeItem(item);
                                displayInventory();
                            }
                        } else { // When pet is angry, only certain items can be used
                            switch (item.toLowerCase()) { 
                                case "magic potion":
                                    currentPet.getStats().decay(0, -25, 0, -10);
                                    inventory.removeItem(item);
                                    displayInventory();
                                    break;
                                case "bandage":
                                    currentPet.getStats().decay(0, -15, 0, -15);
                                    inventory.removeItem(item);
                                    displayInventory();
                                    break;
                                case "tennis ball":
                                    currentPet.getStats().decay(0, 0, 0, -30);
                                    inventory.removeItem(item);
                                    displayInventory();
                                    break;
                            }
                        }
                    }
                }
            });
    
            // Assemble item panel by adding image, description, quantity and button with spacing
            itemPanel.add(Box.createVerticalStrut(5));
            itemPanel.add(itemImageLabel);
            itemPanel.add(Box.createVerticalStrut(5));
            itemPanel.add(descriptionLabel);
            itemPanel.add(Box.createVerticalStrut(5));
            itemPanel.add(quantityLabel);
            itemPanel.add(Box.createVerticalStrut(5));
            itemPanel.add(useButton);
            itemPanel.add(Box.createVerticalStrut(5));
    
            centerPanel.add(itemPanel);
            centerPanel.add(Box.createHorizontalStrut(20)); // Spacing between item panels
        }
    
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    
        // Bottom section: create a panel with a back button to return to the game screen
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setOpaque(false);
        JButton backInvButton = createButton("Back");
        backInvButton.addActionListener(e -> {
            playSound("button.wav");
            displayGame();
        });
        bottomPanel.add(backInvButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        // Add a key listener to allow ESC key to also return to the main menu
        mainFrame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    playSound("button.wav");
                    displayMainMenu();
                }
            }
        });
        
        mainFrame.setFocusable(true);
        mainFrame.requestFocusInWindow();
    
        mainFrame.setContentPane(mainPanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     *  This method is for displaying the parental controls, here you can limit play, view play time and
     *  revive pets
     */
    public void displayParentalControl() {
        // Setup for parental control screen: clear current content and initialize save/load manager
        mainFrame.getContentPane().removeAll();
        SaveAndLoadGame saveGameManager = new SaveAndLoadGame(player);
    
        // Main panel for parental controls with key binding to return to main menu
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Blue2);
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "backToMain");
        actionMap.put("backToMain", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayMainMenu();
            }
        });
    
        // Top section: header panel with a title for parental controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        topPanel.setOpaque(false);
        JLabel controlLabel = new JLabel("Parental Controls");
        controlLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        controlLabel.setForeground(Color.WHITE);
        topPanel.add(controlLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    
        // Center section: panel with grid layout that holds sub-panels for time restrictions, play statistics, and pet revival
        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
    
        // Sub-panel for time restrictions: allows user to enable time controls and set allowed start/end times
        JPanel timeControlPanel = new JPanel();
        timeControlPanel.setLayout(new BoxLayout(timeControlPanel, BoxLayout.Y_AXIS));
        timeControlPanel.setBackground(new Color(255, 255, 255, 200));
        timeControlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Time Restrictions",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
    
        JCheckBox enableTimeControl = new JCheckBox("Enable Time Restrictions");
        enableTimeControl.setSelected(pc.isTimeControlEnabled());
        enableTimeControl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        enableTimeControl.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        JPanel timeSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeSelectPanel.setOpaque(false);
    
        JLabel startLabel = new JLabel("Allowed Start Time: ");
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d", i);
        }
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
    
        JComboBox<String> startHour = new JComboBox<>(hours);
        JComboBox<String> startMinute = new JComboBox<>(minutes);
        JLabel timeColon1 = new JLabel(":");
    
        JLabel endLabel = new JLabel("  Allowed End Time: ");
        endLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    
        JComboBox<String> endHour = new JComboBox<>(hours);
        JComboBox<String> endMinute = new JComboBox<>(minutes);
        JLabel timeColon2 = new JLabel(":");
    
        startHour.setSelectedItem(String.format("%02d", pc.getAllowedStart().getHour()));
        startMinute.setSelectedItem(String.format("%02d", pc.getAllowedStart().getMinute()));
        endHour.setSelectedItem(String.format("%02d", pc.getAllowedEnd().getHour()));
        endMinute.setSelectedItem(String.format("%02d", pc.getAllowedEnd().getMinute()));
    
        timeSelectPanel.add(startLabel);
        timeSelectPanel.add(startHour);
        timeSelectPanel.add(timeColon1);
        timeSelectPanel.add(startMinute);
        timeSelectPanel.add(endLabel);
        timeSelectPanel.add(endHour);
        timeSelectPanel.add(timeColon2);
        timeSelectPanel.add(endMinute);
    
        JButton saveTimeButton = createButton("Save Time Settings");
        saveTimeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveTimeButton.addActionListener(e -> {
            playSound("button.wav");
            LocalTime start = LocalTime.of(
                    Integer.parseInt((String) startHour.getSelectedItem()),
                    Integer.parseInt((String) startMinute.getSelectedItem()));
            LocalTime end = LocalTime.of(
                    Integer.parseInt((String) endHour.getSelectedItem()),
                    Integer.parseInt((String) endMinute.getSelectedItem()));
    
            pc.setPlayTimeWindow(start, end);
            pc.enableTimeControl(enableTimeControl.isSelected());
    
            boolean updated = saveGameManager.saveParentalControls("save.json", pc);
            if (!updated) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Time settings saved locally, but failed to update save file!",
                        "Partial Save",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Time restriction settings saved!",
                        "Settings Saved",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    
        timeControlPanel.add(enableTimeControl);
        timeControlPanel.add(Box.createVerticalStrut(10));
        timeControlPanel.add(timeSelectPanel);
        timeControlPanel.add(Box.createVerticalStrut(10));
        timeControlPanel.add(saveTimeButton);
    
        // Sub-panel for play statistics: displays total play time and average session length with reset option
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(new Color(255, 255, 255, 200));
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Play Statistics",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
    
        JLabel totalTimeLabel = new JLabel("Total Play Time: " + pc.getTotalPlayTime() + " minutes");
        totalTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        JLabel avgTimeLabel = new JLabel(String.format("Average Session Length: %.2f minutes", pc.getAveragePlayTime()));
        avgTimeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        avgTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        JButton resetStatsButton = createButton("Reset Statistics");
        resetStatsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetStatsButton.addActionListener(e -> {
            playSound("button.wav");
            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to reset all play statistics?",
                    "Confirm Reset",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                pc.resetStats();
                saveGameManager.saveParentalControls("save.json", pc);
                displayParentalControl(); 
            }
        });
    
        statsPanel.add(totalTimeLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(avgTimeLabel);
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(resetStatsButton);
    
        // Sub-panel for pet revival: allows the user to enter a pet name and revive it if it is dead
        JPanel revivePanel = new JPanel();
        revivePanel.setLayout(new BoxLayout(revivePanel, BoxLayout.Y_AXIS));
        revivePanel.setBackground(new Color(255, 255, 255, 200));
        revivePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Pet Revival",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
    
        JLabel reviveInfo = new JLabel("Enter the name of the pet to revive.");
        reviveInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reviveInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
    
        JButton reviveButton = createButton("Revive Pet");
        reviveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reviveButton.addActionListener(e -> {
            playSound("button.wav");
            
            String petName = JOptionPane.showInputDialog(
                    mainFrame, 
                    "Enter the name of the pet to revive:", 
                    "Revive Pet", 
                    JOptionPane.QUESTION_MESSAGE);
            if (petName == null || petName.trim().isEmpty()) {
                return; 
            }
            
            Pet[] pets = saveGameManager.loadPetsFromSaveFile("save.json");
            Pet petToRevive = null;
            for (Pet pet : pets) {
                if (pet != null && pet.getName().equalsIgnoreCase(petName.trim())) {
                    petToRevive = pet;
                    break;
                }
            }
            
            if (petToRevive == null) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "No pet found with the name \"" + petName + "\".",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!petToRevive.getStats().getState().equalsIgnoreCase("dead")) {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "The pet \"" + petName + "\" is already alive and well!",
                        "Revival Not Needed",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "Are you sure you want to revive the pet \"" + petName + "\"?",
                    "Confirm Revival",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                VitalStats newStats = new VitalStats();
                newStats.setState("alive");
                newStats.setHunger(100);
                newStats.setSleep(100);
                newStats.setHealth(100);
                newStats.setHappiness(100);
                
                boolean updated = saveGameManager.updatePetOnTheFly("save.json", petToRevive, newStats);
                if (updated) {
                    JOptionPane.showMessageDialog(
                            mainFrame,
                            "The pet \"" + petName + "\" has been revived!",
                            "Pet Revived",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(
                            mainFrame,
                            "Failed to update the pet in the save file.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    
        // Add all parental control sub-panels to the center panel
        centerPanel.add(timeControlPanel);
        centerPanel.add(statsPanel);
        centerPanel.add(revivePanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    
        // Bottom section: back button to return to main menu
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setOpaque(false);
        JButton backButton = createButton("Back");
        backButton.addActionListener(e -> {
            playSound("button.wav");
            displayMainMenu();
        });
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        mainFrame.setContentPane(mainPanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     *  This method display the mini game, where you solve a simple addition problem to gain items
     */
    public void displayMiniGame() {
        // Setup mini game screen by stopping game execution and clearing frame content
        isRunning = false;
        mainFrame.getContentPane().removeAll();
    
        // Generate a random addition problem
        int num1 = (int)(Math.random() * 10) + 1;
        int num2 = (int)(Math.random() * 10) + 1;
        int correctAnswer = num1 + num2;
    
        // Create mini game panel with grid bag layout for proper positioning of components
        JPanel miniGamePanel = new JPanel(new GridBagLayout());
        miniGamePanel.setBackground(Blue2);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
    
        // Add question label to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel questionLabel = new JLabel("What is " + num1 + " + " + num2 + " ?");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        questionLabel.setForeground(Color.WHITE);
        miniGamePanel.add(questionLabel, gbc);
    
        // Add answer input field
        gbc.gridy++;
        JTextField answerField = new JTextField(10);
        answerField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        miniGamePanel.add(answerField, gbc);
    
        // Add submit button with action listener to validate answer and award a random gift if correct
        gbc.gridy++;
        JButton submitButton = createButton("Submit");
        miniGamePanel.add(submitButton, gbc);
        submitButton.addActionListener(e -> {
            try {
                int userAnswer = Integer.parseInt(answerField.getText());
                if (userAnswer == correctAnswer) {
                    String[] gifts = {"magic potion", "bandage", "tennis ball", "steak", "kibble", "salmon"};
                    int giftIndex = (int)(Math.random() * gifts.length);
                    String gift = gifts[giftIndex];
                    JOptionPane.showMessageDialog(mainFrame, "Correct! You've received a " + gift + "!");
                    inventory.addItem(gift, 1);
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Incorrect! Try again.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter a valid number.");
            }
            displayGame();
        });
    
        // Finalize mini game screen and display it
        mainFrame.setContentPane(miniGamePanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
    }
    
    /**
     *  This method is for displaying the tutorial there are images that 
     *  you can switch through with description under
     */
    public void displayTutorial() {
        // Clear current content and prepare tutorial slide components including headings, descriptions, and images
        mainFrame.getContentPane().removeAll();
    
        String[] tutorialHeadings = {
            "Start a new game",
            "Types of pets",
            "Select the pet",
            "Name your pet",
            "Load A Game",
            "No Pets",
            "Preview Stats",
            "Empty Slots",
            "Select pet to load",
            "Vital Stats",
            "Hunger",
            "Health",
            "Happiness",
            "Sleep",
            "Inventory",
            "Quantity of Items",
            "Effects of Items",
            "Use items",
            "Mini Game",
            "How to play mini game",
            "Excercise",
            "Play",
            "Go to bed",
            "Take to vet",
            "Name and Score",
            "Back to main menu",
            "Parental Controls",
            "Set a password",
            "Enter Password",
            "Time Restrictions",
            "Start and End Time",
            "Enable Restrictions",
            "Save Time Settings",
            "Play Statistics",
            "Reset Statistics",
            "Revive Pet",
            "Select Pet to revive",
            "Back to main menu",
            "Exit Game"
        };
        String[] tutorialDescriptions = {
            "Click start new game if you want to make a new pet",
            "These are the three different pets you can choose from, Cat, Bear, Dog, and their descriptions on what they do is underneath them",
            "Click select on the pet you would like to take care of",
            "Name your pet and press ok",
            "Load A Game",
            "If you have no pets then it will show this screen",
            "This is a visual repersentation of your pet with the name, sprite and vital stats",
            "Empty slots show the available slots for new pets",
            "Click load on the pet you want to start taking care of",
            "This are your vital stats that you will have to manage during the duration of the game making sure they don't go to low",
            "This is the hunger stat you can raise it by eating certain items in the inventory if it reaches 0 then the pet becomes hungry causing certain effects",
            "This is the health stat you can raise it with items in the inventory and using the take to vet button",
            "This is the happiness stat you can raise it with items in the inventory and using the play button if you go to 0 happiness reaches 0 your pet will become angry causing certain effects",
            "This is the sleep you can increase this by using the go to bed button but if it goes to 0 it will raise by itself but will lose health",
            "This is the inventory button click it to enter the inventory",
            "This shows the quantity of each item",
            "This is where the effect of the items is show(ones shown right now are not final)",
            "This shows the button you press to use, this will remove the item and apply the effect listed",
            "This show the minigame button clicking it you enter it where you can earn items",
            "This is the mini game enter the math problem to gain a item",
            "This button when pressed it decreases sleep and hunger by 10 but also increases the happiness by 10 can also press this button by pressing the E key",
            "This button increases happiness by 10 and can also be press by P",
            "This button puts the pet to sleep you can't you any buttons but it increase sleep until full also can press B",
            "This button increases health by 10 but lowers score by 10 all other action buttons increase score by 10",
            "This is where your name and score will be displayed",
            "This button brings you back to the main menu you can also press esc",
            "Click this to access the parental controls",
            "If no password set, make one",
            "If already a password then enter it to gain access",
            "This is the panel for time restrictions",
            "Put the allowed start and end time",
            "To enable the time restrictions press enable",
            "Press this to save the settings",
            "This show play time in minutes and your average play time per session",
            "Press this to reset the statistics",
            "Click this button to revive a pet",
            "Provide the name of the pet you would like to revive",
            "Click this button to go back to main menu or press esc",
            "Click exit to close the game"
        };
        String[] tutorialImagePaths = {
            "/images/tut1.jpg",
            "/images/tut7.jpg",
            "/images/tut5.jpg",
            "/images/tut8.jpg",
            "/images/tut2.jpg",
            "/images/tut25.png",
            "/images/tut26.jpg",
            "/images/tut27.jpg",
            "/images/tut28.jpg",
            "/images/tut6.jpg",
            "/images/tut17.jpg",
            "/images/tut18.jpg",
            "/images/tut19.jpg",
            "/images/tut20.jpg",
            "/images/tut9.jpg",
            "/images/tut21.jpg",
            "/images/tut22.jpg",
            "/images/tut23.jpg",
            "/images/tut10.jpg",
            "/images/tut24.png",
            "/images/tut11.jpg",
            "/images/tut12.jpg",
            "/images/tut13.jpg",
            "/images/tut14.jpg",
            "/images/tut15.jpg",
            "/images/tut16.jpg",
            "/images/tut3.jpg",
            "/images/tut29.jpg",
            "/images/tut30.jpg",
            "/images/tut31.jpg",
            "/images/tut32.jpg",
            "/images/tut33.jpg",
            "/images/tut34.jpg",
            "/images/tut35.jpg",
            "/images/tut36.jpg",
            "/images/tut37.jpg",
            "/images/tut39.jpg",
            "/images/tut38.jpg",
            "/images/tut4.jpg"
        };
    
        final int[] currentSlideIndex = {0};
        final int IMAGE_WIDTH = 1200;
        final int IMAGE_HEIGHT = 600;
    
        // Main tutorial panel: setup using GridBagLayout and key binding to return to main menu
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Blue2);
        InputMap inputMap = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainPanel.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "returnToMainMenu");
        actionMap.put("returnToMainMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("button.wav");
                displayMainMenu();
            }
        });
    
        // Components for displaying the tutorial slide: image, heading, description and navigation bullets
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE; 
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setMinimumSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setMaximumSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.LIGHT_GRAY);
        JLabel headingLabel = new JLabel("", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headingLabel.setForeground(Color.WHITE);
        JLabel descriptionLabel = new JLabel("", SwingConstants.CENTER);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        descriptionLabel.setForeground(Color.WHITE);
        JLabel[] bulletLabels = new JLabel[tutorialImagePaths.length];
        for (int i = 0; i < bulletLabels.length; i++) {
            bulletLabels[i] = new JLabel("○");
            bulletLabels[i].setFont(new Font("SansSerif", Font.BOLD, 24));
            bulletLabels[i].setForeground(Color.BLACK);
        }
        JPanel bulletPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        bulletPanel.setBackground(Color.WHITE);
        for (JLabel lbl : bulletLabels) {
            bulletPanel.add(lbl);
        }
    
        // Navigation panel: previous and next buttons along with bullet indicators
        JButton previousButton = createButton("Previous");
        JButton nextButton = createButton("Next");
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);
        navPanel.add(previousButton, BorderLayout.WEST);
        navPanel.add(bulletPanel, BorderLayout.CENTER);
        navPanel.add(nextButton, BorderLayout.EAST);
    
        // Runnable to update the current slide based on the slide index
        Runnable updateSlide = () -> {
            int idx = currentSlideIndex[0];
            headingLabel.setText(tutorialHeadings[idx]);
            descriptionLabel.setText("<html><div style='text-align:center;'>" + tutorialDescriptions[idx] + "</div></html>");
            String path = tutorialImagePaths[idx];
            URL imageURL = getClass().getResource(path);
            if (imageURL != null) {
                ImageIcon icon = new ImageIcon(imageURL);
                Image scaled = icon.getImage().getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                imageLabel.setText("");
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("Image not found: " + path);
            }
            for (int i = 0; i < bulletLabels.length; i++) {
                bulletLabels[i].setText(i == idx ? "●" : "○");
            }
        };
    
        // Action listeners for previous and next buttons to navigate slides
        previousButton.addActionListener(e -> {
            playSound("move.wav");
            currentSlideIndex[0] = (currentSlideIndex[0] - 1 + tutorialImagePaths.length) % tutorialImagePaths.length;
            updateSlide.run();
        });
        nextButton.addActionListener(e -> {
            playSound("move.wav");
            currentSlideIndex[0] = (currentSlideIndex[0] + 1) % tutorialImagePaths.length;
            updateSlide.run();
        });
    
        // Assemble tutorial slide components into the main panel
        mainPanel.add(imageLabel, gbc);
        gbc.gridy = 1;
        mainPanel.add(headingLabel, gbc);
        gbc.gridy = 2;
        mainPanel.add(descriptionLabel, gbc);
        gbc.gridy = 3;
        mainPanel.add(navPanel, gbc);
        JButton backTutorialButton = createButton("Back to Menu");
        backTutorialButton.addActionListener(e -> {
            playSound("button.wav");
            displayMainMenu();
        });
        gbc.gridy = 4;
        mainPanel.add(backTutorialButton, gbc);
    
        mainFrame.setContentPane(mainPanel);
        mainFrame.invalidate();
        mainFrame.validate();
        mainFrame.repaint();
        updateSlide.run();
    }
    
    /**
     * This is a helper file to play sounds
     * @param fileName the name of the sound file
     */
    private void playSound(String fileName) {
        try {
            URL soundURL = getClass().getResource("/sounds/" + fileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This is a helper method to create the buttons 
     * @param text the text on the button
     * @return The button that it made
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setBackground(Blue1);
        button.setForeground(Color.white);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (button.getModel().isRollover()) {
                    button.setBackground(Blue2);
                } else {
                    button.setBackground(Blue1);
                }
            }
        });
        return button;
    }
    
    /**
     *  This is a helper method that sets the pet image 
     */
    private void setPetImage(JLabel label, String imagePath) {
        ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
        int newWidth = 150;  
        int newHeight = (int) (((double) icon.getIconHeight() / icon.getIconWidth()) * newWidth);
        Image scaledImage = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaledImage));
    }
    
    /**
     *  This is a helper method that updates the state, vital states, score and bars 
     */
    private void updateBarsAndState() {
        ParentalControls pc = new ParentalControls();
        hungerBar.setValue(vitalStats.getHunger());
        healthBar.setValue(vitalStats.getHealth());
        happinessBar.setValue(vitalStats.getHappiness());
        sleepBar.setValue(vitalStats.getSleep());
        stateLabel.setText("Score: " + player.getScore());
        if(currentPet.getStats().getHappiness() <= 25){
            happinessBar.setForeground(Color.red);
        } else {
            happinessBar.setForeground(new Color(255, 215, 0));
        }
        if(currentPet.getStats().getSleep() <= 25){
            sleepBar.setForeground(Color.red);
        } else {
            sleepBar.setForeground(new Color(65, 105, 225));
        }
        if(currentPet.getStats().getHunger() <= 25){
            hungerBar.setForeground(Color.red);
        } else {
            hungerBar.setForeground(new Color(255, 99, 71));
        }
        if(currentPet.getStats().getHealth() <= 25){
            healthBar.setForeground(Color.red);
        } else {
            healthBar.setForeground(new Color(50, 205, 50));
        }
    }
    
    /**
     *  This is a helper method to help create the pet cards in the load game page
     *  @param pet this is the pet that wants to be displayed
     *  @param petIndex this is the index of the pet in the array
     */
    private JPanel createPetCard(Pet pet, int petIndex) {
        // Create card panel to represent each pet with image, name, vital stats and a load button
        JPanel petCard = new JPanel();
        petCard.setLayout(new BoxLayout(petCard, BoxLayout.Y_AXIS));
        petCard.setBackground(new Color(255, 255, 255, 200));
        petCard.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        
        JLabel spriteLabel = new JLabel();
        spriteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        spriteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        String spritePath = "/petImages/" + pet.getType() + "/Screenshot_3.png";
        URL spriteURL = getClass().getResource(spritePath);
        if (spriteURL != null) {
            ImageIcon spriteIcon = new ImageIcon(spriteURL);
            int fixedHeight = 120;
            int originalWidth = spriteIcon.getIconWidth();
            int originalHeight = spriteIcon.getIconHeight();
            int scaledWidth = (int) ((double) originalWidth / originalHeight * fixedHeight);
            Image scaledImage = spriteIcon.getImage().getScaledInstance(scaledWidth, fixedHeight, Image.SCALE_SMOOTH);
            spriteIcon = new ImageIcon(scaledImage);
            spriteLabel.setIcon(spriteIcon);
        } else {
            spriteLabel.setText("No Image");
            spriteLabel.setForeground(Color.RED);
        }
        
        JLabel nameLabel = new JLabel(pet.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Panel to display pet vital stats as progress bars
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        statsPanel.setOpaque(false);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.setMaximumSize(new Dimension(200, 50));
        
        VitalStats stats = pet.getStats();
        addStatLabel(statsPanel, "Hunger", stats.getHunger());
        addStatLabel(statsPanel, "Health", stats.getHealth());
        addStatLabel(statsPanel, "Sleep", stats.getSleep());
        addStatLabel(statsPanel, "Happiness", stats.getHappiness());
        
        JLabel stateLabel = new JLabel("State: " + stats.getState(), SwingConstants.CENTER);
        stateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        stateLabel.setForeground(Color.black);
        stateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton loadButton = createButton("Load");
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (pc.isPlayAllowed()) {
            loadButton.addActionListener(e -> {
                playSound("button.wav");
                petName = pet.getName();
                petType = pet.getType();
                vitalStats = pet.getStats();
                hungerDecay = pet.getHungerDecay();
                healthDecay = pet.getHealthDecay();
                sleepDecay = pet.getSleepDecay();
                happinessDecay = pet.getHappinessDecay();
                currentPet = pet;
                displayGame();
            });
        } else {
            loadButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(
                    null, 
                    "You are not allowed to play!", 
                    "Access Denied", 
                    JOptionPane.ERROR_MESSAGE
                );
            });
        }
        petCard.add(Box.createVerticalStrut(10));
        petCard.add(spriteLabel);
        petCard.add(Box.createVerticalStrut(5));
        petCard.add(nameLabel);
        petCard.add(Box.createVerticalStrut(5));
        petCard.add(statsPanel);
        petCard.add(Box.createVerticalStrut(5));
        petCard.add(stateLabel);
        petCard.add(Box.createVerticalStrut(10));
        petCard.add(loadButton);
        petCard.add(Box.createVerticalStrut(10));
        
        return petCard;
    }
    
    /**
     * This is a helper method for the load game page for putting the stats under the pet
     * @param panel This is the pet card panel
     * @param statName This is the name of the stat bar
     * @param statValue This is the value of the stat
     */
    private void addStatLabel(JPanel panel, String statName, int statValue) {
        // Create a panel with a label and a progress bar to represent a pet stat
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.setOpaque(false);
        
        JLabel label = new JLabel(statName + ": ", SwingConstants.RIGHT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.DARK_GRAY);
        
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(statValue);
        bar.setStringPainted(true);
        bar.setString(statValue + "%");
        bar.setForeground(getColorForStat(statValue));
        bar.setUI(new BasicProgressBarUI() {
            @Override
            protected Color getSelectionBackground() {
                return Color.black;
            }
            
            @Override
            protected Color getSelectionForeground() {
                return Color.black;
            }
        });
        statPanel.add(label, BorderLayout.WEST);
        statPanel.add(bar, BorderLayout.CENTER);
        
        panel.add(statPanel);
    }
    
    /**
     * This method a helper method that changes the bar colour for the load game page
     * to reflect the level that, that stat is at 
     * @param value The amount of that stat is at
     * @return the colour that asscioated with that level
     */
    private Color getColorForStat(int value) {
        if (value < 25) {
            return Color.RED;
        } else if (value < 50) {
            return Color.ORANGE;
        } else if (value < 75) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }
}