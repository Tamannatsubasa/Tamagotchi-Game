/**
 *CS 2212B
 *Name: Emma Winkeler
 *Student Number: 251449478
 *Email: ewinkele@uwo.ca
 *Created: March 19, 2025
 */
public class VitalStats {
	/**
	 * an int representing how awake the pet is
	 */
	private int sleep;
	/**
	 * an int representing how full the pet is
	 */
	private int hunger;
	/**
	 * an int representing how healthy the pet is
	 */
	private int health;
	/**
	 * an int representing how happy the pet is
	 */
	private int happiness;
	/**
	 * a String representing the pet's current state
	 */
	String state;
	/**
	 * a constructor for the class
	 * puts all the vital stats at full
	 */
	public VitalStats(){
		sleep = 100;
		hunger = 100;
		health = 100;
		happiness = 100;
		state = "alive";
	}
	/**
	 * a constructor for the class that lets the stat amounts be determined
	 * @param state the pet's state
	 * @param hunger the pet's hunger
	 * @param sleep the pet's sleepiness
	 * @param health the pet's health
	 * @param happiness the pet's happiness
	 */
	public VitalStats(String state, int hunger, int sleep, int health, int happiness) {
		this.state = state;
		this.happiness = happiness;
		this.health = health;
		this.hunger = hunger;
		this.sleep = sleep;
		
		//this is to make sure that none of the stats go over 100
		decay(0,0,0,0);
	}
	/**
	 * a setter for the pet's state
	 * @param newState the pet's new state
	 */
	public void setState(String newState) {
		state = newState;
	}
	/**
	 * a setter for the pet's health
	 * @param newHealth the new value
	 */
	public void setHealth(int newHealth) {
		health = newHealth;
	}
	/**
	 * a setter for the pet's hunger
	 * @param newHunger the new value
	 */
	public void setHunger(int newHunger) {
		hunger = newHunger;
	}
	/**
	 * a setter for the pet's happiness
	 * @param newHappy the new value
	 */
	public void setHappiness(int newHappy) {
		happiness = newHappy;
	}
	/**
	 * a setter for the pet's sleepiness
	 * @param newSleep the new value
	 */
	public void setSleep(int newSleep) {
		sleep = newSleep;
	}
	/**
	 * a getter for the pet's state
	 * @return a String representing the pet's state
	 */
	public String getState() {
		return state;
	}
	/**
	 * a getter for the pet's health
	 * @return an int representing the pet's health
	 */
	public int getHealth() {
		return health;
	}
	/**
	 * a getter for the pet's hunger
	 * @return an int representing the pet's hunger
	 */
	public int getHunger() {
		return hunger;
	}
	/**
	 * a getter for the pet's happiness
	 * @return an int representing the pet's happiness
	 */
	public int getHappiness() {
		return happiness;
	}
	/**
	 * a getter for the pet's sleepiness
	 * @return an int representing the pet's sleepiness
	 */
	public int getSleep() {
		return sleep;
	}
	/**
	 * decays the pet's various stats
	 * @param hungerDecay the amount hunger is decreased by
	 * @param healthDecay the amount health is decreased by
	 * @param sleepDecay the amount sleep is decreased by
	 * @param happinessDecay the amount happiness is decreased by
	 */
	public void decay(int hungerDecay, int healthDecay, int sleepDecay, int happinessDecay) {
		//the stats are organized by importance in ascending order
		//first it's checked to make sure the stats won't go below zero
		//then checked to make sure they won't go above 100 (if, say the decay amount was a negative number)
		//lastly, if neither of the previous cases are caught, it'll decrement by the decay amount
		
		if((happiness - happinessDecay) <= 0) {
			setState("angry");
			setHappiness(0);
		} else if ((happiness - happinessDecay) >= 100) {
			setHappiness(100);
		} else {
			setHappiness(happiness - happinessDecay);
			if((!(getState()=="hungry")) &&(!(getState()=="sleeping"))&& (!(getState()=="dead"))&& (getHappiness()>=50)){
				setState("alive");
			}
		}
	
		
		if((hunger - hungerDecay) <= 0) {
			setState("hungry");
			setHunger(0);
		} else if ((hunger - hungerDecay) >= 100) {
			setHunger(100);
		} else {
			setHunger(hunger - hungerDecay);
			if((!(getState()=="angry")) &&(!(getState()=="sleeping"))&& (!(getState()=="dead"))){
				setState("alive");
			}
		}

		if((sleep - sleepDecay) <= 0) {
			setState("sleeping");
			setSleep(0);
			setHealth(getHealth()-20);
		} else if ((sleep - sleepDecay) >= 100) {
			setSleep(100);
		} else {
			setSleep(sleep - sleepDecay);
		}
		if((health - healthDecay) <= 0) {
			setState("dead");
			setHealth(0);
		} else if ((health - healthDecay) >= 100) {
			setHealth(100);
		} else {
			setHealth(health - healthDecay);
		}
	}
	/**
	 * returns a list containing all the stats in the order in which they were placed in the constructor
	 * parameters
	 * @return an int array
	 */
	public int[] getAllStats() {
		int[] stats = new int[4];
		stats[0] = hunger;
		stats[1] = sleep;
		stats[2] = health;
		stats[3] = happiness;
		return stats;
	}
}
