import java.time.LocalTime;
import java.util.Scanner;
import java.io.*;

/**
 * ParentalControls.java
 *  * ParentalControls.java
 * Handles time restrictions, play statistics, and pet editing (revival).
 *
 * Custom format:
 * sessionCount=4
 *
 * Yousef, CS2212B Winter 2025
 * 
 * Manages parental control features including playtime restrictions, 
 * usage statistics tracking, and pet revival functionality.
 * 
 * <p>Settings are saved to and loaded from 'parental.txt' file.</p>
 */

 public class ParentalControls {
    /** Default start time for allowed play (00:00) */
    private LocalTime allowedStart = LocalTime.of(0, 0);
    /** Default end time for allowed play (23:59) */
    private LocalTime allowedEnd = LocalTime.of(23, 59);
    /** Whether time restrictions are enabled */
    private boolean timeControlEnabled = false;
    /** Total minutes of playtime recorded */
    private int totalPlayTime = 0;
    /** Number of play sessions */
    private int sessionCount = 0;
    /** Parental control password */
    private String password;
    /** File path for saving settings */
    private final String FILE_PATH = "parental.txt";

    /** Initializes controls and loads saved settings */
    public ParentalControls() {
        load();
    }

    /**
     * Checks if current time is within allowed play window
     * @return true if playing is allowed now
     */

    public boolean isPlayAllowed() {
        if (!timeControlEnabled) return true;
        
        LocalTime now = LocalTime.now();
        
        if (allowedStart.isBefore(allowedEnd)) {
            return !now.isBefore(allowedStart) && !now.isAfter(allowedEnd);
        } else {
            return !now.isBefore(allowedStart) || !now.isAfter(allowedEnd);
        }
    }
    /**
     * Sets allowed play time window
     * @param start Beginning of allowed period
     * @param end End of allowed period
     */

    public void setPlayTimeWindow(LocalTime start, LocalTime end) {
        this.allowedStart = start;
        this.allowedEnd = end;
        save();
    }
    /** @param password New parental control password */
    public void setPassword(String password){
        this.password = password;
        save();
    }
    /** @return Current parental control password */
    public String getPassword(){
        return(password);
    }
    /**
     * Enables/disables time restrictions
     * @param enabled true to enable time controls
     */
    public void enableTimeControl(boolean enabled) {
        this.timeControlEnabled = enabled;
        save();
    }

    /** Records a new play session */
    public void startSession() {
        sessionCount++;
        save();
    }

    /** Increments total playtime by 1 minute */
    public void addPlayTime() {
        totalPlayTime++;
        save();
    }
    /** @return Total minutes of playtime recorded */
    public int getTotalPlayTime() {
        return totalPlayTime;
    }
    /** @return Average minutes per play session */
    public double getAveragePlayTime() {
        return sessionCount == 0 ? 0 : (double) totalPlayTime / sessionCount;
    }
    /** Resets all play statistics */
    public void resetStats() {
        totalPlayTime = 0;
        sessionCount = 0;
        save();
    }
    /**
     * Revives a pet by restoring all vital stats
     * @param stats The pet's vital stats to reset
     */
    public void revivePet(VitalStats stats) {
        stats.setState("alive");
        stats.setHunger(100);
        stats.setSleep(100);
        stats.setHealth(100);
        stats.setHappiness(100);
    }
        /** @return Allowed start time for play */
        public LocalTime getAllowedStart() {
            return allowedStart;
        }
    
        /** @return Allowed end time for play */
        public LocalTime getAllowedEnd() {
            return allowedEnd;
        }
    
        /** @return Number of play sessions */
        public int getSessionCount() {
            return sessionCount;
        }
    
        /** @return Whether time controls are enabled */
        public boolean isTimeControlEnabled() {
            return timeControlEnabled;
        }
    
    /** Saves current settings to file */
    private void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            writer.println("allowedStart=" + allowedStart);
            writer.println("allowedEnd=" + allowedEnd);
            writer.println("timeControlEnabled=" + timeControlEnabled);
            writer.println("totalPlayTime=" + totalPlayTime);
            writer.println("sessionCount=" + sessionCount);
            writer.println("password=" + password); // Added password field
        } catch (IOException e) {
            System.out.println("Failed to save parental controls: " + e.getMessage());
        }
    }
    /** Loads settings from file */
    private void load() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;
    
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split("=");
                if (parts.length != 2) continue;
    
                switch (parts[0]) {
                    case "allowedStart":
                        allowedStart = LocalTime.parse(parts[1]);
                        break;
                    case "allowedEnd":
                        allowedEnd = LocalTime.parse(parts[1]);
                        break;
                    case "timeControlEnabled":
                        timeControlEnabled = Boolean.parseBoolean(parts[1]);
                        break;
                    case "totalPlayTime":
                        totalPlayTime = Integer.parseInt(parts[1]);
                        break;
                    case "sessionCount":
                        sessionCount = Integer.parseInt(parts[1]);
                        break;
                    case "password":  // Added case to load password
                        password = parts[1];
                        break;
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Failed to load parental controls: " + e.getMessage());
        }
    }
    /* 
    @Test
    public static void Test(){
        //Test 1
        ParentalControls pc = new ParentalControls();

        pc.setPassword("Wyatt");

        String expectedValue1 = "Wyatt";
        String actualValue1 = pc.getPassword();

        if(expectedValue1.equals(actualValue1)){
            System.out.println("Parental Controls Test 1 - PASSED");
        } else {
            System.out.println("Parental Controls Test 1 - FAILED");
        }

        //Test 2 (done at 8:02 pm)

        boolean expectedValue2 = false;
        boolean actualValue2 = pc.isPlayAllowed();

        if(expectedValue2 == actualValue2){
            System.out.println("Parental Controls Test 2 - PASSED");
        } else {
            System.out.println("Parental Controls Test 2 - FAILED");
        }

        //Test 3
        Pet fluffy = new Pet();

        fluffy.getStats().setState("dead");
        fluffy.getStats().decay(100);

        pc.revivePet(fluffy.getStats());

        String expectedValue3 = "alive";
        String actualValue3 = fluffy.getStats().getState();

        if(expectedValue3.equals(actualValue3)){
            System.out.println("Parental Controls Test 3 - PASSED");
        } else {
            System.out.println("Parental Controls Test 3 - FAILED");
        }
    }
    */
}
