package brotherdong.bomberman;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Lawrence
 */
public class Profile implements Serializable {

	//TODO: Change this number for release
	private static final long serialVersionUID = -1L;

	//TODO: Add more stuff
	private String name;
	private char[] password;
	private long timeUpdated;
	private HashMap<String, Integer> killsForPlayer;
	private int kills,
		deaths,
		wins,
		losses,
		score,
		bombsUsed,
		distanceWalked,
		timePlayed;

	private final long id;

	public Profile(String name, char[] password) {
		this.name = name;
		this.password = password.clone();
		kills = 0;
		deaths = 0;
		wins = 0;
		losses = 0;
		score = 0;
		bombsUsed = 0;
		distanceWalked = 0;
		timePlayed = 0;
		killsForPlayer = new HashMap<String, Integer>();
		
		//Generate ID:
		//hash of name * last 1000 sec (last 1000000 ms)
		id = name.hashCode() * (System.currentTimeMillis() % 1000000);
	}
	
	public boolean comparePassword(char[] input) {
		return java.util.Arrays.equals(password, input);
	}
	
	public long getTimestamp() {
		return timeUpdated;
	}
	
	public void setTimestamp(long time) {
		this.timeUpdated = time;
	}
	
	public void updateTimestamp() {
		timeUpdated = System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public long getID() {
		return id;
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getWins() {
		return wins;
	}

	public int getLosses() {
		return losses;
	}

	public int getScore() {
		return score;
	}

	public int getBombsUsed() {
		return bombsUsed;
	}

	public int getDistanceWalked() {
		return distanceWalked;
	}

	public int getTimePlayed() {
		return timePlayed;
	}

	public int getKillsForPlayer(String player) {
		return killsForPlayer.containsKey(player)
			? killsForPlayer.get(player) : 0;
	}

	public void addKills(int kills) {
		this.kills += kills;
	}

	public void addDeaths(int deaths) {
		this.deaths += deaths;
	}

	public void addWins(int wins) {
		this.wins += wins;
	}

	public void addLosses(int losses) {
		this.losses += losses;
	}

	public void addScore(int score) {
		this.score += score;
	}

	public void addBombsUsed(int bombsUsed) {
		this.bombsUsed += bombsUsed;
	}

	public void addDistanceWalked(int distanceWalked) {
		this.distanceWalked += distanceWalked;
	}

	public void addTimePlayed(int timePlayed) {
		this.timePlayed += timePlayed;
	}

	public void addKillsForPlayer(String player, int kills) {
		setKillsForPlayer(player, getKillsForPlayer(player) + kills);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setBombsUsed(int bombsUsed) {
		this.bombsUsed = bombsUsed;
	}

	public void setDistanceWalked(int distanceWalked) {
		this.distanceWalked = distanceWalked;
	}

	public void setTimePlayed(int timePlayed) {
		this.timePlayed = timePlayed;
	}

	public void setKillsForPlayer(String player, int kills) {
		killsForPlayer.put(player, 0);
	}
}