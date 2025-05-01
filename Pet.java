/**
 *CS 2212B
 *Name: Emma Winkeler
 *Student Number: 251449478
 *Email: ewinkele@uwo.ca
 *Created: March 19, 2025
 *
 *a class representing a pet for a game
 */
public class Pet {
	/**The sprite (visual) representing the pet**/
	private String sprite;
	/**The pet's name**/
	private String name;
	/**how fast the pet's health decays**/
	private int healthDecay;
	/**how fast the pet's hunger decays**/
	private int hungerDecay;
	/**how fast the pet's sleep decays**/
	private int sleepDecay;
	/**how fast the pet's happiness decays**/
	private int happinessDecay;
	private String type;
	/**the pet's vital stats**/
	private VitalStats stats;
	private final static int MAX_STATS = 100;
	/**
	 * the constructor for the class
	 * @param type the type of animal the pet is
	 * @param name a String representing the pet's name
	 * @param hungerDecay an int representing the pet's hunger decay rate
	 * @param healthDecay an int representing the pet's health decay rate
	 * @param sleepDecay an int representing the pet's sleep decay rate
	 * @param happinessDecay an int representing the pet's happiness decay rate
	 * @param sprite a String representing the visual appearance of the pet
	 */
	public Pet(String type,String name, int hungerDecay, int healthDecay, int sleepDecay, int happinessDecay, String sprite) {
		this.name = name;
		this.hungerDecay = hungerDecay;
		this.happinessDecay = happinessDecay;
		this.healthDecay = healthDecay;
		this.sleepDecay = sleepDecay;
		this.sprite = sprite;
		this.type = type;

		stats = new VitalStats();
	}
	/**
	 * a setter that changes the pet's name
	 * @param newName a String that represents the pet's new name
	 */
	public void setName(String newName) {
		this.name = newName;
	}
	/**
	 * a setter that changes the pet sprite
	 * @param newSprite the new sprite for the pet
	 */
	public void setSprite(String newSprite) {
		this.sprite = newSprite;
	}
	/**
	 * a setter for the pet's type
	 * @param type the pet's type
	 */
	public void setType(String type){
		this.type = type;
	}
	/**
	 * a setter that changes the rate of decay for health
	 * @param newDecay the new rate of decay
	 */
	public void setHealthDecay(int newDecay) {
		this.healthDecay = newDecay;
	}
	/**
	 * a setter that changes the rate of decay for hunger
	 * @param newDecay the new rate of decay
	 */
	public void setHungerDecay(int newDecay) {
		this.hungerDecay = newDecay;
	}
	/**
	 * a setter that changes the rate of decay for sleep
	 * @param newDecay the new rate of decay
	 */
	public void setSleepDecay(int newDecay) {
		this.sleepDecay = newDecay;
	}
	/**
	 * a setter that changes the rate of decay for happiness
	 * @param newDecay the new rate of decay
	 */
	public void setHappinessDecay(int newDecay) {
		this.happinessDecay = newDecay;
	}
	/**
	 * a setter for changing the pet's vital stats
	 * @param newStats
	 */
	public void setStats(VitalStats newStats) {
		stats = newStats;
	}
	/**
	 * a getter for the pet's type
	 * @return a String representing the pet's type
	 */
	public String getType(){
		return type; 
	}
	/**
	 * a getter that returns the pet's name
	 * @return a String representation of the pet's name
	 */
	public String getName() {
		return name;
	}
	/**
	 * a getter for the pet's sprite
	 * @return a string representation of the pet's sprite
	 */
	public String getSprite() {
		return sprite;
	}
	/**
	 * a getter for the pet's health decay
	 * @return an int representing the rate of decay
	 */
	public int getHealthDecay() {
		return healthDecay;
	}
	/**
	 * a getter for the pet's happiness decay
	 * @return an int representing the rate of decay
	 */
	public int getHappinessDecay() {
		return happinessDecay;
	}
	/**
	 * a getter for the pet's sleep decay
	 * @return an int representing the rate of decay
	 */
	public int getSleepDecay() {
		return sleepDecay;
	}
	/**
	 * a getter for the pet's hunger decay
	 * @return an int representing the rate of decay
	 */
	public int getHungerDecay() {
		return hungerDecay;
	}
	/**
	 * a getter that provides the pet's Vital stats
	 * @return the VitalStats of the pet
	 */
	public VitalStats getStats() {
		return stats;
	}
	/**
	 * represents one clock tick
	 * its purpose is to decrement the various vital stats of the pet 
	 */
	public void oneTick() {
		stats.decay(hungerDecay, healthDecay, sleepDecay, happinessDecay);
	}
	/**
	 * fully heals and restores all of the pet's various vital stats
	 */
	public void fullHeal() {
		stats.decay(-100, -100, -100, -100);
	}
	
    //@Test
    public static void Test(){
        Pet pet = new Pet("dog", "fluffy", 2, 1, 3, 1, "random");

		//Test 1
		String[] expectedValue1= {"dog", "fluffy"};
		String[] actualValue1={pet.getType(), pet.getName()};
		if(expectedValue1.equals(actualValue1)){
			System.out.println("Pet Test 1 - PASSED");
		} else {
			System.out.println("Pet Test 1 - FAILED");
		}

		//Test 2
		int[] expectedValue2={98, 97, 99, 99};
		pet.oneTick();
		int actualValue2[] = pet.getStats().getAllStats();

		boolean fail2 = false;
		for(int i = 0; i < 4; i++){
			if(expectedValue2[i] != actualValue2[i]){
				System.out.println("Pet Test 2 - FAILED");
				fail2 = true;
			}
		}
		if(!fail2){
			System.out.println("Pet Test 2 - PASSED");
		}

		//Test 3
		pet.fullHeal();

		int[] expectedValue3={100, 100, 100, 100};
		int actualValue3[] = pet.getStats().getAllStats();

		boolean fail3 = false;
		for(int i = 0; i < 4; i++){
			if(expectedValue3[i] != actualValue3[i]){
				System.out.println("Pet Test 3 - FAILED");
				fail3 = true;
			}
		}
		if(!fail3){
			System.out.println("Pet Test 3 - PASSED");
		}
    }
	
}
